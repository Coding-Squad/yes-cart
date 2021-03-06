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

package org.yes.cart.service.vo.impl;

import org.yes.cart.domain.entity.Tax;
import org.yes.cart.service.domain.TaxService;
import org.yes.cart.service.vo.VoValidationService;

import java.util.List;
import java.util.Map;

/**
 * User: denispavlov
 * Date: 25/01/2020
 * Time: 16:07
 */
public class VoValidationServiceTaxCodeImpl extends AbstractVoValidationServiceSubjectCodeFieldImpl implements VoValidationService {

    private final TaxService taxService;

    public VoValidationServiceTaxCodeImpl(final TaxService taxService) {
        this.taxService = taxService;
    }

    @Override
    protected Long getDuplicateId(final long currentId, final String valueToCheck, final Map<String, String> context) {
        final List<Tax> type = this.taxService.findByCriteria(" where e.code = ?1 ", valueToCheck);
        return type != null && type.size() > 0 && type.get(0).getTaxId() != currentId ? type.get(0).getTaxId() : null;
    }
}
