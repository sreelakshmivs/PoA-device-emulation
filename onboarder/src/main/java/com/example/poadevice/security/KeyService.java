package com.example.poadevice.security;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import com.example.poadevice.exceptions.InternalServerErrorException;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;
import com.example.poadevice.security.KeyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

@Service
public class KeyService {

	@Value("${server.ssl.key-store}")
    private Resource KEY_STORE;

    @Value("${server.ssl.key-password}")
    private String KEY_PASSWORD;

    @Value("${server.ssl.key-store-password}")
    private String KEY_STORE_PASSWORD;

    public PrivateKey readPrivateKey() {
        final KeyStore keyStore = readKeyStore();
        try {
            final String alias = keyStore.aliases().nextElement();
            final PrivateKey privateKey =
                    (PrivateKey) keyStore.getKey(alias, KEY_STORE_PASSWORD.toCharArray());
            return privateKey;
        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Failed to read private key");
        }
    }

    public PublicKey readPublicKey() {
        final KeyStore keyStore = readKeyStore();
        try {
            final String alias = keyStore.aliases().nextElement();
            final Certificate certificate = keyStore.getCertificate(alias);
            final PublicKey publicKey = certificate.getPublicKey();
			return publicKey;
        } catch (KeyStoreException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Failed to read public key");
        }  
    }

    public String readPrivateKeyAsString() {
        return toString(readPrivateKey());
    }

    public String readPublicKeyAsString() {
        return toString(readPublicKey());
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
