package com.example.poadevice.resources;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.util.Map;
import com.example.poadevice.domain.OnboardingService;
import com.example.poadevice.domain.Poa;
import com.example.poadevice.exceptions.BadGatewayException;
import com.example.poadevice.exceptions.InternalServerErrorException;
import com.example.poadevice.exceptions.UnauthorizedException;
import com.example.poadevice.repositories.PoaRepository;
import com.example.poadevice.security.KeyService;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/device")
public class Controller {

    @Autowired
    private PoaRepository poaRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KeyService keyService;

    @Autowired
    OnboardingService onboardingService;

    @Value("${server.ssl.key-store-password}")
    private String KEY_STORE_PASSWORD;

    @Value("${subcontractor-poa-uri}")
    private String SUBCONTRACTOR_POA_URI;

    @Value("${device-name}")
    private String DEVICE_NAME;

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

        final String publicKey = keyService.readPublicKeyAsString();
        final Map<String, String> requestBody = Map.of(
                "name", DEVICE_NAME,
                "publicKey", publicKey);

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

        final String ahCertificate = onboardingService.requestAhCertificate(poa);
        
        try {
            removeMe(ahCertificate);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerErrorException("GAH!");
        }

        return "OK";
    }

    private void removeMe(String certificate) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        System.out.println(certificate);
        certificate = "-----BEGIN CERTIFICATE-----\n" + certificate + "\n-----END CERTIFICATE-----";

        // Get the certificate
        StringReader certificateReader = new StringReader(certificate);
        PEMParser certificateParser = new PEMParser(certificateReader);

        X509CertificateHolder certificateHolder = (X509CertificateHolder) certificateParser.readObject();
        java.security.cert.Certificate X509Certificate =
            new JcaX509CertificateConverter()
                .setProvider(new BouncyCastleProvider())
                .getCertificate(certificateHolder);

        certificateParser.close();
        certificateReader.close();

        // Put them into a PKCS12 keystore and write it to a byte[]
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        PrivateKey key = keyService.readPrivateKey();
        keyStore.load(null);
        // TODO: Use real device name instead of hardcoded value
        keyStore.setKeyEntry("poadevice", key, KEY_STORE_PASSWORD.toCharArray(),
            new java.security.cert.Certificate[]{X509Certificate});
        keyStore.store(outputStream, KEY_STORE_PASSWORD.toCharArray());
        outputStream.close();

        FileOutputStream fileOutputStream = new FileOutputStream("/home/nicnys/Projects/arrowhead/sos-examples-spring/demo-car/demo-car-provider/src/main/resources/certificates/locationprovider.p12");
        keyStore.store(fileOutputStream, KEY_STORE_PASSWORD.toCharArray());
        fileOutputStream.close();

        // return bos.toByteArray();
    }
}
