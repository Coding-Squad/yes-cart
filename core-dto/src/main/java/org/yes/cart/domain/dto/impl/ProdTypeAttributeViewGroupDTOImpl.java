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

package org.yes.cart.domain.dto.impl;

import com.inspiresoftware.lib.dto.geda.annotations.Dto;
import com.inspiresoftware.lib.dto.geda.annotations.DtoField;
import org.yes.cart.domain.dto.ProdTypeAttributeViewGroupDTO;

import java.time.Instant;
import java.util.Map;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 6/28/12
 * Time: 10:15 PM
 */
@Dto
public class ProdTypeAttributeViewGroupDTOImpl  implements ProdTypeAttributeViewGroupDTO {


    private static final long serialVersionUID = 20120628L;


    @DtoField(value = "prodTypeAttributeViewGroupId", readOnly = true)
    private long prodTypeAttributeViewGroupId;


    @DtoField(
            value = "producttype",
            converter = "producttypeId2ProductType",
            entityBeanKeys = "org.yes.cart.domain.entity.ProductType"
    )
    private long producttypeId;

    @DtoField(value = "attrCodeList")
    private String attrCodeList;

    @DtoField(value = "rank")
    private int rank;

    @DtoField(value = "name")
    private String name;

    @DtoField(value = "displayName", converter = "i18nModelConverter")
    private Map<String, String> displayNames;


    @DtoField(readOnly = true)
    private Instant createdTimestamp;
    @DtoField(readOnly = true)
    private Instant updatedTimestamp;
    @DtoField(readOnly = true)
    private String createdBy;
    @DtoField(readOnly = true)
    private String updatedBy;
    /** {@inheritDoc} */
    @Override
    public long getId() {
        return prodTypeAttributeViewGroupId;
    }

    /** {@inheritDoc} */
    @Override
    public long getProdTypeAttributeViewGroupId() {
        return prodTypeAttributeViewGroupId;
    }

    /** {@inheritDoc} */
    @Override
    public void setProdTypeAttributeViewGroupId(final long prodTypeAttributeViewGroupId) {
        this.prodTypeAttributeViewGroupId = prodTypeAttributeViewGroupId;
    }

    /** {@inheritDoc} */
    @Override
    public long getProducttypeId() {
        return producttypeId;
    }

    /** {@inheritDoc} */
    @Override
    public void setProducttypeId(final long producttypeId) {
        this.producttypeId = producttypeId;
    }

    /** {@inheritDoc} */
    @Override
    public String getAttrCodeList() {
        return attrCodeList;
    }

    /** {@inheritDoc} */
    @Override
    public void setAttrCodeList(final String attrCodeList) {
        this.attrCodeList = attrCodeList;
    }

    /** {@inheritDoc} */
    @Override
    public int getRank() {
        return rank;
    }

    /** {@inheritDoc} */
    @Override
    public void setRank(final int rank) {
        this.rank = rank;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(final String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, String> getDisplayNames() {
        return displayNames;
    }

    /** {@inheritDoc} */
    @Override
    public void setDisplayNames(final Map<String, String> displayNames) {
        this.displayNames = displayNames;
    }

    /** {@inheritDoc} */
    @Override
    public Instant getCreatedTimestamp() {
        return createdTimestamp;
    }

    /** {@inheritDoc} */
    @Override
    public void setCreatedTimestamp(final Instant createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    /** {@inheritDoc} */
    @Override
    public Instant getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    /** {@inheritDoc} */
    @Override
    public void setUpdatedTimestamp(final Instant updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    /** {@inheritDoc} */
    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    /** {@inheritDoc} */
    @Override
    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    /** {@inheritDoc} */
    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    /** {@inheritDoc} */
    @Override
    public void setUpdatedBy(final String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "ProdTypeAttributeViewGroupDTOImpl{" +
                "prodTypeAttributeViewGroupId=" + prodTypeAttributeViewGroupId +
                ", producttypeId=" + producttypeId +
                ", attrCodeList='" + attrCodeList + '\'' +
                ", rank=" + rank +
                ", name='" + name + '\'' +
                '}';
    }
}
