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

import org.yes.cart.dao.GenericDAO;
import org.yes.cart.domain.entity.DataGroup;
import org.yes.cart.service.domain.DataGroupService;

import java.util.List;

/**
 * User: denispavlov
 * Date: 01/06/2015
 * Time: 13:02
 */
public class DataGroupServiceImpl extends BaseGenericServiceImpl<DataGroup> implements DataGroupService {

    public DataGroupServiceImpl(final GenericDAO<DataGroup, Long> genericDao) {
        super(genericDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataGroup> findByType(final String type) {
        return getGenericDao().findByNamedQuery("DATAGROUPS.BY.TYPE", type);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DataGroup findByName(final String name) {
        final List<DataGroup> groups = getGenericDao().findByNamedQuery("DATAGROUP.BY.NAME", name);
        if (groups.isEmpty()) {
            return null;
        }
        return groups.get(0);
    }
}
