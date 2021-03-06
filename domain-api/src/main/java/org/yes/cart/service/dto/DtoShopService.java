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

package org.yes.cart.service.dto;

import org.yes.cart.domain.dto.ShopDTO;
import org.yes.cart.exception.UnableToCreateInstanceException;
import org.yes.cart.exception.UnmappedInterfaceException;

import java.util.List;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 07-May-2011
 * Time: 11:13:01
 */
public interface DtoShopService extends GenericDTOService<ShopDTO>, GenericAttrValueService {


    /**
     * Get all top level shops.
     *
     * @return list of all <T>.
     * @throws org.yes.cart.exception.UnableToCreateInstanceException
     *          in case of reflection problem
     * @throws org.yes.cart.exception.UnmappedInterfaceException
     *          in case of configuration problem
     */
    List<ShopDTO> getAllTopLevel() throws UnmappedInterfaceException, UnableToCreateInstanceException;

    /**
     * Get all sub shops.
     *
     * @param masterId master PK
     * @return list of all <T>.
     * @throws org.yes.cart.exception.UnableToCreateInstanceException
     *          in case of reflection problem
     * @throws org.yes.cart.exception.UnmappedInterfaceException
     *          in case of configuration problem
     */
    List<ShopDTO> getAllSubs(long masterId) throws UnmappedInterfaceException, UnableToCreateInstanceException;


    /**
     * Get supported currencies by given shop.
     *
     * @param shopId given shop id.
     * @return comma separated list of supported currency codes. Example USD,EUR
     */
    String getSupportedCurrencies(long shopId);

    /**
     * Get all supported currencies by all shops.
     *
     * @return all supported currencies.
     */
    List<String> getAllSupportedCurrenciesByShops();

    /**
     * Set supported currencies by given shop.
     *
     * @param shopId     shop id
     * @param currencies comma separated list of supported currency codes. Example USD,EUR
     */
    void updateSupportedCurrencies(long shopId, String currencies);

    /**
     * Get supported shipping countries by given shop.
     *
     * @param shopId given shop id.
     * @return comma separated list of supported country codes. Example GB,US
     */
    String getSupportedShippingCountries(long shopId);

    /**
     * Set supported shipping countries by given shop.
     *
     * @param shopId     shop id
     * @param countries comma separated list of supported country codes. Example GB,US
     */
    void updateSupportedShippingCountries(long shopId, String countries);

    /**
     * Get supported billing countries by given shop.
     *
     * @param shopId given shop id.
     * @return comma separated list of supported country codes. Example GB,US
     */
    String getSupportedBillingCountries(long shopId);

    /**
     * Set supported billing countries by given shop.
     *
     * @param shopId     shop id
     * @param countries comma separated list of supported country codes. Example GB,US
     */
    void updateSupportedBillingCountries(long shopId, String countries);

    /**
     * Get supported languages by given shop.
     *
     * @param shopId given shop id.
     * @return comma separated list of supported language codes. Example en,uk,ru
     */
    String getSupportedLanguages(long shopId);

    /**
     * Set supported languages by given shop.
     *
     * @param shopId     shop id
     * @param languages comma separated list of supported language codes. Example en,uk,ru
     */
    void updateSupportedLanguages(long shopId, String languages);


    /**
     * Get shop by server domain name.
     * @param serverDomainName given domain name.
     * @return shop dto if found otherwise null.
     */
    ShopDTO getShopDtoByDomainName(String serverDomainName);

    /**
     * Update the shop disabled flag.
     *
     * @param shopId shop PK
     * @param disabled true if shop is disabled
     * @return shop dto if found otherwise null.
     */
    ShopDTO updateDisabledFlag(long shopId, boolean disabled);

    /**
     * Get CSV of PKs for disabled carrier SLA for given shop.
     *
     * @param shopId shop PK
     *
     * @return CSV of carrier SLA PKs
     */
    String getDisabledCarrierSla(long shopId);

    /**
     * Update CSV of PKs for disabled carrier SLA for given shop.
     *
     * @param shopId  shop PK
     * @param disabledPks CSV of carrier SLA PKs
     */
    void updateDisabledCarrierSla(long shopId, String disabledPks);

    /**
     * Get properties of PKs and ranks for carrier SLA for given shop.
     *
     * @param shopId shop PK
     *
     * @return Properties of pk=rank
     */
    String getSupportedCarrierSlaRanks(long shopId);

    /**
     * Set properties of PKs and ranks for carrier SLA for given shop.
     *
     * @param shopId shop PK
     * @param pksAndRanks Properties of pk=rank
     */
    void updateSupportedCarrierSlaRanks(long shopId, String pksAndRanks);

}
