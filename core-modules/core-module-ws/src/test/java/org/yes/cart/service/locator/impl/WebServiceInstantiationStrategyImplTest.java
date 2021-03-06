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

package org.yes.cart.service.locator.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 09-May-2011
 * Time: 14:12:54
 */
public class WebServiceInstantiationStrategyImplTest {

    @Test
    public void testGetServiceName() throws Exception {
        WebServiceInstantiationStrategyImpl instantiationStrategy = new WebServiceInstantiationStrategyImpl();
        assertEquals("OrteLookup.asmx",
                instantiationStrategy.getServiceName("http://mathertel.de/AJAXEngine/S02_AJAXCoreSamples/OrteLookup.asmx"));
    }
}
