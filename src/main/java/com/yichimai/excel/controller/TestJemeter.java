package com.yichimai.excel.controller;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jmeter")
public class TestJemeter {

	private static final String ATTRIBUTE_KEY = "word";
	
	@PostMapping("/hello")
	public Object hello(String a,String b,HttpSession session) {
		
		session.setAttribute(ATTRIBUTE_KEY, a+b);
		
		return a+b;
	}
	
	
	@GetMapping("/repeat")
	public Object repeat(HttpSession session) {
		return session.getAttribute(ATTRIBUTE_KEY);//把刚才请求的参数重新返回一次（用来测试不同现成独立维持会话的能力）
	}
	
	
}
