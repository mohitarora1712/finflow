package com.lpu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	
	@GetMapping("/test")
	public String test(){
	    return "SECURE ENDPOINT";
	}
	
	  
	@GetMapping("/test/whoami")
	public String whoami(
	        @RequestHeader("X-User-Email") String email,
	        @RequestHeader("X-User-Role") String role) {

	    return email + " : " + role;
	}

}
