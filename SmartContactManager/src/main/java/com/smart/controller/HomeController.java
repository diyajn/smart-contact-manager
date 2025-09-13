package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;



@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	@RequestMapping("/")
	public String home(Model m) {
		m.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model m) {
		m.addAttribute("title","About - Smart Contact Manager");
		return "about";
	}
	
	
	@RequestMapping("/signup")
	public String signup(HttpSession session, Model model) {
	    Object msg = session.getAttribute("message");
	    if (msg != null) {
	        model.addAttribute("message", msg);
	        session.removeAttribute("message"); // ✅ clear after showing
	    }
	    model.addAttribute("title","Register - Smart Contact Manager");
		model.addAttribute("user",new User());  //blank fields ko bhejdenge users m
		return "signup";
	}

	
	
	
	
	
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model m, HttpSession session) {
		
		try {
			if(!agreement) {     //agreement check nhi kra
				System.out.println("you have not agreed the terms and conditions");
				throw new Exception("you have not agreed the terms and conditions");  //catch block chl jayega
			}
			
			if(result.hasErrors()) {
				System.out.println("error:"+result.toString());
				m.addAttribute("user",user);
				return "signup";
			}
			
			System.out.println(agreement);
			System.out.println(user  );
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			
			
			userRepository.save(user);
			m.addAttribute("user",new User());  //after saving form khali hojayega
			session.setAttribute("message", new Message("Successfully Registered!!","alert-success"));
			return "signup";
			
		}catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went wrong! "+e.getMessage(),"alert-danger"));
			return "signup";
		}
		
		
	}
	
}
