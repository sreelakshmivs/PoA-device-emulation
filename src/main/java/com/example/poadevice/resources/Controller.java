package com.example.poadevice.resources;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Map;
import com.example.poadevice.domain.Poa;
import com.example.poadevice.exceptions.BadGatewayException;
import com.example.poadevice.exceptions.InternalServerErrorException;
import com.example.poadevice.exceptions.UnauthorizedException;
import com.example.poadevice.repositories.PoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/device")
public class Controller {

    @Autowired
    PoaRepository poaRepository;

    @Autowired
    RestTemplate restTemplate;

    @Value("${ah-onboarding-uri}")
    private String AH_ONBOARDING_URI;

    @Value("${subcontractor-poa-uri}")
    private String SUBCONTRACTOR_POA_URI;

    @Value("${device-name}")
    private String DEVICE_NAME;

    @Value("${server.ssl.key-store}")
    private Resource KEY_STORE;

    @Value("${server.ssl.key-password}")
    private String KEY_PASSWORD;

    @Value("${server.ssl.key-store-password}")
    private String KEY_STORE_PASSWORD;

    @GetMapping("/echo")
    public String echo() {
        return "OK";
    }

    /**
     * Prompt this system to fetch a PoA from a subcontractor.
     *
     * @return
     */
    @GetMapping("/fetch-poa")
    public String fetchPoa() {

        final PublicKey publicKey = readPublicKey();
        final Map<String, String> requestBody = Map.of(
                "name", DEVICE_NAME,
                "publicKey", toString(publicKey));

        try {
            final String poa =
                    restTemplate.postForObject(SUBCONTRACTOR_POA_URI, requestBody, String.class);
            poaRepository.write(poa);
            return "PoA successfully retrieved from the subcontractor.";
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadGatewayException(
                    "Failed to retrieve PoA from Arrowhead PoaOnboarding controller");
        }
    }

    /**
     * @return The most recently fetched PoA.
     */
    @GetMapping("/poa")
    public String poa() {
        final Poa poa = poaRepository.readLatest();
        if (poa == null) {
            return null;
        }
        return poa.getPoa();
    }

    @GetMapping("/onboard")
    public String onboard() {
        final String poa = poaRepository.readLatest().getPoa();
        if (poa == null) {
            throw new UnauthorizedException("No power of attorney present");
        }
        final PrivateKey privateKey = readPrivateKey();

        try {
            final Map<String, String> requestBody = Map.of(
                "poa", poa,
                "privateKey", toString(privateKey));
            return "Onboarding result: " + restTemplate.postForObject(AH_ONBOARDING_URI, requestBody, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadGatewayException("Failed to onboard");
        }
    }

    private PrivateKey readPrivateKey() {
        final KeyStore keyStore = readKeyStore();
        try {
            final String alias = keyStore.aliases().nextElement();
            return (PrivateKey) keyStore.getKey(alias, KEY_STORE_PASSWORD.toCharArray());
        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Failed to read private key");
        }
    }

    private PublicKey readPublicKey() {
        final KeyStore keyStore = readKeyStore();
        try {
            final String alias = keyStore.aliases().nextElement();
            final Certificate certificate = keyStore.getCertificate(alias);
            return certificate.getPublicKey();
        } catch (KeyStoreException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Failed to read public key");
        }
    }

    private KeyStore readKeyStore() {
        try {
            final KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(KEY_STORE.getInputStream(), KEY_STORE_PASSWORD.toCharArray());
        return keyStore;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Failed to read key store");
        }
    }

    private String toString(final Key key) {
        final byte[] encodedKey = key.getEncoded();
        return Base64.getEncoder().encodeToString(encodedKey);
    }
}
