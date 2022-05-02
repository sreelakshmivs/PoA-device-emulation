package com.example.poadevice.resources;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.example.poadevice.domain.OnboardingService;
import com.example.poadevice.domain.Poa;
import com.example.poadevice.exceptions.BadGatewayException;
import com.example.poadevice.exceptions.InternalServerErrorException;
import com.example.poadevice.exceptions.UnauthorizedException;
import com.example.poadevice.repositories.PoaRepository;
import com.example.poadevice.security.KeyService;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
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

    @Value("${location-provider-jar}")
    private String LOCATION_PROVIDER_JAR;

    @Value("${device-certificate-output-file}")
    private String CERTIFICATE_FILE;

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

    @GetMapping("/fetch-certificate")
    public List<String> fetchCertificate() {
        final String poa = poaRepository.readLatest().getPoa();
        if (poa == null) {
            throw new UnauthorizedException("No power of attorney present");
        }

        final List<String> ahCertificates = onboardingService.requestAhCertificates(poa);

        try {
            return saveCertificate(ahCertificates);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Something went wrong");
        }
    }

    @GetMapping("/provide-location")
    public String onboard() throws IOException {

        final Process proc =
                new ProcessBuilder("java", "-Dserver.ssl.key-store=file:" + CERTIFICATE_FILE,
                        "-jar", LOCATION_PROVIDER_JAR).start();
        final OutputStream out = proc.getOutputStream();
        out.flush();
        return "OK";
    }

    // TODO: Return void instead, this is only for the demo
    private List<String> saveCertificate(List<String> certificateChain)
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {

        Certificate[] x509Certificates = new Certificate[certificateChain.size()];
        List<String> certificateNames = new ArrayList<>();

        for (int i = 0; i < certificateChain.size(); i++) {
            final String certificate = certificateChain.get(i);
            final StringReader certificateReader = new StringReader(
                    "-----BEGIN CERTIFICATE-----\n" + certificate + "\n-----END CERTIFICATE-----");
            final PEMParser certificateParser = new PEMParser(certificateReader);

            final X509CertificateHolder certificateHolder =
                    (X509CertificateHolder) certificateParser.readObject();
            final X509Certificate x509Certificate =
                    new JcaX509CertificateConverter()
                            .setProvider(new BouncyCastleProvider())
                            .getCertificate(certificateHolder);

            x509Certificates[i] = x509Certificate;

            // TODO: Remove! ===================================================
            X500Name x500name = certificateHolder.getSubject();
            RDN cn = x500name.getRDNs(BCStyle.CN)[0];
            String name = IETFUtils.valueToString(cn.getFirst().getValue());
            certificateNames.add(name);
            // =================================================================
            certificateParser.close();
            certificateReader.close();
        }

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final KeyStore keyStore = KeyStore.getInstance("PKCS12");
        final PrivateKey key = keyService.readPrivateKey();
        keyStore.load(null);

        keyStore.setKeyEntry(DEVICE_NAME, key, KEY_STORE_PASSWORD.toCharArray(), x509Certificates);
        keyStore.store(outputStream, KEY_STORE_PASSWORD.toCharArray());
        outputStream.close();

        final FileOutputStream fileOutputStream = new FileOutputStream(CERTIFICATE_FILE);
        keyStore.store(fileOutputStream, KEY_STORE_PASSWORD.toCharArray());
        fileOutputStream.close();

        return certificateNames;
    }
}
