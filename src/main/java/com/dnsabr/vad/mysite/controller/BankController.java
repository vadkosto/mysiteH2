package com.dnsabr.vad.mysite.controller;

import com.dnsabr.vad.mysite.model.User;
import com.dnsabr.vad.mysite.service.SecurityService;
import com.dnsabr.vad.mysite.service.UserServiceImpl;
import lombok.extern.java.Log;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@Controller
@Log
public class BankController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/bank")
    public String index(Model model) {
        try {
            String user_str = "";
            try {
                user_str = request.getParameter("user");
            } catch (NullPointerException np) {}
            if (null==user_str || user_str.isEmpty()) {
                user_str = securityService.findLoggedInUsername();
            }
            if (null==user_str || user_str.isEmpty()) {
                try {
                    user_str = request.getUserPrincipal().getName();
                } catch (NullPointerException np) {}
            }
            if (null==user_str) {
                return "redirect:/login";
            }

            User user = userService.findUser(user_str);
            model.addAttribute("user", user_str);
            if (user.isConfirmed()) {
                model.addAttribute("confirmed", "Confirmed");
                model.addAttribute("email", user.getEmail());
            } else if (user.getEmail().isEmpty()) {
                model.addAttribute("confirmed", "Not provided");
                model.addAttribute("link", "addemail");
                model.addAttribute("text", "Add email");
            } else {
                model.addAttribute("confirmed", "Not confirmed");
                model.addAttribute("link", "resend");
                model.addAttribute("text", "Resend confirmation email");
                model.addAttribute("email", user.getEmail());
                try {
                    if (request.getParameter("confirm").equals("true")) {
                        model.addAttribute("confirm", "Confirmation email sent");
                    }
                } catch (NullPointerException np) {}
            }
            StringBuilder roles_str = new StringBuilder();
            Collection<String> roles = userService.getRolesByUsername(user_str);
            for (String role : roles) {
                if (roles_str.length()==0) {
                    roles_str.append(role);
                } else {
                    roles_str.append(";");
                    roles_str.append(role);
                }
            }
            model.addAttribute("roles", roles_str);
            Collection<String> privileges = userService.getPrivilegesByUsername(user_str);
            StringBuilder privileges_str = new StringBuilder();
            for (String privilege : privileges) {
                if (privileges_str.length()==0) {
                    privileges_str.append(privilege);
                } else {
                    privileges_str.append(";");
                    privileges_str.append(privilege);
                }
            }
            model.addAttribute("privileges", privileges_str);
        } catch (NullPointerException | LazyInitializationException np) {
            int dsfg=0;
        }
        return "bank";
    }

}
