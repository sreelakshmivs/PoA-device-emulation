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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;

@Configuration
public class HttpConfig {

//     @Value("${server.ssl.key-store}")
//     private String KEY_STORE;

//     @Value("${server.ssl.trust-store}")
//     private String TRUST_STORE;

//     @Value("${server.ssl.key-password}")
//     private String KEY_PASSWORD;

//     @Value("${server.ssl.key-store-password}")
//     private String KEY_STORE_PASSWORD;

//     @Value("${server.ssl.trust-store-password}")
//     private String TRUST_STORE_PASSWORD;

//     @Bean
//     public RestTemplate restTemplate(RestTemplateBuilder builder) throws Exception {
//         final SSLContext sslContext = SSLContextBuilder
//                 .create()
//                 .loadKeyMaterial(
//                         ResourceUtils.getFile(KEY_STORE),
//                         KEY_STORE_PASSWORD.toCharArray(),
//                         KEY_PASSWORD.toCharArray())
//                 .loadTrustMaterial(
//                         ResourceUtils.getFile(TRUST_STORE),
//                         TRUST_STORE_PASSWORD.toCharArray())
//                 .build();

//         final HttpClient client = HttpClients.custom()
//                 .setSSLContext(sslContext)
//                 .build();

//         return builder
//                 .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(client))
//                 .build();
//     }

    @Bean
public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException, FileNotFoundException, IOException {
    TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

    SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
        // .loadKeyMaterial(
        //         ResourceUtils.getFile(KEY_STORE),
        //         KEY_STORE_PASSWORD.toCharArray(),
        //         KEY_PASSWORD.toCharArray())
        .loadTrustMaterial(null, acceptingTrustStrategy)
        .build();

    SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
    CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .build();
    HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory();

    requestFactory.setHttpClient(httpClient);
    RestTemplate restTemplate = new RestTemplate(requestFactory);
    return restTemplate;
 }

}
