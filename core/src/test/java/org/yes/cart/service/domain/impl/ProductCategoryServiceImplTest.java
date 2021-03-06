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

package org.yes.cart.service.domain.impl;

import org.junit.Test;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.yes.cart.BaseCoreDBTestCase;
import org.yes.cart.constants.ServiceSpringKeys;
import org.yes.cart.dao.GenericDAO;
import org.yes.cart.domain.entity.ProductCategory;
import org.yes.cart.service.domain.ProductCategoryService;

import java.util.List;

import static org.junit.Assert.*;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 09-May-2011
 * Time: 14:12:54
 */
public class ProductCategoryServiceImplTest extends BaseCoreDBTestCase {

    @Test
    public void testGetProductById() {
        ProductCategoryService productCategoryService =
                (ProductCategoryService) ctx().getBean(ServiceSpringKeys.PRODUCT_CATEGORY_SERVICE);
        int rez = productCategoryService.getNextRank(211L);
        assertEquals("Next rank must be 450 for 211 category", 450, rez);
        rez = productCategoryService.getNextRank(-777L);
        assertEquals("Next rank must be 50 for unexisting -777 category", 50, rez);
        rez = productCategoryService.getNextRank(116L);
        assertEquals("Next rank must be 50 for existing -116 category without products", 50, rez);
    }
             //<TPRODUCTCATEGORY PRODUCTCATEGORY_ID="10000" GUID="10000"  PRODUCT_ID="10000" CATEGORY_ID="101" RANK="999"/>

    @Test
    public void testFindByCategoryIdProductId() {

        ProductCategoryService productCategoryService =
                (ProductCategoryService) ctx().getBean(ServiceSpringKeys.PRODUCT_CATEGORY_SERVICE);
        assertNotNull(productCategoryService.findByCategoryIdProductId(101, 10000));
        assertNull(productCategoryService.findByCategoryIdProductId(101, 11000));

    }


    @Test
    public void testRemoveByProductIds() throws Exception {

        final GenericDAO<ProductCategory, Long> productCategoryDAO =
                (GenericDAO<ProductCategory, Long>) ctx().getBean("productCategoryDao");
        final ProductCategoryService productCategoryService =
                (ProductCategoryService) ctx().getBean(ServiceSpringKeys.PRODUCT_CATEGORY_SERVICE);

        final long product1pk = 11000L;

        getTxReadOnly().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus transactionStatus) {
                final List<ProductCategory> pc = productCategoryDAO.findByCriteria(
                        " where e.product.productId = ?1", product1pk);
                assertEquals(1, pc.size());
                transactionStatus.setRollbackOnly();
            }
        });

        productCategoryService.removeByProductIds(product1pk);

        getTxReadOnly().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus transactionStatus) {
                final List<ProductCategory> pc = productCategoryDAO.findByCriteria(
                        " where e.product.productId = ?1", product1pk);
                assertEquals(0, pc.size());
                transactionStatus.setRollbackOnly();
            }
        });

    }

    @Test
    public void testRemoveByCategoryProductIds() throws Exception {

        final GenericDAO<ProductCategory, Long> productCategoryDAO =
                (GenericDAO<ProductCategory, Long>) ctx().getBean("productCategoryDao");
        final ProductCategoryService productCategoryService =
                (ProductCategoryService) ctx().getBean(ServiceSpringKeys.PRODUCT_CATEGORY_SERVICE);

        final long product1pk = 11000L;
        final long[] product1categorypk = new long[1];

        getTxReadOnly().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus transactionStatus) {
                final List<ProductCategory> pc = productCategoryDAO.findByCriteria(
                        " where e.product.productId = ?1", product1pk);
                assertEquals(1, pc.size());
                product1categorypk[0] = pc.get(0).getCategory().getCategoryId();
                transactionStatus.setRollbackOnly();
            }
        });

        assertTrue(product1categorypk[0] > 0L);
        productCategoryService.removeByCategoryProductIds(product1categorypk[0], product1pk);


        getTxReadOnly().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus transactionStatus) {
                final List<ProductCategory> pc = productCategoryDAO.findByCriteria(
                        " where e.product.productId = ?1", product1pk);
                assertEquals(0, pc.size());
                transactionStatus.setRollbackOnly();
            }
        });

    }
}
