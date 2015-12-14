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

package org.yes.cart.bulkcommon.service.support.impl;

import org.yes.cart.bulkcommon.model.ImpExDescriptor;
import org.yes.cart.bulkcommon.model.ImpExTuple;
import org.yes.cart.bulkcommon.model.ValueAdapter;
import org.yes.cart.bulkcommon.service.support.LookUpQuery;
import org.yes.cart.bulkcommon.service.support.LookUpQueryParameterStrategy;

import java.util.List;

/**
 * Generates a native sql string with all parameter placeholders replaced.
 * No parameter object is available, just plain sql string.
 *
 * User: denispavlov
 * Date: 12-08-08
 * Time: 9:22 AM
 */
public class ImpExDescriptorNativeInsertStrategy extends AbstractByParameterByColumnNameStrategy
        implements LookUpQueryParameterStrategy {

    @Override
    protected void addParameter(final int index, final Object param, final StringBuilder query, final List<Object> params) {
        if (param == null) {
            final char character = query.charAt(query.length() - 1);
            if (character != '\'' && character != '"' && character != '#') {
                query.append("NULL");
            } // else it is enclosed by quotes or part of I18N so just leave empty
        } else {
            query.append(param);
        }
    }

    /** {@inheritDoc} */
    public LookUpQuery getQuery(final ImpExDescriptor descriptor,
                                final Object masterObject,
                                final ImpExTuple tuple,
                                final ValueAdapter adapter,
                                final String queryTemplate) {

        final StringBuilder sql = new StringBuilder();
        replaceColumnNamesInTemplate(queryTemplate, sql, null, descriptor, masterObject, tuple, adapter);
        return new NativeSQLQuery(sql.toString());
    }
}