/*
 * Copyright 2009 Denys Pavlov, Igor Azarnyi
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
package org.yes.cart.domain.dto;


import org.yes.cart.domain.entity.Identifiable;
import org.yes.cart.domain.misc.Pair;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Customer order detail DTO interface.
 * Represent single order line in order with delivery details.
 */

public interface CustomerOrderDetailDTO extends Identifiable {

    /**
     * Get order detail pk value.
     *
     * @return detail pk value
     */
    long getCustomerOrderDetId();

    /**
     * Set detail pk value
     *
     * @param customerOrderDetId set pk value.
     */
    void setCustomerOrderDetId(long customerOrderDetId);

    /**
     * Get sku code .
     * @return sku code .
     */
    String getSkuCode();

    /**
     * Set sku code .
     * @param skuCode sku code .
     */
    void setSkuCode(String skuCode);

    /**
     * Get sku name.
     * @return  sku name.
     */
    String getSkuName();

    /**
     * Set  sku name.
     * @param skuName sku name.
     */
    void setSkuName(String skuName);

    /**
     * @return supplier code.
     */
    String getSupplierCode();

    /**
     * Set supplier code (fulfilment centre/warehouse code).
     *
     * @param supplierCode supplier code
     */
    void setSupplierCode(String supplierCode);


    /**
     * @return delivery group.
     */
    String getDeliveryGroup();

    /**
     * Set delivery group
     *
     * @param deliveryGroup delivery group
     */
    void setDeliveryGroup(String deliveryGroup);


    /**
     * @return item group.
     */
    String getItemGroup();

    /**
     * Set item group
     *
     * @param itemGroup item group
     */
    void setItemGroup(String itemGroup);

    /**
     * Get quantity.
     * @return  quantity.
     */
    BigDecimal getQty();

    /**
     * Set product qty.
     * @param qty       quantity.
     */
    void setQty(BigDecimal qty) ;

    /**
     * Get price of product, which is in delivery.
     * @return deal price.
     */
    BigDecimal getPrice() ;

    /**
     * Set deal price.
     *
     * @param price deal price.
     */
    void setPrice(BigDecimal price);



    /**
     * Get the sku sale price including all promotions.
     *
     * @return after tax price
     */
    BigDecimal getNetPrice();

    /**
     * Set net price (price before tax).
     *
     * @param netPrice price before tax
     */
    void setNetPrice(final BigDecimal netPrice);

    /**
     * Get the sku sale price including all promotions.
     *
     * @return before tax price
     */
    BigDecimal getGrossPrice();

    /**
     * Set net price (price after tax).
     *
     * @param grossPrice price after tax
     */
    void setGrossPrice(final BigDecimal grossPrice);

    /**
     * Get tax code used for this item.
     *
     * @return tax code
     */
    String getTaxCode();

    /**
     * Set tax code reference.
     *
     * @param taxCode tax code
     */
    void setTaxCode(final String taxCode);

    /**
     * Get tax rate for this item.
     *
     * @return tax rate 0-99
     */
    BigDecimal getTaxRate();

    /**
     * Set tax rate used (0-99).
     *
     * @param taxRate tax rate
     */
    void setTaxRate(final BigDecimal taxRate);

    /**
     * Tax exclusive of price flag.
     *
     * @return true if exclusive, false if inclusive
     */
    boolean isTaxExclusiveOfPrice();

    /**
     * Set whether this tax is included or excluded from price.
     *
     * @param taxExclusiveOfPrice tax flag
     */
    void setTaxExclusiveOfPrice(final boolean taxExclusiveOfPrice);


    /**
     * Get sale price.
     * @return price
     */
    BigDecimal getSalePrice();

    /**
     * Set sale price.
     * @param salePrice to set.
     */
    void setSalePrice(BigDecimal salePrice);

    /**
     * Get price in catalog.
     * @return price in catalog.
     */
    BigDecimal getListPrice();

    /**
     * Set price in catalog.
     * @param listPrice price in catalog.
     */
    void setListPrice(BigDecimal listPrice);


    /**
     * This is a configurable product.
     *
     * @return true if this is a configurable product.
     */
    boolean isConfigurable();

    /**
     * Set configurable
     *
     * @param configurable true if this is a configurable product.
     */
    void setConfigurable(boolean configurable);

    /**
     * This product not to be sold separately.
     *
     * @return not to be sold separately product.
     */
    boolean isNotSoldSeparately();

    /**
     * Set not sold separately
     *
     * @param notSoldSeparately not to be sold separately product.
     */
    void setNotSoldSeparately(boolean notSoldSeparately);

    /**
     * Returns true if this item has been added as gift as
     * a result of promotion.
     *
     * @return true if this is a promotion gift
     */
    boolean isGift();

    /**
     * @param gift set gift flag
     */
    void setGift(boolean gift);

    /**
     * Returns true if promotions have been applied to this
     * item.
     *
     * @return true if promotions have been applied
     */
    boolean isPromoApplied();

    /**
     * @param promoApplied set promotion applied flag
     */
    void setPromoApplied(boolean promoApplied);

    /**
     * Comma separated list of promotion codes that have been applied
     * for this cart item.
     *
     * @return comma separated promo codes
     */
    String getAppliedPromo();

    /**
     * @param appliedPromo comma separated promo codes
     */
    void setAppliedPromo(String appliedPromo);

    /**
     * Total amount for this line.
     *
     * @return  qty * grossPrice;
     */
    BigDecimal getLineTotal();

    /**
     * Total amount for this line.
     *
     * @param lineTotal qty * grossPrice;
     */
    void setLineTotal(BigDecimal lineTotal);

    /**
     * Total tax amount for this line.
     *
     * @return qty * (grossPrice - netPrice)
     */
    BigDecimal getLineTax();

    /**
     * Total tax amount for this line.
     *
     * @param lineTax qty * (grossPrice - netPrice)
     */
    void setLineTax(BigDecimal lineTax);

    /**
     * @return all values mapped to codes
     */
    Map<String, Pair<String, Map<String, String>>> getAllValues();

    /**
     * @param allValues all values
     */
    void setAllValues(Map<String, Pair<String, Map<String, String>>> allValues);

}
