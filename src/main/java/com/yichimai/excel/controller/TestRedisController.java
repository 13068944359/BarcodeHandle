package com.yichimai.excel.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
public class TestRedisController {

	@PostMapping("/hello")
	public Object hello(String a,String b) {
		
		
		
		
		return a+b;
	}
	
}
