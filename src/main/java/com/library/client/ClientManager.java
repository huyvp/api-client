package com.library.client;

import com.library.client.config.ConfigRegistry;
import com.library.client.token.TokenFactory;

public class ClientManager {
    private static String defaultRegion;

    public static void init() {
        init(ConfigRegistry.DEFAULT_CONFIG_FILE_NAME);
    }

    public static void init(String configFileName) {
        ConfigRegistry.init(configFileName);
        TokenFactory.init();
        defaultRegion = ConfigRegistry.getDefaultRegion();
    }

    public static void setDefaultRegion(String defaultRegion) {
        ClientManager.defaultRegion = defaultRegion;
    }

    public static String getAuthorizationValue() {
        checkDefaultRegionExists();
        return getAuthorizationValue(defaultRegion);
    }

    public static String getAuthorizationValue(String region) {
        return TokenFactory.getAuthorizationValue(region);
    }

    public static String getAccessToken() {
        checkDefaultRegionExists();
        return getAccessToken(defaultRegion);
    }

    public static String getAccessToken(String region) {
        return TokenFactory.getAccessToken(region);
    }

    public static String getApiBaseUrl() {
        return getApiBaseUrl(defaultRegion);
    }

    public static String getApiBaseUrl(String region) {
        checkDefaultRegionExists();
        return ConfigRegistry.getApiBaseUrl(region);
    }

    private static void checkDefaultRegionExists() {
        if (defaultRegion == null || defaultRegion.isEmpty())
            throw new RuntimeException("You need to set a default region. Use RegionManager.setDefaultRegion(region) or configuration file.");
    }
}
