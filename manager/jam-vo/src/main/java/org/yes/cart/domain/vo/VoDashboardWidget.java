/*
 * Copyright 2009 - 2016 Denys Pavlov, Igor Azarnyi
 *
 *    Licensed under the Apache License, Version 2.0 (the 'License');
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an 'AS IS' BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.yes.cart.domain.vo;

/**
 * User: denispavlov
 * Date: 23/09/2016
 * Time: 09:21
 */
public class VoDashboardWidget {

    private String widgetId;
    private Object data;

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(final String widgetId) {
        this.widgetId = widgetId;
    }

    public Object getData() {
        return data;
    }

    public void setData(final Object data) {
        this.data = data;
    }
}