package com.assignment.api.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.assignment.api.dto.CustomerRecord;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

@Service
public class CustomerService {

	@Value("${api.url.customerListApiUrl}")
	private String customerListApiUrl;

	public ResponseEntity<String> getCustomerList(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", token);

		RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, null);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.exchange(customerListApiUrl+"?cmd=get_customer_list", HttpMethod.GET, requestEntity, String.class);
		return responseEntity;
	}

	public ResponseEntity<String> deleteCustomer(String token,String uuid) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", token);

		RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, null);
		RestTemplate restTemplate = new RestTemplate();
		String queryparam="?cmd=delete&uuid="+uuid;
		ResponseEntity<String> responseEntity = restTemplate.exchange(customerListApiUrl+queryparam, HttpMethod.POST, requestEntity, String.class);
		return responseEntity;
	}

	public String createCustomer(String token,CustomerRecord customerRecord) {
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("first_name", customerRecord.firstName());
		requestBody.put("last_name", customerRecord.lastName());
		requestBody.put("street", customerRecord.street());
		requestBody.put("address", customerRecord.address());
		requestBody.put("city", customerRecord.city());
		requestBody.put("state", customerRecord.state());
		requestBody.put("email", customerRecord.email());
		requestBody.put("phone", customerRecord.phone());

		HttpClient httpClient = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(customerListApiUrl+"?cmd=create"))
				.header("Authorization",token)
				.header("Content-Type", "application/json")
				.POST(buildJsonFromMap(requestBody))
				.build();

		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println("Response Status Code: " + response.statusCode());
			System.out.println("Response Body: " + response.body());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}


	public LinkedTreeMap<String, String> selectCustomer(String token,String uuid) {
		ResponseEntity<String> response=getCustomerList(token);
		String resp=response.getBody();
		Gson gson=new Gson();
		ArrayList<LinkedTreeMap<String,String>> customerList=gson.fromJson(resp,ArrayList.class);
		Optional<Object> value= customerList.stream()
				.filter(cust->cust.get("uuid").equals(uuid))
				.findFirst()
				.map(cust->cust);

		if(value.isPresent()) {
			LinkedTreeMap<String,String> outfinal=(LinkedTreeMap<String, String>) value.get();
			return outfinal;
		}
		return null; 
	}







	private static HttpRequest.BodyPublisher buildJsonFromMap(Map<String, String> data) {
		var builder = new StringBuilder();
		builder.append("{");
		for (var entry : data.entrySet()) {
			builder.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
		}
		builder.deleteCharAt(builder.length() - 1);
		builder.append("}");
		return HttpRequest.BodyPublishers.ofString(builder.toString());
	}

	public void updateCustomer(String token, CustomerRecord customerRecord) {
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("first_name", customerRecord.firstName());
		requestBody.put("last_name", customerRecord.lastName());
		requestBody.put("street", customerRecord.street());
		requestBody.put("address", customerRecord.address());
		requestBody.put("city", customerRecord.city());
		requestBody.put("state", customerRecord.state());
		requestBody.put("email", customerRecord.email());
		requestBody.put("phone", customerRecord.phone());

		HttpClient httpClient = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(customerListApiUrl+"?cmd=update&uuid="+customerRecord.uuid()))
				.header("Authorization",token)
				.header("Content-Type", "application/json")
				.POST(buildJsonFromMap(requestBody))
				.build();

		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
