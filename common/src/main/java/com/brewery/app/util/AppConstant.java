package com.brewery.app.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AppConstant {
    public static final String TENANT_ID = "tenantId";
    public static final String CUSTOMER_ID = "customerId";

    public static final String RESILIENCE_ID_MONGO = "mongo";

    public static final String RESILIENCE_ID_BEER_CLIENT = "beer-client";
    public static final String RESILIENCE_ID_INVENTORY_CLIENT = "inventory-client";

    public static final String LZ4_COMPRESSION = "lz4";
}
