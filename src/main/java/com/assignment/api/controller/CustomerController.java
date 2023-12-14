package com.assignment.api.controller;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import com.assignment.api.dto.CustomerRecord;
import com.assignment.api.dto.LoginDTO;
import com.assignment.api.service.AuthenticatorService;
import com.assignment.api.service.CustomerService;
import com.google.gson.Gson;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping(path = "/")
public class CustomerController {

	@Autowired
	AuthenticatorService authService;
	@Autowired
	CustomerService customerService; 

	Gson gson=new Gson();

	@GetMapping("login")
	public String login(HttpSession session,Model model ) {
		String msg=(String)session.getAttribute("msg");
		if(msg!=null) {
			model.addAttribute("msg",msg);
			session.removeAttribute("msg");
		}
		return "login";
	}
	@GetMapping("logout")
	public String logout(HttpSession session,Model model ) {
		session.removeAttribute("token");

		return "redirect:/login";
	}
	@PostMapping("authenticate")
	public String login(@ModelAttribute LoginDTO loginDto,HttpSession session,Model model ) {

		try {
			String response=authService.authenticateUser(loginDto.login_id(), loginDto.password());
			if(response==null) {
				session.setAttribute("msg","Invalid Login ID/ Password");
				return "redirect:/login";
			}
			Map<String,String> value=gson.fromJson(response, Map.class);
			String token=value.get("access_token");
			session.setAttribute("token", "Bearer "+token);
		}
		catch(Exception e) {
			session.setAttribute("msg","Invalid Login ID/ Password");
			return "redirect:/login";
		}
		return getCustomerList(session,model);
	}

	@GetMapping("/customer-list")
	public String getCustomerList(HttpSession session,Model model) {
		String token = (String) session.getAttribute("token");
		validateAuthorizationHeader(token);
		if (token == null) {
			// Handle the case where the user is not authenticated
			return "redirect:/login";
		}

		ResponseEntity<String> response=customerService.getCustomerList(token);
		String resp=response.getBody();
		ArrayList<Map<String,String>> customerList=gson.fromJson(resp,ArrayList.class);

		model.addAttribute("customerList",customerList);
		// You can return the customer list page or any other page to display the list
		return "customer-list";
	}

	@GetMapping("/delete/{uuid}")
	public String deleteCustomer(@PathVariable String uuid,HttpSession session) {
		String token = (String) session.getAttribute("token");
		ResponseEntity<String> response=customerService.deleteCustomer(token, uuid);
		return "redirect:/customer-list";
	}

	@GetMapping("/update/{uuid}")
	public String updateCustomer(@PathVariable String uuid,HttpSession session,Model model) {
		String token=(String)session.getAttribute("token");
		validateAuthorizationHeader(token);
		if (token == null) {
			// Handle the case where the user is not authenticated
			return "redirect:/login";
		}
		Map<String, String> cusrec=customerService.selectCustomer(token, uuid);
		model.addAttribute("customer",cusrec);
		return "update-customer";
	}

	@GetMapping("/create")
	public String showForm(HttpSession session) {
		String token=(String)session.getAttribute("token");
		validateAuthorizationHeader(token);
		if (token == null) {
			// Handle the case where the user is not authenticated
			return "redirect:/login";
		}
		return "customer_detail";
	}

	@PostMapping("/create-customer")
	public String createCustomer(@ModelAttribute CustomerRecord customerDTO,HttpSession session) {
		String token=(String)session.getAttribute("token");
		validateAuthorizationHeader(token);
		if (token == null) {
			// Handle the case where the user is not authenticated
			return "redirect:/login";
		}
		customerService.createCustomer(token, customerDTO);
		return "redirect:/customer-list";
	}

	@PostMapping("/update-customer")
	public String updateCustomer(@ModelAttribute CustomerRecord customerDTO,HttpSession session) {
		String token=(String)session.getAttribute("token");
		validateAuthorizationHeader(token);
		if (token == null) {
			// Handle the case where the user is not authenticated
			return "redirect:/login";
		}
		try {
		customerService.updateCustomer(token, customerDTO);
		}
		catch(Exception e) {
			return "redirect:/customer-list";
		}
		return "redirect:/customer-list";
	}

	private void validateAuthorizationHeader(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			throw new IllegalArgumentException("Invalid Authorization Header");
		}
	}
}
