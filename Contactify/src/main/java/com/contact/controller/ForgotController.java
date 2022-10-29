package com.contact.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.contact.dao.UserRepository;
import com.contact.entities.User;
import com.contact.service.EmailService;

@Controller
public class ForgotController {

	Random random = new Random(100000);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private BCryptPasswordEncoder bycBCryptPasswordEncoder;

	// email id form open handler

	@RequestMapping("/forgot")
	public String openEmailForm(Model m) {

		m.addAttribute("title", "Email Form");

		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, Model m, HttpSession session) {

		m.addAttribute("title", "Email Verification");

		System.out.println("Email " + email);

		// generating otp of 4 digit

		int otp = random.nextInt(999999);

		System.out.println("OTP " + otp);

		// sending otp to email
		String subject = "OTP from Contactify";
		String message = "" + "<div style='border: 1px solid #e2e2e2; padding: 20px'>" + "<h1>"
				+ "email verification code: " + "<b>" + otp + "</b>" + "</h1>" + "</div>"

		;
		String to = email;

		boolean flag = this.emailService.sendEmail(subject, message, email);

		if (flag) {

			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		} else {

			session.setAttribute("message", "Check your email id");
			return "forgot_email_form";
		}

	}

	// verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session, Model m) {

		m.addAttribute("title", "OTP Verification");

		int myOtp = (int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");

		if (myOtp == otp) {

			// password change form
			User user = this.userRepository.getUserByUserName(email);

			if (user == null) {

				// send error message
				session.setAttribute("message", "User does not exist with this email id");
				return "forgot_email_form";
			} else {

				// change password
				return "password_change_form";
			}
		} else {

			session.setAttribute("message", "You have Entered Wrong OTP");
			return "verify_otp";
		}
	}

	// change password

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session) {

		String email = (String) session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);

		user.setPassword(this.bycBCryptPasswordEncoder.encode(newPassword));

		this.userRepository.save(user);

		return "redirect:/signin?change=password changed successfully";
	}
}
