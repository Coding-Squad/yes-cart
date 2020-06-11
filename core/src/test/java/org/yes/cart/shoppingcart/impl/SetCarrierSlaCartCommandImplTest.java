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

package org.yes.cart.shoppingcart.impl;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.yes.cart.BaseCoreDBTestCase;
import org.yes.cart.domain.entity.Address;
import org.yes.cart.shoppingcart.AmountCalculationStrategy;
import org.yes.cart.shoppingcart.MutableShoppingCart;
import org.yes.cart.shoppingcart.ShoppingCartCommand;
import org.yes.cart.shoppingcart.ShoppingCartCommandFactory;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 09-May-2011
 * Time: 14:12:54
 */
public class SetCarrierSlaCartCommandImplTest extends BaseCoreDBTestCase {

    private final Mockery context = new JUnit4Mockery();

    @Test
    public void testExecuteNoAddressParams() {
        MutableShoppingCart shoppingCart = new ShoppingCartImpl();
        shoppingCart.initialise(ctx().getBean("amountCalculationStrategy", AmountCalculationStrategy.class));
        final ShoppingCartCommandFactory commands = ctx().getBean("shoppingCartCommandFactory", ShoppingCartCommandFactory.class);

        assertNotNull(shoppingCart.getCarrierSlaId());
        assertTrue(shoppingCart.getCarrierSlaId().isEmpty());
        assertNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());

        commands.execute(shoppingCart, new HashMap<String, Object>() {{
            put(ShoppingCartCommand.CMD_CHANGECURRENCY, "EUR");
            put(ShoppingCartCommand.CMD_CHANGELOCALE, "en");
            put(ShoppingCartCommand.CMD_SETSHOP, "10");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA, "123");
        }});

        assertEquals(123, shoppingCart.getCarrierSlaId().get("").intValue());
        assertNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());
        assertFalse(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
        assertFalse(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
    }

    @Test
    public void testExecuteWithAddressParams() {
        MutableShoppingCart shoppingCart = new ShoppingCartImpl();
        shoppingCart.initialise(ctx().getBean("amountCalculationStrategy", AmountCalculationStrategy.class));
        final ShoppingCartCommandFactory commands = ctx().getBean("shoppingCartCommandFactory", ShoppingCartCommandFactory.class);

        final Address billing = context.mock(Address.class, "billing");
        final Address delivery = context.mock(Address.class, "delivery");

        context.checking(new Expectations() {{
            allowing(billing).getAddressId(); will(returnValue(234L));
            allowing(billing).getCountryCode(); will(returnValue("GB"));
            allowing(billing).getStateCode(); will(returnValue("GB-CAM"));
            allowing(delivery).getAddressId(); will(returnValue(345L));
            allowing(delivery).getCountryCode(); will(returnValue("GB"));
            allowing(delivery).getStateCode(); will(returnValue("GB-CAM"));
        }});

        assertNotNull(shoppingCart.getCarrierSlaId());
        assertTrue(shoppingCart.getCarrierSlaId().isEmpty());
        assertNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());

        commands.execute(shoppingCart, new HashMap<String, Object>() {{
            put(ShoppingCartCommand.CMD_CHANGECURRENCY, "EUR");
            put(ShoppingCartCommand.CMD_CHANGELOCALE, "en");
            put(ShoppingCartCommand.CMD_SETSHOP, "10");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA, "123-Main");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_ADDRESS, billing);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_ADDRESS, delivery);
        }});

        assertEquals(123, shoppingCart.getCarrierSlaId().get("Main").intValue());
        assertEquals(345, shoppingCart.getOrderInfo().getBillingAddressId().intValue());
        assertEquals(345, shoppingCart.getOrderInfo().getDeliveryAddressId().intValue());
        assertFalse(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
        assertFalse(shoppingCart.getOrderInfo().isDeliveryAddressNotRequired());
        assertEquals("GB", shoppingCart.getShoppingContext().getCountryCode());
        assertEquals("GB-CAM", shoppingCart.getShoppingContext().getStateCode());
    }

    @Test
    public void testExecuteWithAddressParamsSeparate() {
        MutableShoppingCart shoppingCart = new ShoppingCartImpl();
        shoppingCart.initialise(ctx().getBean("amountCalculationStrategy", AmountCalculationStrategy.class));
        final ShoppingCartCommandFactory commands = ctx().getBean("shoppingCartCommandFactory", ShoppingCartCommandFactory.class);

        final Address billing = context.mock(Address.class, "billing");
        final Address delivery = context.mock(Address.class, "delivery");

        context.checking(new Expectations() {{
            allowing(billing).getAddressId(); will(returnValue(234L));
            allowing(billing).getCountryCode(); will(returnValue("GB"));
            allowing(billing).getStateCode(); will(returnValue("GB-CAM"));
            allowing(delivery).getAddressId(); will(returnValue(345L));
            allowing(delivery).getCountryCode(); will(returnValue("GB"));
            allowing(delivery).getStateCode(); will(returnValue("GB-CAM"));
        }});

        assertNotNull(shoppingCart.getCarrierSlaId());
        assertTrue(shoppingCart.getCarrierSlaId().isEmpty());
        assertNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());

        commands.execute(shoppingCart, new HashMap<String, Object>() {{
            put(ShoppingCartCommand.CMD_CHANGECURRENCY, "EUR");
            put(ShoppingCartCommand.CMD_CHANGELOCALE, "en");
            put(ShoppingCartCommand.CMD_SETSHOP, "10");
            put(ShoppingCartCommand.CMD_SEPARATEBILLING, "true");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA, "123");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_ADDRESS, billing);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_ADDRESS, delivery);
        }});

        assertEquals(123, shoppingCart.getCarrierSlaId().get("").intValue());
        assertEquals(234, shoppingCart.getOrderInfo().getBillingAddressId().intValue());
        assertEquals(345, shoppingCart.getOrderInfo().getDeliveryAddressId().intValue());
        assertFalse(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
        assertFalse(shoppingCart.getOrderInfo().isDeliveryAddressNotRequired());
        assertEquals("GB", shoppingCart.getShoppingContext().getCountryCode());
        assertEquals("GB-CAM", shoppingCart.getShoppingContext().getStateCode());
    }

    @Test
    public void testExecuteWithAddressParamsBillingNotRequired() {
        MutableShoppingCart shoppingCart = new ShoppingCartImpl();
        shoppingCart.initialise(ctx().getBean("amountCalculationStrategy", AmountCalculationStrategy.class));
        final ShoppingCartCommandFactory commands = ctx().getBean("shoppingCartCommandFactory", ShoppingCartCommandFactory.class);

        final Address billing = context.mock(Address.class, "billing");
        final Address delivery = context.mock(Address.class, "delivery");

        context.checking(new Expectations() {{
            allowing(billing).getAddressId(); will(returnValue(234L));
            allowing(billing).getCountryCode(); will(returnValue("GB"));
            allowing(billing).getStateCode(); will(returnValue("GB-CAM"));
            allowing(delivery).getAddressId(); will(returnValue(345L));
            allowing(delivery).getCountryCode(); will(returnValue("GB"));
            allowing(delivery).getStateCode(); will(returnValue("GB-CAM"));
        }});


        assertNotNull(shoppingCart.getCarrierSlaId());
        assertTrue(shoppingCart.getCarrierSlaId().isEmpty());
        assertNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());

        commands.execute(shoppingCart, new HashMap<String, Object>() {{
            put(ShoppingCartCommand.CMD_CHANGECURRENCY, "EUR");
            put(ShoppingCartCommand.CMD_CHANGELOCALE, "en");
            put(ShoppingCartCommand.CMD_SETSHOP, "10");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA, "123");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_NOT_REQUIRED, Boolean.TRUE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_NOT_REQUIRED, Boolean.FALSE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_ADDRESS, billing);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_ADDRESS, delivery);
        }});

        assertEquals(123, shoppingCart.getCarrierSlaId().get("").intValue());
        assertNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertEquals(345, shoppingCart.getOrderInfo().getDeliveryAddressId().intValue());
        assertTrue(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
        assertFalse(shoppingCart.getOrderInfo().isDeliveryAddressNotRequired());
        assertNull(shoppingCart.getShoppingContext().getCountryCode());
        assertNull(shoppingCart.getShoppingContext().getStateCode());
    }

    @Test
    public void testExecuteWithAddressParamsBillingNotRequiredSeparate() {
        MutableShoppingCart shoppingCart = new ShoppingCartImpl();
        shoppingCart.initialise(ctx().getBean("amountCalculationStrategy", AmountCalculationStrategy.class));
        final ShoppingCartCommandFactory commands = ctx().getBean("shoppingCartCommandFactory", ShoppingCartCommandFactory.class);

        final Address billing = context.mock(Address.class, "billing");
        final Address delivery = context.mock(Address.class, "delivery");

        context.checking(new Expectations() {{
            allowing(billing).getAddressId(); will(returnValue(234L));
            allowing(billing).getCountryCode(); will(returnValue("GB"));
            allowing(billing).getStateCode(); will(returnValue("GB-CAM"));
            allowing(delivery).getAddressId(); will(returnValue(345L));
            allowing(delivery).getCountryCode(); will(returnValue("GB"));
            allowing(delivery).getStateCode(); will(returnValue("GB-CAM"));
        }});

        assertNotNull(shoppingCart.getCarrierSlaId());
        assertTrue(shoppingCart.getCarrierSlaId().isEmpty());
        assertNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());

        commands.execute(shoppingCart, new HashMap<String, Object>() {{
            put(ShoppingCartCommand.CMD_CHANGECURRENCY, "EUR");
            put(ShoppingCartCommand.CMD_CHANGELOCALE, "en");
            put(ShoppingCartCommand.CMD_SETSHOP, "10");
            put(ShoppingCartCommand.CMD_SEPARATEBILLING, "true");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA, "123");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_NOT_REQUIRED, Boolean.TRUE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_NOT_REQUIRED, Boolean.FALSE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_ADDRESS, billing);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_ADDRESS, delivery);
        }});

        assertEquals(123, shoppingCart.getCarrierSlaId().get("").intValue());
        assertNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertEquals(345, shoppingCart.getOrderInfo().getDeliveryAddressId().intValue());
        assertTrue(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
        assertFalse(shoppingCart.getOrderInfo().isDeliveryAddressNotRequired());
        assertNull(shoppingCart.getShoppingContext().getCountryCode());
        assertNull(shoppingCart.getShoppingContext().getStateCode());
    }

    @Test
    public void testExecuteWithAddressParamsShippingNotRequired() {
        MutableShoppingCart shoppingCart = new ShoppingCartImpl();
        shoppingCart.initialise(ctx().getBean("amountCalculationStrategy", AmountCalculationStrategy.class));
        final ShoppingCartCommandFactory commands = ctx().getBean("shoppingCartCommandFactory", ShoppingCartCommandFactory.class);

        final Address billing = context.mock(Address.class, "billing");
        final Address delivery = context.mock(Address.class, "delivery");

        context.checking(new Expectations() {{
            allowing(billing).getAddressId(); will(returnValue(234L));
            allowing(billing).getCountryCode(); will(returnValue("GB"));
            allowing(billing).getStateCode(); will(returnValue("GB-CAM"));
            allowing(delivery).getAddressId(); will(returnValue(345L));
            allowing(delivery).getCountryCode(); will(returnValue("GB"));
            allowing(delivery).getStateCode(); will(returnValue("GB-CAM"));
        }});

        assertNotNull(shoppingCart.getCarrierSlaId());
        assertTrue(shoppingCart.getCarrierSlaId().isEmpty());
        assertNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());

        commands.execute(shoppingCart, new HashMap<String, Object>() {{
            put(ShoppingCartCommand.CMD_CHANGECURRENCY, "EUR");
            put(ShoppingCartCommand.CMD_CHANGELOCALE, "en");
            put(ShoppingCartCommand.CMD_SETSHOP, "10");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA, "123");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_NOT_REQUIRED, Boolean.FALSE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_NOT_REQUIRED, Boolean.TRUE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_ADDRESS, billing);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_ADDRESS, delivery);
        }});

        assertEquals(123, shoppingCart.getCarrierSlaId().get("").intValue());
        assertEquals(345, shoppingCart.getOrderInfo().getBillingAddressId().intValue());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());
        assertFalse(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
        assertTrue(shoppingCart.getOrderInfo().isDeliveryAddressNotRequired());
        assertEquals("GB", shoppingCart.getShoppingContext().getCountryCode());
        assertEquals("GB-CAM", shoppingCart.getShoppingContext().getStateCode());

    }

    @Test
    public void testExecuteWithAddressParamsShippingNotRequiredNoBilling() {
        MutableShoppingCart shoppingCart = new ShoppingCartImpl();
        shoppingCart.initialise(ctx().getBean("amountCalculationStrategy", AmountCalculationStrategy.class));
        final ShoppingCartCommandFactory commands = ctx().getBean("shoppingCartCommandFactory", ShoppingCartCommandFactory.class);

        final Address billing = null;
        final Address delivery = context.mock(Address.class, "delivery");

        context.checking(new Expectations() {{
            allowing(delivery).getAddressId(); will(returnValue(345L));
            allowing(delivery).getCountryCode(); will(returnValue("GB"));
            allowing(delivery).getStateCode(); will(returnValue("GB-CAM"));
        }});

        assertNotNull(shoppingCart.getCarrierSlaId());
        assertTrue(shoppingCart.getCarrierSlaId().isEmpty());
        assertNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());

        commands.execute(shoppingCart, new HashMap<String, Object>() {{
            put(ShoppingCartCommand.CMD_CHANGECURRENCY, "EUR");
            put(ShoppingCartCommand.CMD_CHANGELOCALE, "en");
            put(ShoppingCartCommand.CMD_SETSHOP, "10");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA, "123");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_NOT_REQUIRED, Boolean.FALSE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_NOT_REQUIRED, Boolean.TRUE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_ADDRESS, billing);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_ADDRESS, delivery);
        }});

        assertEquals(123, shoppingCart.getCarrierSlaId().get("").intValue());
        assertEquals(345, shoppingCart.getOrderInfo().getBillingAddressId().intValue());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());
        assertFalse(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
        assertTrue(shoppingCart.getOrderInfo().isDeliveryAddressNotRequired());
        assertEquals("GB", shoppingCart.getShoppingContext().getCountryCode());
        assertEquals("GB-CAM", shoppingCart.getShoppingContext().getStateCode());

    }

    @Test
    public void testExecuteWithAddressParamsShippingNotRequiredSeparate() {
        MutableShoppingCart shoppingCart = new ShoppingCartImpl();
        shoppingCart.initialise(ctx().getBean("amountCalculationStrategy", AmountCalculationStrategy.class));
        final ShoppingCartCommandFactory commands = ctx().getBean("shoppingCartCommandFactory", ShoppingCartCommandFactory.class);

        final Address billing = context.mock(Address.class, "billing");
        final Address delivery = context.mock(Address.class, "delivery");

        context.checking(new Expectations() {{
            allowing(billing).getAddressId(); will(returnValue(234L));
            allowing(billing).getCountryCode(); will(returnValue("GB"));
            allowing(billing).getStateCode(); will(returnValue("GB-CAM"));
            allowing(delivery).getAddressId(); will(returnValue(345L));
            allowing(delivery).getCountryCode(); will(returnValue("GB"));
            allowing(delivery).getStateCode(); will(returnValue("GB-CAM"));
        }});

        assertNotNull(shoppingCart.getCarrierSlaId());
        assertTrue(shoppingCart.getCarrierSlaId().isEmpty());
        assertNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());

        commands.execute(shoppingCart, new HashMap<String, Object>() {{
            put(ShoppingCartCommand.CMD_CHANGECURRENCY, "EUR");
            put(ShoppingCartCommand.CMD_CHANGELOCALE, "en");
            put(ShoppingCartCommand.CMD_SETSHOP, "10");
            put(ShoppingCartCommand.CMD_SEPARATEBILLING, "true");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA, "123");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_NOT_REQUIRED, Boolean.FALSE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_NOT_REQUIRED, Boolean.TRUE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_ADDRESS, billing);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_ADDRESS, delivery);
        }});

        assertEquals(123, shoppingCart.getCarrierSlaId().get("").intValue());
        assertEquals(234, shoppingCart.getOrderInfo().getBillingAddressId().intValue());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());
        assertFalse(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
        assertTrue(shoppingCart.getOrderInfo().isDeliveryAddressNotRequired());
        assertEquals("GB", shoppingCart.getShoppingContext().getCountryCode());
        assertEquals("GB-CAM", shoppingCart.getShoppingContext().getStateCode());

    }

    @Test
    public void testExecuteWithAddressParamsBillingNotRequiredOverwrite() {
        MutableShoppingCart shoppingCart = new ShoppingCartImpl();
        shoppingCart.initialise(ctx().getBean("amountCalculationStrategy", AmountCalculationStrategy.class));
        final ShoppingCartCommandFactory commands = ctx().getBean("shoppingCartCommandFactory", ShoppingCartCommandFactory.class);

        final Address billing = context.mock(Address.class, "billing");
        final Address delivery = context.mock(Address.class, "delivery");

        context.checking(new Expectations() {{
            allowing(billing).getAddressId(); will(returnValue(234L));
            allowing(billing).getCountryCode(); will(returnValue("GB"));
            allowing(billing).getStateCode(); will(returnValue("GB-CAM"));
            allowing(delivery).getAddressId(); will(returnValue(345L));
            allowing(delivery).getCountryCode(); will(returnValue("GB"));
            allowing(delivery).getStateCode(); will(returnValue("GB-CAM"));
        }});

        shoppingCart.getOrderInfo().setBillingAddressId(234L);
        shoppingCart.getOrderInfo().setDeliveryAddressId(345L);

        assertNotNull(shoppingCart.getCarrierSlaId());
        assertTrue(shoppingCart.getCarrierSlaId().isEmpty());
        assertNotNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNotNull(shoppingCart.getOrderInfo().getDeliveryAddressId());

        commands.execute(shoppingCart, new HashMap<String, Object>() {{
            put(ShoppingCartCommand.CMD_CHANGECURRENCY, "EUR");
            put(ShoppingCartCommand.CMD_CHANGELOCALE, "en");
            put(ShoppingCartCommand.CMD_SETSHOP, "10");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA, "123");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_NOT_REQUIRED, Boolean.TRUE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_NOT_REQUIRED, Boolean.FALSE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_ADDRESS, billing);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_ADDRESS, delivery);
        }});

        assertEquals(123, shoppingCart.getCarrierSlaId().get("").intValue());
        assertNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertEquals(345, shoppingCart.getOrderInfo().getDeliveryAddressId().intValue());
        assertTrue(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
        assertFalse(shoppingCart.getOrderInfo().isDeliveryAddressNotRequired());
        assertNull(shoppingCart.getShoppingContext().getCountryCode());
        assertNull(shoppingCart.getShoppingContext().getStateCode());
    }

    @Test
    public void testExecuteWithAddressParamsBillingNotRequiredOverwriteSeparate() {
        MutableShoppingCart shoppingCart = new ShoppingCartImpl();
        shoppingCart.initialise(ctx().getBean("amountCalculationStrategy", AmountCalculationStrategy.class));
        final ShoppingCartCommandFactory commands = ctx().getBean("shoppingCartCommandFactory", ShoppingCartCommandFactory.class);

        final Address billing = context.mock(Address.class, "billing");
        final Address delivery = context.mock(Address.class, "delivery");

        context.checking(new Expectations() {{
            allowing(billing).getAddressId(); will(returnValue(234L));
            allowing(billing).getCountryCode(); will(returnValue("GB"));
            allowing(billing).getStateCode(); will(returnValue("GB-CAM"));
            allowing(delivery).getAddressId(); will(returnValue(345L));
            allowing(delivery).getCountryCode(); will(returnValue("GB"));
            allowing(delivery).getStateCode(); will(returnValue("GB-CAM"));
        }});

        shoppingCart.getOrderInfo().setBillingAddressId(234L);
        shoppingCart.getOrderInfo().setDeliveryAddressId(345L);

        assertNotNull(shoppingCart.getCarrierSlaId());
        assertTrue(shoppingCart.getCarrierSlaId().isEmpty());
        assertNotNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNotNull(shoppingCart.getOrderInfo().getDeliveryAddressId());

        commands.execute(shoppingCart, new HashMap<String, Object>() {{
            put(ShoppingCartCommand.CMD_CHANGECURRENCY, "EUR");
            put(ShoppingCartCommand.CMD_CHANGELOCALE, "en");
            put(ShoppingCartCommand.CMD_SETSHOP, "10");
            put(ShoppingCartCommand.CMD_SEPARATEBILLING, "true");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA, "123");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_NOT_REQUIRED, Boolean.TRUE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_NOT_REQUIRED, Boolean.FALSE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_ADDRESS, billing);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_ADDRESS, delivery);
        }});

        assertEquals(123, shoppingCart.getCarrierSlaId().get("").intValue());
        assertNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertEquals(345, shoppingCart.getOrderInfo().getDeliveryAddressId().intValue());
        assertTrue(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
        assertFalse(shoppingCart.getOrderInfo().isDeliveryAddressNotRequired());
        assertNull(shoppingCart.getShoppingContext().getCountryCode());
        assertNull(shoppingCart.getShoppingContext().getStateCode());
    }

    @Test
    public void testExecuteWithAddressParamsShippingNotRequiredOverwrite() {
        MutableShoppingCart shoppingCart = new ShoppingCartImpl();
        shoppingCart.initialise(ctx().getBean("amountCalculationStrategy", AmountCalculationStrategy.class));
        final ShoppingCartCommandFactory commands = ctx().getBean("shoppingCartCommandFactory", ShoppingCartCommandFactory.class);

        final Address billing = context.mock(Address.class, "billing");
        final Address delivery = context.mock(Address.class, "delivery");

        context.checking(new Expectations() {{
            allowing(billing).getAddressId(); will(returnValue(234L));
            allowing(billing).getCountryCode(); will(returnValue("GB"));
            allowing(billing).getStateCode(); will(returnValue("GB-CAM"));
            allowing(delivery).getAddressId(); will(returnValue(345L));
            allowing(delivery).getCountryCode(); will(returnValue("GB"));
            allowing(delivery).getStateCode(); will(returnValue("GB-CAM"));
        }});

        shoppingCart.getOrderInfo().setBillingAddressId(234L);
        shoppingCart.getOrderInfo().setDeliveryAddressId(345L);

        assertNotNull(shoppingCart.getCarrierSlaId());
        assertTrue(shoppingCart.getCarrierSlaId().isEmpty());
        assertNotNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNotNull(shoppingCart.getOrderInfo().getDeliveryAddressId());

        commands.execute(shoppingCart, new HashMap<String, Object>() {{
            put(ShoppingCartCommand.CMD_CHANGECURRENCY, "EUR");
            put(ShoppingCartCommand.CMD_CHANGELOCALE, "en");
            put(ShoppingCartCommand.CMD_SETSHOP, "10");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA, "123");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_NOT_REQUIRED, Boolean.FALSE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_NOT_REQUIRED, Boolean.TRUE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_ADDRESS, billing);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_ADDRESS, delivery);
        }});

        assertEquals(123, shoppingCart.getCarrierSlaId().get("").intValue());
        assertEquals(345, shoppingCart.getOrderInfo().getBillingAddressId().intValue());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());
        assertFalse(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
        assertTrue(shoppingCart.getOrderInfo().isDeliveryAddressNotRequired());
        assertEquals("GB", shoppingCart.getShoppingContext().getCountryCode());
        assertEquals("GB-CAM", shoppingCart.getShoppingContext().getStateCode());

    }

    @Test
    public void testExecuteWithAddressParamsShippingNotRequiredOverwriteNoBilling() {
        MutableShoppingCart shoppingCart = new ShoppingCartImpl();
        shoppingCart.initialise(ctx().getBean("amountCalculationStrategy", AmountCalculationStrategy.class));
        final ShoppingCartCommandFactory commands = ctx().getBean("shoppingCartCommandFactory", ShoppingCartCommandFactory.class);

        final Address billing = null;
        final Address delivery = context.mock(Address.class, "delivery");

        context.checking(new Expectations() {{
            allowing(delivery).getAddressId(); will(returnValue(345L));
            allowing(delivery).getCountryCode(); will(returnValue("GB"));
            allowing(delivery).getStateCode(); will(returnValue("GB-CAM"));
        }});

        shoppingCart.getOrderInfo().setBillingAddressId(234L);
        shoppingCart.getOrderInfo().setDeliveryAddressId(345L);

        assertNotNull(shoppingCart.getCarrierSlaId());
        assertTrue(shoppingCart.getCarrierSlaId().isEmpty());
        assertNotNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNotNull(shoppingCart.getOrderInfo().getDeliveryAddressId());

        commands.execute(shoppingCart, new HashMap<String, Object>() {{
            put(ShoppingCartCommand.CMD_CHANGECURRENCY, "EUR");
            put(ShoppingCartCommand.CMD_CHANGELOCALE, "en");
            put(ShoppingCartCommand.CMD_SETSHOP, "10");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA, "123");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_NOT_REQUIRED, Boolean.FALSE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_NOT_REQUIRED, Boolean.TRUE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_ADDRESS, billing);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_ADDRESS, delivery);
        }});

        assertEquals(123, shoppingCart.getCarrierSlaId().get("").intValue());
        assertEquals(345, shoppingCart.getOrderInfo().getBillingAddressId().intValue());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());
        assertFalse(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
        assertTrue(shoppingCart.getOrderInfo().isDeliveryAddressNotRequired());
        assertEquals("GB", shoppingCart.getShoppingContext().getCountryCode());
        assertEquals("GB-CAM", shoppingCart.getShoppingContext().getStateCode());

    }

    @Test
    public void testExecuteWithAddressParamsShippingNotRequiredOverwriteSeparate() {
        MutableShoppingCart shoppingCart = new ShoppingCartImpl();
        shoppingCart.initialise(ctx().getBean("amountCalculationStrategy", AmountCalculationStrategy.class));
        final ShoppingCartCommandFactory commands = ctx().getBean("shoppingCartCommandFactory", ShoppingCartCommandFactory.class);

        final Address billing = context.mock(Address.class, "billing");
        final Address delivery = context.mock(Address.class, "delivery");

        context.checking(new Expectations() {{
            allowing(billing).getAddressId(); will(returnValue(234L));
            allowing(billing).getCountryCode(); will(returnValue("GB"));
            allowing(billing).getStateCode(); will(returnValue("GB-CAM"));
            allowing(delivery).getAddressId(); will(returnValue(345L));
            allowing(delivery).getCountryCode(); will(returnValue("GB"));
            allowing(delivery).getStateCode(); will(returnValue("GB-CAM"));
        }});

        shoppingCart.getOrderInfo().setBillingAddressId(234L);
        shoppingCart.getOrderInfo().setDeliveryAddressId(345L);

        assertNotNull(shoppingCart.getCarrierSlaId());
        assertTrue(shoppingCart.getCarrierSlaId().isEmpty());
        assertNotNull(shoppingCart.getOrderInfo().getBillingAddressId());
        assertNotNull(shoppingCart.getOrderInfo().getDeliveryAddressId());

        commands.execute(shoppingCart, new HashMap<String, Object>() {{
            put(ShoppingCartCommand.CMD_CHANGECURRENCY, "EUR");
            put(ShoppingCartCommand.CMD_CHANGELOCALE, "en");
            put(ShoppingCartCommand.CMD_SETSHOP, "10");
            put(ShoppingCartCommand.CMD_SEPARATEBILLING, "true");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA, "123");
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_NOT_REQUIRED, Boolean.FALSE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_NOT_REQUIRED, Boolean.TRUE);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_BILLING_ADDRESS, billing);
            put(ShoppingCartCommand.CMD_SETCARRIERSLA_P_DELIVERY_ADDRESS, delivery);
        }});

        assertEquals(123, shoppingCart.getCarrierSlaId().get("").intValue());
        assertEquals(234, shoppingCart.getOrderInfo().getBillingAddressId().intValue());
        assertNull(shoppingCart.getOrderInfo().getDeliveryAddressId());
        assertFalse(shoppingCart.getOrderInfo().isBillingAddressNotRequired());
        assertTrue(shoppingCart.getOrderInfo().isDeliveryAddressNotRequired());
        assertNull(shoppingCart.getShoppingContext().getCountryCode());
        assertNull(shoppingCart.getShoppingContext().getStateCode());

    }
}
