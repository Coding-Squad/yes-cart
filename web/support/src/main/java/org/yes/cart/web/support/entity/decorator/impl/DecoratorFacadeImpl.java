/*
 * Copyright 2009 Igor Azarnyi, Denys Pavlov
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

package org.yes.cart.web.support.entity.decorator.impl;

import com.google.common.collect.MapMaker;
import org.yes.cart.constants.Constants;
import org.yes.cart.domain.entity.Category;
import org.yes.cart.domain.entity.Product;
import org.yes.cart.domain.entity.ProductSku;
import org.yes.cart.service.domain.CategoryService;
import org.yes.cart.service.domain.ImageService;
import org.yes.cart.service.domain.ProductService;
import org.yes.cart.web.support.entity.decorator.CategoryDecorator;
import org.yes.cart.web.support.entity.decorator.DecoratorFacade;
import org.yes.cart.web.support.entity.decorator.ProductDecorator;
import org.yes.cart.web.support.entity.decorator.ProductSkuDecorator;
import org.yes.cart.web.support.i18n.I18NWebSupport;
import org.yes.cart.web.support.seo.BookmarkService;
import org.yes.cart.web.support.service.AttributableImageService;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * User: denispavlov
 * Date: 12-08-20
 * Time: 5:27 PM
 */
public class DecoratorFacadeImpl implements DecoratorFacade {


    private static final ConcurrentMap<String, ProductDecorator> PRODUCT_CACHE = new MapMaker()
            .concurrencyLevel(16).softValues()
            .expireAfterWrite(Constants.DEFAULT_EXPIRATION_TIMEOUT, TimeUnit.MINUTES).makeMap();


    private final ImageService imageService;
    private final AttributableImageService categoryImageService;
    private final AttributableImageService productImageService;
    private final AttributableImageService skuImageService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final BookmarkService bookmarkService;

    public DecoratorFacadeImpl(final ImageService imageService,
                               final AttributableImageService categoryImageService,
                               final AttributableImageService productImageService,
                               final AttributableImageService skuImageService,
                               final CategoryService categoryService,
                               final ProductService productService,
                               final BookmarkService bookmarkService) {
        this.imageService = imageService;
        this.categoryImageService = categoryImageService;
        this.productImageService = productImageService;
        this.skuImageService = skuImageService;
        this.categoryService = categoryService;
        this.productService = productService;
        this.bookmarkService = bookmarkService;
    }

    /** {@inheritDoc} */
    public CategoryDecorator decorate(final Category category,
                                      final String servletContextPath,
                                      final I18NWebSupport i18NWebSupport) {

        return new CategoryDecoratorImpl(
                imageService,
                categoryImageService,
                categoryService,
                category,
                servletContextPath,
                i18NWebSupport);
    }

    /** {@inheritDoc} */
    public ProductDecorator decorate(final Product product,
                                     final String servletContextPath,
                                     final I18NWebSupport i18NWebSupport,
                                     final boolean withAttributes) {

        final String key = servletContextPath + product.getProductId() + withAttributes;
        ProductDecorator decorator = PRODUCT_CACHE.get(key);

        if (decorator == null) {

            decorator = new ProductDecoratorImpl(
                    imageService,
                    productImageService,
                    categoryService,
                    product,
                    servletContextPath,
                    withAttributes,
                    productService, productService.getDefaultImage(product.getProductId()),
                    i18NWebSupport);

            PRODUCT_CACHE.put(key, decorator);
            bookmarkService.saveBookmarkForProduct(decorator);

        }

        return decorator;
    }

    /** {@inheritDoc} */
    public ProductSkuDecorator decorate(final ProductSku sku, final String servletContextPath, final I18NWebSupport i18NWebSupport) {
        return new ProductSkuDecoratorImpl(
                imageService,
                skuImageService,
                categoryService,
                sku,
                servletContextPath,
                productService,
                i18NWebSupport);
    }

}