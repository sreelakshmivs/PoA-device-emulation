package com.example.poadevice.resources;

import java.util.Map;
import com.example.poadevice.domain.OnboardingService;
import com.example.poadevice.domain.Poa;
import com.example.poadevice.exceptions.BadGatewayException;
import com.example.poadevice.exceptions.UnauthorizedException;
import com.example.poadevice.repositories.PoaRepository;
import com.example.poadevice.security.KeyService;
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
        return "OK";
    }
}
