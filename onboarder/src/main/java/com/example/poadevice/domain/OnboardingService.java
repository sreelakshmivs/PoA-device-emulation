package com.example.poadevice.domain;

import java.util.List;
import java.util.Map;
import com.example.poadevice.exceptions.BadGatewayException;
import com.example.poadevice.security.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class OnboardingService {

	@Value("${ah-onboarding-uri}")
	private String AH_ONBOARDING_URI;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private KeyService keyService;

	public List<String> requestAhCertificates(final String poa) {

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		final Map<String, String> keyPair = Map.of(
				"keyAlgorithm", "SHA256WithRSA", // TODO: Should not be hardcoded
				"keyFormat", "PKCS#8", // TODO: Should not be hardcoded
				"publicKey", keyService.readPublicKeyAsString(),
				"privateKey", keyService.readPrivateKeyAsString());

		final Map<String, Object> requestBody = Map.of(
				"poa", poa,
				"keyPair", keyPair);

		try {
			final OnboardingResponse response =
					restTemplate.postForObject(AH_ONBOARDING_URI, requestBody, OnboardingResponse.class);
			print(response);
			return response.getCertificateChain();
		} catch (final HttpClientErrorException e){
			throw new BadGatewayException(e.getResponseBodyAsString());
		}
	}

	private void print(final OnboardingResponse response) {
		System.out.println("Onboarding request succeeded");
		System.out.println("----------------------------");
		System.out.println("Service endpoints:");
		System.out.println("  " + response.getOrchestrationService().getService() + ": " + response.getOrchestrationService().getUri());
		System.out.println("  " + response.getDeviceRegistry().getService() + ": " + response.getDeviceRegistry().getUri());
		System.out.println("  " + response.getSystemRegistry().getService() + ": " + response.getSystemRegistry().getUri());
		System.out.println("  " + response.getServiceRegistry().getService() + ": " + response.getServiceRegistry().getUri());
		System.out.println("Certificate chain:");
		System.out.println("  " + response.getCertificateChain());
	}
}
