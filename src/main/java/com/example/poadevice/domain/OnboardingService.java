package com.example.poadevice.domain;

import java.util.Map;
import com.example.poadevice.CsrResponse;
import com.example.poadevice.exceptions.BadGatewayException;
import com.example.poadevice.security.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OnboardingService {

	@Value("${ah-onboarding-uri}")
	private String AH_ONBOARDING_URI;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private KeyService keyService;

	public String requestAhCertificate(final String poa) {

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		final Map<String, String> keyPair = Map.of(
				"keyAlgorithm", "SHA256WithRSA",
				"keyFormat", "PKCS#8",
				"publicKey", keyService.readPublicKey(),
				"privateKey", keyService.readPrivateKey());

		final Map<String, Object> requestBody = Map.of(
				"poa", poa,
				"keyPair", keyPair);

		try {
			final CsrResponse csrResponse =
					restTemplate.postForObject(AH_ONBOARDING_URI, requestBody, CsrResponse.class);
			return csrResponse.getCertificateChain().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadGatewayException("Failed to onboard");
		}
	}
}
