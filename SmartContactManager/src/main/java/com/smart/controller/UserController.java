package com.smart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@RequestMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title","Dashboard - Smart Contact Manager");
		return "user/dashboard";
	}

}
