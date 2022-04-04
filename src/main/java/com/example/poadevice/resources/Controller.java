package com.example.poadevice.resources;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import com.example.poadevice.domain.Poa;
import com.example.poadevice.exceptions.BadGatewayException;
import com.example.poadevice.exceptions.InternalServerErrorException;
import com.example.poadevice.repositories.PoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
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

    @Value("${public-key}")
    private Resource PUBLIC_KEY;

    @GetMapping("/echo")
    public String echo() {
        return "OK";
    }

    @GetMapping("/fetch-poa")
    public String fetchPoa() {

        final Map<String, String> requestBody = Map.of(
                "name", DEVICE_NAME,
                "publicKey", readPublicKey());

        try {
            final String poa = restTemplate.postForObject(SUBCONTRACTOR_POA_URI, requestBody, String.class);
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

    private String readPublicKey() {
        try {
            String publicKey =
                    StreamUtils.copyToString(PUBLIC_KEY.getInputStream(), Charset.defaultCharset());
            publicKey = publicKey.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "");
            return publicKey;
        } catch (IOException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Failed to read public key");
        }
    }
}
