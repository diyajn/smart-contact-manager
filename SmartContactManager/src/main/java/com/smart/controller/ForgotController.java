package com.smart.controller;

import java.security.Principal;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	
	Random random=new Random(1000);
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	
	// email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

	// send random otp handler
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {

		System.out.println(email);

		// generate random otp (4 digit)
		int otp = random.nextInt(999999);
		System.out.println("OTP:" + otp);

		// send this otp to email
		String subject = "OTP from SCM";
		String message = "" + "<div style='border:1px solid #e2e2e2; padding:20px'>" + "<h1>" + "OTP IS: " + "<b>" + otp
				+ "</b>" + "</h1>" + "</div>";
		String to = email;
		boolean result = emailService.sendEmail(subject, message, to);

		if (result) {
			session.setAttribute("generatedotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		} else {
			session.setAttribute("message", "Check your email id again");
			return "forgot_email_form";
		}
	}

	// verify otp handler
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp") int myotp, HttpSession session) {
		int generatedotp = (int) session.getAttribute("generatedotp");
		String email = (String) session.getAttribute("email");

		if (generatedotp == myotp) {
			System.out.println("right otp");
			User user = userRepository.getUserByEmail(email);
			if (user == null) {
				// send error message
				session.setAttribute("message", "User does not exist with this email!!");
				return "forgot_email_form";
			} else {
				// change the password
				return "password_change_form";
			}
		} else {
			System.out.println("wrong otp");
			session.setAttribute("message", "You have entered wrong otp!!");
			return "verify_otp";
		}
	}

	// change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("password") String password, HttpSession session) {
		String email = (String) session.getAttribute("email");
		User user = userRepository.getUserByEmail(email);

		// set new password
		user.setPassword(bCryptPasswordEncoder.encode(password));
		userRepository.save(user);
		System.out.println("password changed successfully!!");
		return "redirect:/signin?change=password changed successfully!!";
	}
}
