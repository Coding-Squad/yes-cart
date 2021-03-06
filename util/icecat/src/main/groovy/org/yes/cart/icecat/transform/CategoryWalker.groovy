/*
 * Copyright 2009 Inspire-Software.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */





package org.yes.cart.icecat.transform

import org.xml.sax.InputSource
import org.yes.cart.icecat.transform.domain.Category
import org.yes.cart.icecat.transform.xml.CategoryFeaturesListHandler
import org.yes.cart.icecat.transform.xml.CategoryHandler
import org.yes.cart.icecat.transform.xml.ProductHandler
import org.yes.cart.icecat.transform.xml.ProductPointerHandler

import javax.xml.parsers.SAXParserFactory

import org.yes.cart.icecat.transform.csv.*
import org.yes.cart.icecat.transform.domain.ProductPointer
import org.yes.cart.icecat.transform.domain.Feature

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 5/7/12
 * Time: 10:11 PM
 */
class CategoryWalker {

    Context context;

    CategoryWalker(Context context) {

        this.context = context;

    }


    void collectData() {

        println("Parsing categories...")

        def path =  "export/freexml.int/refs.xml";

        loadFile(path, path);

        def FileInputStream refs = new FileInputStream("$context.dataDirectory/export/freexml.int/refs.xml");
        def handler = new CategoryHandler(context.categories, context.langId, context.langNames)
        def reader = SAXParserFactory.newInstance().newSAXParser().XMLReader
        reader.setContentHandler(handler)
        reader.parse(new InputSource(refs))
        refs.close();
        println("Added " + handler.counter + " categories...")

        println("Parsing categories features...")
        refs = new FileInputStream("$context.dataDirectory/export/freexml.int/refs.xml");
        def categoryFeaturelistHandler = new CategoryFeaturesListHandler(handler.categoryMap,
                context.langId, context.langNames)
        reader = SAXParserFactory.newInstance().newSAXParser().XMLReader
        reader.setContentHandler(categoryFeaturelistHandler)
        reader.parse(new InputSource(refs))
        refs.close();
        println("Added " + categoryFeaturelistHandler.counter + " features to categories...")

        println("Parsing product indexes...")
        def productPointerHandler = new ProductPointerHandler(handler.categoryMap, context.mindata, context.productsPerCategoryLimit);
        productPointerHandler.allLangs = Arrays.asList(context.langNames.split(','));
        def productReadeReader = SAXParserFactory.newInstance().newSAXParser().XMLReader

        def i = 0;
        for (String dir : Arrays.asList(context.productDir.split(','))) {
            loadFile("export/freexml.int/$dir/", "export/freexml.int/$dir/index.html");
            println("index file: $context.dataDirectory/export/freexml.int/$dir/index.html")
            def indexes = new FileInputStream("$context.dataDirectory/export/freexml.int/$dir/index.html");
            productPointerHandler.lang = context.langNames.split(',')[i];
            productReadeReader.setContentHandler(productPointerHandler)
            productReadeReader.parse(new InputSource(indexes))
            i++
        }

        int langCount = context.productDir.split(',').length;
        List<String> ppToRemove = new ArrayList<String>();
        for (ProductPointer pp : productPointerHandler.productMap.values()) {
            for (int ii = 0; ii < langCount; ii++) {
                String checkLang = context.langNames.split(',')[ii];
                if (pp.path.size() == langCount - 1 && !pp.path.containsKey("uk")) {
                    // if UK is missing
                    pp.path.put("uk", pp.path.get("ru"));
                }
                if (pp.path.size() < langCount - 1) {
                    for (Category cat : pp.categories.values()) {
                        cat.productPointer.remove(pp);
                    }
                    ppToRemove.add(pp.Product_ID);
                    println("Product $pp.Product_ID does not have path for $checkLang ... removing")
                    break;
                }
            }
        }
        for (String ppId : ppToRemove) {
            productPointerHandler.productMap.remove(ppId);
        }

        println("Added " + productPointerHandler.productMap.size() + " products to all categories...")
        List<String> toRemove = new ArrayList<String>();
        for (Map.Entry<String, Category> entry : handler.categoryMap.entrySet()) {
            Category cat = entry.getValue();
            println("Added " + cat.productPointer.size() + " products to category $cat.id ...");
            if (cat.productPointer.size() == 0) {
                toRemove.add(entry.getKey());
            }
        }
        for (String catId : toRemove) {
            println("Removing $catId because it has no products...");
            handler.categoryMap.remove(catId);
        }

        println("Download product data...")
        //check the cache and download product's xml if need
        downloadProducts(productPointerHandler.productMap,
                Arrays.asList(context.productDir.split(',')),
                Arrays.asList(context.langNames.split(',')))
        parseProducts(handler.categoryMap, categoryFeaturelistHandler.featureMap, productPointerHandler.productMap,
                Arrays.asList(context.productDir.split(',')),
                Arrays.asList(context.langNames.split(',')),
                Arrays.asList(context.langId.split(',')))

        downloadProductPictures(productPointerHandler.productMap,
                Arrays.asList(context.productDir.split(',')),
                Arrays.asList(context.langNames.split(',')),
                Arrays.asList(context.langId.split(',')))
        println("Added products data to categories...")

        println("Generating CSV files...")

        def rootDir = "$context.dataDirectory/export/freexml.int/csvresult/$context.productDir";

        //create folder for csv
        new File(rootDir).mkdirs();

        println("Generating producttypenames.csv")
        new ProductTypeCsvAdapter(handler.categoryMap).toCsvFile("$rootDir/producttypenames.csv");

        println("Generating attributenames.csv")
        new AttributeCsvAdapter(categoryFeaturelistHandler.featureMap).toCsvFile("$rootDir/attributenames.csv");

        println("Generating productypeattributeviewgroupnames.csv")
        new ProductTypeViewGroupCsvAdapter(handler.categoryMap).toCsvFile("$rootDir/productypeattributeviewgroupnames.csv");

        println("Generating producttypeattrnames.csv")
        new ProductTypeAttributeCsvAdapter(handler.categoryMap).toCsvFile("$rootDir/producttypeattrnames.csv");

        println("Generating categorynames.csv")
        new CategoryCsvAdapter(handler.categoryMap).toCsvFile("$rootDir/categorynames.csv");

        println("Generating productcategorynames.csv")
        new ProductCategoryCsvAdapter(productPointerHandler.productMap).toCsvFile("$rootDir/productcategorynames.csv");

        println("Generating brandnames.csv")
        new BrandCsvAdapter(handler.categoryMap).toCsvFile("$rootDir/brandnames.csv");

        println("Generating skuinventory.csv")
        new ProductInventoryCsvAdapter(productPointerHandler.productMap).toCsvFile("$rootDir/skuinventory.csv");

        println("Generating skuprices.csv")
        new ProductPricesCsvAdapter(productPointerHandler.productMap).toCsvFile("$rootDir/skuprices.csv");

        println("Generating productnames.csv")
        new ProductsCsvAdapter(productPointerHandler.productMap, context.langNames.split(',')[0]).toCsvFile("$rootDir/productnames.csv");

        println("Generating productsattributes.csv")
        new ProductsAttributesCsvAdapter(productPointerHandler.productMap, context.langNames.split(',')[0]).toCsvFile("$rootDir/productsattributes.csv");

        println("Generating productskunames.csv")
        new ProductsSkuCsvAdapter(productPointerHandler.productMap, context.langNames.split(',')[0]).toCsvFile("$rootDir/productskunames.csv");

        // Main warehouse is part of initial data
        // new File("$rootDir/warehouse.csv").write("Ware house code;name;description\nMain;Main warehouse;Main warehouse", 'UTF-8');

        println("All done...")

    }

    private void loadFile(String from, String to) {
        def File refsFile = new File("$context.dataDirectory/$to");
        if (!refsFile.exists()) {

            def authString = "$context.login:$context.pwd".getBytes().encodeBase64().toString();

            println("Loading $context.url$from into " + refsFile.absolutePath);
            def conn = "$context.url$from".toURL().openConnection();
            new File(refsFile.getParent()).mkdirs();
            refsFile.createNewFile();
            def fos = new FileOutputStream(refsFile);

            conn.setRequestProperty("Authorization", "Basic ${authString}");
            fos << conn.getInputStream();

        }
    }


    private def dumpProducts(Map<String, Category> categoryMap) {
        StringBuilder builder = new StringBuilder();
        builder.append("name;category;producttype;brand;availability;skucode;qty;price;barcode;attributes;description\n");
        categoryMap.values().each {
            Category cat = it;
            cat.product.each {
                builder.append(it.toString());
            }
        }
        return builder.toString();
    }


    private def parseProducts(Map<String, Category> categoryMap,
                              Map<String, Feature> featureMap,
                              Map<String, ProductPointer> productMap,
                              List<String> dirs, List<String> langs, List<String> langsids) {
        productMap.values().each {
            for (int i = 0; i < dirs.size(); i++) {
                String cacheFolderName = createCacheFolder(dirs[i])
                String lang = langs.get(i);
                String langId = langsids.get(i);
                String path = it.path.get(lang);
                String langUsed = lang;
                /* Hack: Ukrainian version of icecat has many missing values, try to use RU for it */
                if (path == null && lang == "uk") {
                    println("No path for $it.Product_ID in $lang trying ru ");
                    path = it.path.get("ru");
                    langUsed = "ru";
                }
                /* eof hack */
                if (path == null) { // failsafe
                    println("No path for $it.Product_ID in $lang trying en ");
                    path = it.path.get("en");
                    langUsed = "en";
                }
                println("Path for $it.Product_ID in $lang is $path (lang used: $langUsed)");

                try {
                    String productFile = cacheFolderName + path.substring(1 + path.lastIndexOf("/"));

                    def FileInputStream prodis = new FileInputStream(productFile);
                    def handler = new ProductHandler(categoryMap, featureMap, it, lang, langId);
                    def reader = SAXParserFactory.newInstance().newSAXParser().XMLReader
                    reader.setContentHandler(handler)
                    reader.parse(new InputSource(prodis))
                    prodis.close();

                }   catch ( Exception e) {
                    println("Failed parsing products for language: $lang, id: $langId, path: $path ");
                    e.printStackTrace();
                }
            }
        }
    }

    private def downloadProductPictures(Map<String, ProductPointer> productMap,
                                        List<String> dirs, List<String> langs, List<String> langsids) {
        def authString = "$context.login:$context.pwd".getBytes().encodeBase64().toString()
        def cacheFolder = createPictureCacheFolder();
        productMap.values().each {
            if (it.product != null) { // if we reached limit all pointers will be null

                try {

                    char idx = 'a';

                    // product name is used in file name so need en only
                    String productName
                    if (it.product.Title == null || it.product.Title.get('en') == null || it.product.Title.get('en').length() ==0) {
                        productName = "product";

                    } else {
                        productName = Util.normalize(it.product.Title.get('en'));
                    }

                    println "Scanning pictures for: $it.Prod_ID with title $productName"

                    String skuCode = it.product.Prod_id;



                    idx += downloadProductPicture(it.product.HighPic, authString, cacheFolder, idx, productName, skuCode);
                    it.product.productPicture.each {

                        //limit to 3 pictures only, because of import size
                        if (idx < 'd') {
                            idx += downloadProductPicture(it, authString, cacheFolder, (char) idx, productName, skuCode);
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private def downloadProductPicture(String url, String authString, String cacheFolderName, char idx, String productName, String skuCode) {
        try {
            //preformat filename for import
            String productFile = cacheFolderName + productName + "_" + skuCode + "_" + idx + url.substring(url.lastIndexOf("."));


            if (!(new File(productFile).exists())) {
                try {
                    //Thread.sleep(3000);
                    URLConnection conn = "$url".toURL().openConnection();
                    conn.setRequestProperty("Authorization", "Basic ${authString}")
                    InputStream input = conn.getInputStream()
                    def output = new BufferedOutputStream(new FileOutputStream(productFile));
                    output << input
                    input.close();
                    output.close();
                    println "Downloaded $url into $productFile"
                    return 1;
                } catch (FileNotFoundException e) {
                    println "File $url not exists on remote server, skipped"
                    return 0;
                }


            } else {
                println "Skipped $url"
                return 1;
            }

        } catch (Exception e) {
            println "cant download $url, because of $e.message"
            return 0;
        }
    }

    private def createPictureCacheFolder() {
        def cacheFolderName = "$context.dataDirectory/export/freexml.int/pictcache/$context.productDir/";
        File cacheFolderFile = new File(cacheFolderName);
        if (!cacheFolderFile.exists()) {
            cacheFolderFile.mkdirs();
        }
        return cacheFolderName
    }




    private def downloadProducts(Map<String, ProductPointer> productMap,
                                 List<String> dirs, List<String> langs) {

        List<ProductPointer> toRemove = new ArrayList<ProductPointer>();

        def authString = "$context.login:$context.pwd".getBytes().encodeBase64().toString()
        productMap.values().each {
            for (int i = 0; i < dirs.size(); i++) {
                String cacheFolderName = createCacheFolder(dirs[i])
                String lang = langs.get(i);
                String path = it.path.get(lang);
                try {
                    if (path!= null) {
                        String productFile = cacheFolderName + path.substring(1 + path.lastIndexOf("/"));
                        println("file: $productFile")
                        downloadSingleProduct(lang, productFile, authString, it)
                    }
                }   catch (Exception e) {
                    e.printStackTrace();

                    toRemove.add(it); // remove those for which download failed in any language
                    break;
                }

            }
        }

        for (ProductPointer pp : toRemove) {
            productMap.remove(pp.Product_ID);
            for (Category cat : pp.categories.values()) {
                cat.productPointer.remove(pp);
            }
        }

    }


    private downloadSingleProduct(String lang, String productFile, authString, productPointer) {
        if (!(new File(productFile).exists())) {
            def path = productPointer.path.get(lang);
            def conn = "$context.url$path".toURL().openConnection();
            conn.setRequestProperty("Authorization", "Basic ${authString}")
            new File(productFile) << conn.getInputStream().text
            println "Downloaded $productPointer.Model_Name"


        }
    }

    private def createCacheFolder(String dir) {
        def cacheFolderName = "$context.dataDirectory/export/freexml.int/xmlcache/$dir/";
        File cacheFolderFile = new File(cacheFolderName);
        if (!cacheFolderFile.exists()) {
            cacheFolderFile.mkdirs();
        }
        return cacheFolderName
    }


}
