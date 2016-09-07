/*
 * Copyright 2009 - 2016 Denys Pavlov, Igor Azarnyi
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

package org.yes.cart.service.vo.impl;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.yes.cart.constants.Constants;
import org.yes.cart.domain.dto.AttrValueCustomerDTO;
import org.yes.cart.domain.dto.CustomerDTO;
import org.yes.cart.domain.dto.ShopDTO;
import org.yes.cart.domain.misc.MutablePair;
import org.yes.cart.domain.misc.Pair;
import org.yes.cart.domain.vo.VoAttrValueCustomer;
import org.yes.cart.domain.vo.VoCustomer;
import org.yes.cart.domain.vo.VoCustomerInfo;
import org.yes.cart.domain.vo.VoCustomerShopLink;
import org.yes.cart.service.dto.DtoAttributeService;
import org.yes.cart.service.dto.DtoCustomerService;
import org.yes.cart.service.federation.FederationFacade;
import org.yes.cart.service.vo.VoAssemblySupport;
import org.yes.cart.service.vo.VoCustomerService;
import org.yes.cart.service.vo.VoIOSupport;

import java.util.*;

/**
 * User: denispavlov
 * Date: 21/08/2016
 * Time: 14:29
 */
public class VoCustomerServiceImpl implements VoCustomerService {

    private final DtoCustomerService dtoCustomerService;
    private final DtoAttributeService dtoAttributeService;

    private final FederationFacade federationFacade;
    private final VoAssemblySupport voAssemblySupport;
    private final VoIOSupport voIOSupport;

    private Set<String> skipAttributesInView = Collections.emptySet();

    private final VoAttributesCRUDTemplate<VoAttrValueCustomer, AttrValueCustomerDTO> voAttributesCRUDTemplate;

    public VoCustomerServiceImpl(final DtoCustomerService dtoCustomerService,
                                 final DtoAttributeService dtoAttributeService,
                                 final FederationFacade federationFacade,
                                 final VoAssemblySupport voAssemblySupport,
                                 final VoIOSupport voIOSupport) {
        this.dtoCustomerService = dtoCustomerService;
        this.dtoAttributeService = dtoAttributeService;
        this.federationFacade = federationFacade;
        this.voAssemblySupport = voAssemblySupport;
        this.voIOSupport = voIOSupport;

        this.voAttributesCRUDTemplate =
                new VoAttributesCRUDTemplate<VoAttrValueCustomer, AttrValueCustomerDTO>(
                        VoAttrValueCustomer.class,
                        AttrValueCustomerDTO.class,
                        Constants.CUSTOMER_IMAGE_REPOSITORY_URL_PATTERN,
                        this.dtoCustomerService,
                        this.dtoAttributeService,
                        this.voAssemblySupport,
                        this.voIOSupport
                )
        {
            @Override
            protected boolean skipAttributesInView(final String code) {
                return skipAttributesInView.contains(code);
            }

            @Override
            protected long determineObjectId(final VoAttrValueCustomer vo) {
                return vo.getCustomerId();
            }

            @Override
            protected Pair<Boolean, String> verifyAccessAndDetermineObjectCode(final long objectId) throws Exception {

                boolean accessible = federationFacade.isManageable(objectId, CustomerDTO.class);
                if (!accessible) {
                    return new Pair<>(false, null);
                }
                final CustomerDTO customer = dtoCustomerService.getById(objectId);
                return new Pair<>(true, String.valueOf(customer.getCustomerId()));
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public List<VoCustomerInfo> getFiltered(final String filter, final int max) throws Exception {

        final List<VoCustomerInfo> results = new ArrayList<>();

        int start = 0;
        do {
            final List<CustomerDTO> batch = dtoCustomerService.findBy(filter, start, max);
            if (batch.isEmpty()) {
                break;
            }
            federationFacade.applyFederationFilter(batch, CustomerDTO.class);
            results.addAll(voAssemblySupport.assembleVos(VoCustomerInfo.class, CustomerDTO.class, batch));

            start += max;
        } while (results.size() < max);
        return results.size() > max ? results.subList(0, max) : results;

    }

    /**
     * {@inheritDoc}
     */
    public VoCustomer getById(final long id) throws Exception {
        if (federationFacade.isManageable(id, CustomerDTO.class)) {
            final CustomerDTO customerDTO = dtoCustomerService.getById(id);
            final VoCustomer vo = voAssemblySupport.assembleVo(VoCustomer.class, CustomerDTO.class, new VoCustomer(), customerDTO);

            vo.setAttributes(getCustomerAttributes(customerDTO.getCustomerId()));

            final Map<ShopDTO, Boolean> links = dtoCustomerService.getAssignedShop(id);
            for (final Map.Entry<ShopDTO, Boolean> assign : links.entrySet()) {
                final VoCustomerShopLink link = new VoCustomerShopLink();
                link.setCustomerId(vo.getCustomerId());
                link.setShopId(assign.getKey().getShopId());
                link.setDisabled(assign.getValue());
                vo.getCustomerShops().add(link);
            }

            return vo;
        } else {
            throw new AccessDeniedException("Access is denied");
        }
    }

    /**
     * {@inheritDoc}
     */
    public VoCustomer update(final VoCustomer vo) throws Exception {

        if (federationFacade.isManageable(vo.getCustomerId(), CustomerDTO.class)) {
            final CustomerDTO customerDTO = dtoCustomerService.getById(vo.getCustomerId());
            dtoCustomerService.update(
                    voAssemblySupport.assembleDto(CustomerDTO.class, VoCustomerInfo.class, customerDTO, vo)
            );

            final Map<ShopDTO, Boolean> links = dtoCustomerService.getAssignedShop(vo.getCustomerId());
            for (final Map.Entry<ShopDTO, Boolean> assign : links.entrySet()) {
                if (federationFacade.isShopAccessibleByCurrentManager(assign.getKey().getShopId())) {
                    dtoCustomerService.revokeShop(vo.getCustomerId(), assign.getKey().getShopId(), true);
                }
            }

            for (final VoCustomerShopLink link : vo.getCustomerShops()) {
                if (federationFacade.isShopAccessibleByCurrentManager(link.getShopId())) {
                    dtoCustomerService.grantShop(vo.getCustomerId(), link.getShopId(), false);
                }
            }

        } else {
            throw new AccessDeniedException("Access is denied");
        }
        return getById(vo.getCustomerId());
    }

    /**
     * {@inheritDoc}
     */
    public VoCustomer create(final VoCustomer vo) throws Exception {

        final List<CustomerDTO> existing = dtoCustomerService.findCustomer(vo.getEmail(), null, null, null, null, null, null);
        final Map<String, Set<Long>> registered = new HashMap<>();
        if (!existing.isEmpty()) {
            for (final CustomerDTO existingDto : existing) {
                final Map<ShopDTO, Boolean> shops = dtoCustomerService.getAssignedShop(existingDto.getCustomerId());
                final Set<Long> shopIds = new HashSet<>();
                for (final ShopDTO shopDTO : shops.keySet()) {
                    shopIds.add(shopDTO.getShopId());
                }
                if (registered.containsKey(existingDto.getEmail())) {
                    registered.get(existingDto.getEmail()).addAll(shopIds);
                } else {
                    registered.put(existingDto.getEmail(), shopIds);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(vo.getCustomerShops())) {

            final Set<Long> alreadyRegistered = registered.get(vo.getEmail());

            for (final VoCustomerShopLink link : vo.getCustomerShops()) {
                if (!federationFacade.isShopAccessibleByCurrentManager(link.getShopId())) {
                    throw new AccessDeniedException("Access is denied");
                }
                if (alreadyRegistered != null && alreadyRegistered.contains(link.getShopId())) {
                    throw new DuplicateKeyException(vo.getEmail() + " is already registered in " + link.getShopId());
                }
            }
        } else {
            throw new AccessDeniedException("Access is denied");
        }


        CustomerDTO customerDTO = dtoCustomerService.getNew();
        customerDTO = dtoCustomerService.create(
                voAssemblySupport.assembleDto(CustomerDTO.class, VoCustomerInfo.class, customerDTO, vo)
        );

        for (final VoCustomerShopLink link : vo.getCustomerShops()) {
            dtoCustomerService.grantShop(customerDTO.getCustomerId(), link.getShopId(), false);
        }

        return getById(customerDTO.getCustomerId());
    }

    /**
     * {@inheritDoc}
     */
    public void remove(final long id) throws Exception {
        if (federationFacade.isCurrentUserSystemAdmin()) {
            dtoCustomerService.remove(id);
        } else {
            throw new AccessDeniedException("Access is denied");
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<VoAttrValueCustomer> getCustomerAttributes(final long customerId) throws Exception {

        return voAttributesCRUDTemplate.verifyAccessAndGetAttributes(customerId);

    }

    /**
     * {@inheritDoc}
     */
    public List<VoAttrValueCustomer> update(final List<MutablePair<VoAttrValueCustomer, Boolean>> vo) throws Exception {

        final long customerId = voAttributesCRUDTemplate.verifyAccessAndUpdateAttributes(vo);

        return getCustomerAttributes(customerId);
    }

    @Override
    public void resetPassword(final long customerId, final long shopId) throws Exception {
        if (federationFacade.isManageable(customerId, CustomerDTO.class) && federationFacade.isShopAccessibleByCurrentManager(shopId)) {

            dtoCustomerService.remoteResetPassword(dtoCustomerService.getById(customerId), shopId);

        } else {
            throw new AccessDeniedException("Access is denied");
        }
    }

    /**
     * Spring IoC
     *
     * @param attributes attributes to skip
     */
    public void setSkipAttributesInView(List<String> attributes) {
        this.skipAttributesInView = new HashSet<>(attributes);
    }
}
