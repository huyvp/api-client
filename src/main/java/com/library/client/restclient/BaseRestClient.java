package com.library.client.restclient;

import com.library.client.exception.SSLException;

import javax.net.ssl.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class BaseRestClient implements RestClient {
    private static final HostnameVerifier allHostsValid;
    private static final SSLSocketFactory ignoreSSLSocketFactory;

    static {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            ignoreSSLSocketFactory = sc.getSocketFactory();

            // Create all-trusting host name verifier
            allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

        } catch (Exception e) {
            throw new SSLException("Failed to ignore SSL. " + e.getMessage(), e);
        }
    }

    abstract void setRequestProperty(HttpsURLConnection connection, String authString);

    @Override
    public String sendRequest(String apiUrl, String method, String authString, String requestBody) {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(apiUrl);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(ignoreSSLSocketFactory);
            connection.setHostnameVerifier(allHostsValid);
            connection.setConnectTimeout(DEFAULT_TIMEOUT);
            connection.setReadTimeout(DEFAULT_TIMEOUT);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod(method);
            setRequestProperty(connection, authString);

            OutputStream os = connection.getOutputStream();

            try {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                try {os.close();} catch (Exception ignored) {}
            }

            if (is2xxSuccessful(connection.getResponseCode())) {
                return getResponseConnect(connection.getInputStream());
            } else {
                throw new RuntimeException(
                        "Response Code: " + connection.getResponseCode() +
                                "Error: " + connection.getErrorStream()
                );
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    public static String getResponseConnect(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data from InputStream. " + e.getMessage(), e);
        } finally {
            try {
                if (br != null) br.close();
            } catch (Exception ignored) {
            }
        }

        return sb.toString();
    }

    private boolean is2xxSuccessful(int responseCode) {
        return responseCode >= 200 && responseCode < 300;
    }

}
