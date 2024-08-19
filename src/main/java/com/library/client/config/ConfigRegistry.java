package com.library.client.config;

import com.library.client.exception.AuthInfoException;
import com.library.client.ultil.CryptorUtil;

import java.io.*;
import java.util.*;

public class ConfigRegistry {
    private static final String VALUE_TRUE = "TRUE";
    private static final String VALUE_KEY_VALUE_DELIMITER = "=";
    // PROPS
    public static final String PROP_REGIONS = "_regions";
    public static final String PROP_DEFAULT_REGION = "_default.region";
    public static final String PROP_ENCRYPTED = ".encrypted";
    public static final String PROP_API_BASE_URL = ".api.base.url";
    public static final String PROP_TOKEN_API_URL = ".token.url";
    public static final String PROP_CLIENT_ID = ".client.id";
    public static final String PROP_CLIENT_SECRET = ".client.secret";
    public static final String PROP_USER_NAME = ".user.name";
    public static final String PROP_USER_PASSWORD = ".user.password";
    public static final String PROP_SCOPE = ".scope";
    private static Properties props;

    private static final List<String> regions = new ArrayList<String>();
    // Map list info
    private static final Map<String, AuthInfo> authInfoMap = new HashMap<String, AuthInfo>();
    private static final Map<String, String> apiBaseUrlMap = new HashMap<String, String>();
    // File name config
    public static final String DEFAULT_CONFIG_FILE_NAME = "ApiClientConfig.properties";
    private static String configFileName = DEFAULT_CONFIG_FILE_NAME;
    private static final String[] encryptFields = {PROP_CLIENT_ID, PROP_USER_PASSWORD, PROP_USER_NAME, PROP_CLIENT_SECRET};
    private static List<String> notEncryptedRegions;

    public static void init(String configFileName) {
        ConfigRegistry.configFileName = configFileName;
        try {
            props = new Properties();
            if (!getNotEncryptedRegions().isEmpty())
                encryptAndSave();

            loadAndDecrypt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDefaultRegion() {
        return props.getProperty(PROP_DEFAULT_REGION);
    }

    public static List<String> getRegions() {
        return regions;
    }

    public static AuthInfo getAuthInfo(String region) {
        return authInfoMap.get(region);
    }

    public static String getApiBaseUrl(String region) {
        return apiBaseUrlMap.get(region);
    }

    /**
     * The function to get all region has encrypted param.
     * The value of {region}.encrypted=FALSE
     *
     * @return List<String> : all region not encrypted
     * @throws IOException
     */
    public static List<String> getNotEncryptedRegions() throws IOException {
        notEncryptedRegions = new ArrayList<>();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(configFileName);
        } finally {
            try {
                if (fileReader != null) fileReader.close();
            } catch (Exception ignored) {
            }
        }
        String[] regions = getPropertyValue(PROP_REGIONS).split(",");
        for (String region : regions) {
            String encrypted = getPropertyValue(region.trim() + PROP_ENCRYPTED);
            if (!encrypted.equalsIgnoreCase(VALUE_TRUE)) {
                notEncryptedRegions.add(region.trim());
            }
        }
        return notEncryptedRegions;
    }

    /**
     * Check all the field in the .properties and encrypt all the values of this one
     * @throws IOException
     */
     private static void encryptAndSave()throws IOException{
        ArrayList<String> contents = new ArrayList<String>();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(configFileName);
            br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    contents.add(line);
                    continue;
                }
                String key = line.substring(0, line.indexOf(VALUE_KEY_VALUE_DELIMITER)).trim();
                if (line.contains(PROP_ENCRYPTED)) {
                    contents.add(key + VALUE_KEY_VALUE_DELIMITER + VALUE_TRUE);
                    continue;
                }
                if (needEncryption(line)) {
                    String value = line.substring(line.indexOf(VALUE_KEY_VALUE_DELIMITER) + 1).trim();
                    contents.add(key + VALUE_KEY_VALUE_DELIMITER + CryptorUtil.encrypt(value));
                    continue;
                }
                contents.add(line);
            }

        } finally {
            try { if (br != null) br.close(); } catch (Exception ignored) {}
            try { if (fr != null) fr.close(); } catch (Exception ignored) {}
        }
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {

            fw = new FileWriter(configFileName);
            bw = new BufferedWriter(fw);
            for (String s : contents) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        } finally {
            try { if (bw != null) bw.close(); } catch (Exception ignored) {}
            try { if (fw != null) fw.close(); } catch (Exception ignored) {}
        }
    }

    private static boolean needEncryption(String value) {
        for (String encryptFieldName : encryptFields) {
            for (String region : notEncryptedRegions) {
                if (value.contains(region + encryptFieldName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getPropertyValue(String key) {
        String value = props.getProperty(key);
        if (key == null || key.trim().isEmpty())
            throw new AuthInfoException("Key " + key + " can not null or empty", null);
        return value;
    }

    private static void loadAndDecrypt() throws IOException {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(configFileName);
            props.load(fileReader);
        } finally {
            try {if (fileReader != null) fileReader.close();} catch (Exception ignored) {}
        }
        decryptPropertyValues();
        makeRegionsInfo();
    }

    private static void decryptPropertyValues(){
        try {
            Enumeration<Object> enu = props.keys();
            while (enu.hasMoreElements()){
                String key = (String) enu.nextElement();
                if (hasEncryptField(key))
                    decryptPropertyValue(key);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static boolean hasEncryptField(String line){
         for (String encryptFieldName: encryptFields){
             if (line.contains(encryptFieldName)){
                 return true;
             }
         }
         return false;
    }
    private static void decryptPropertyValue(String key){
        props.setProperty(key, CryptorUtil.decrypt(props.getProperty(key)));
    }

    private static void makeRegionsInfo() {
        regions.clear();
        Enumeration<Object> keys = props.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (key.contains(PROP_API_BASE_URL)) {
                regions.add(key.substring(0, key.indexOf(".")));
            }
        }

        apiBaseUrlMap.clear();
        authInfoMap.clear();

        for (String region : regions) {
            apiBaseUrlMap.put(region, makeBaseUrl(region));
            authInfoMap.put(region, makeAuthInfo(region));
        }
    }
    private static String makeBaseUrl(String region) {
        return getPropertyValue(region + PROP_API_BASE_URL);
    }
    private static AuthInfo makeAuthInfo(String region) {
        AuthInfo authInfo = new AuthInfo();
        authInfo.setTokenApiUrl(getPropertyValue(region + PROP_TOKEN_API_URL));
        authInfo.setScope(getPropertyValue(region + PROP_SCOPE));
        authInfo.setUserName(getPropertyValue(region + PROP_USER_NAME));
        authInfo.setUserPassword(getPropertyValue(region + PROP_USER_PASSWORD));
        authInfo.setClientId(getPropertyValue(region + PROP_CLIENT_ID));
        authInfo.setClientSecret(getPropertyValue(region + PROP_CLIENT_SECRET));
        return authInfo;
    }
}
