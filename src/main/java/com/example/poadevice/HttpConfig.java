package com.example.poadevice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.ssl.SSLContextBuilder;

@Configuration
public class HttpConfig {

    @Value("${server.ssl.key-store}")
    private String KEY_STORE;

    @Value("${server.ssl.key-password}")
    private String KEY_PASSWORD;

    @Value("${server.ssl.key-store-password}")
    private String KEY_STORE_PASSWORD;

    @Bean
    public RestTemplate httpRestTemplate()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
            UnrecoverableKeyException, CertificateException, FileNotFoundException, IOException {
        final TrustStrategy acceptingTrustStrategy =
                (X509Certificate[] chain, String authType) -> true;
        final SSLContext sslContext = SSLContextBuilder
                .create()
                .loadKeyMaterial(
                        ResourceUtils.getFile(KEY_STORE),
                        KEY_STORE_PASSWORD.toCharArray(),
                        KEY_PASSWORD.toCharArray())
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        final SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        final CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();
        final HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        return new RestTemplate(requestFactory);
    }

}
