package com.assignment.api.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthenticatorService {
	@Value("${api.url.auth}")
	private String authApiUrl;

	public String authenticateUser(String loginId, String password) {
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<String> request = new HttpEntity<>("{\"login_id\":\"" + loginId + "\",\"password\":\"" + password + "\"}", headers);

		ResponseEntity<String> response = restTemplate.exchange(authApiUrl, HttpMethod.POST, request, String.class);

		if (response.getStatusCode().is2xxSuccessful()) {
			return response.getBody();
		} else {
			// Handle authentication failure
			return null;
		}
	}
}
