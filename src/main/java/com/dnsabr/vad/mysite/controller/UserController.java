package com.dnsabr.vad.mysite.controller;

import com.dnsabr.vad.mysite.listener.OnEmailConfirmEvent;
import com.dnsabr.vad.mysite.validator.*;
import lombok.extern.java.Log;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import com.dnsabr.vad.mysite.model.*;
import com.dnsabr.vad.mysite.service.*;


import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@Log
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RegistrationValidator registrationValidator;

    @Autowired
    private EmailValidator emailValidator;

    @Autowired
    private DataValidator dataValidator;

    @Autowired
    private LoginValidator loginValidator;

    @Autowired
    private ForgotValidator forgotValidator;

    @Autowired
    private ChangePasswordValidator changePasswordValidator;

    @Autowired
    private ResetPasswordValidator resetPasswordValidator;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    @Qualifier("messageSource")
    private MessageSource messages;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    Environment env;

    @GetMapping("/registration")
    public String registration(Model model) {
        String ip = getClientIP();
        if (loginAttemptService.isBlocked(ip)) {
            model.addAttribute("error", "Your IP-address is blocked for 2 minutes.");
            return "index";
        }
        try {
            User user = userService.findUser(securityService.findLoggedInUsername());
            if (null==user) {
                user = userService.findUser(request.getUserPrincipal().getName());
            }
            model.addAttribute("userForm", user);
        } catch (NullPointerException np) {
            model.addAttribute("userForm", new User());
        }
        return "registration";
    }

    @PostMapping({"/registration"})
    public String registration(@ModelAttribute("userForm") User userForm, HttpServletRequest request, BindingResult bindingResult, Model model) {
        registrationValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "registration";
        }

        userService.saveNew(userForm);

        if (!userForm.getEmail().isEmpty()) {
            try {
                String appUrl = request.getContextPath();
                eventPublisher.publishEvent(new OnEmailConfirmEvent
                        (userForm, request.getLocale(), appUrl));
            } catch (Exception me) {
                return "registration";
            }
        }

        securityService.autologin(userForm.getUsername(), userForm.getPasswordConfirm());

        return "redirect:/";
    }

    @GetMapping("/login")
    public String login(Model model, String error, String logout) {
        try {
            User user = userService.findUser(securityService.findLoggedInUsername());
            user.setPasswordOld(null);
        } catch (NullPointerException np) {}
        request.getSession().removeAttribute("user");
        String ip = getClientIP();
        if (loginAttemptService.isBlocked(ip)) {
            model.addAttribute("error", "Your IP-address is blocked for 2 minutes.");
            return "index";
        } else if (error != null) {
            model.addAttribute("error", "Your username and password is invalid.");
        }

        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }

        return "login";
    }

    @GetMapping(value = {"/", "/index"})
    public String index(Model model) {
        try {
            String user_str = getUser();
            if (null!=user_str) {
                User user = userService.findUser(user_str);
                model.addAttribute("user", user_str);
                if (user.isConfirmed()) {
                    model.addAttribute("confirmed", "Confirmed");
                    model.addAttribute("email", user.getEmail());
                } else if (null==user.getEmail() || user.getEmail().isEmpty()) {
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
                    } catch (NullPointerException np) {
                    }
                }
                StringBuilder roles_str = new StringBuilder();
                Collection<String> roles = userService.getRolesByUsername(user_str);
                for (String role : roles) {
                    if (roles_str.length() == 0) {
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
                    if (privileges_str.length() == 0) {
                        privileges_str.append(privilege);
                    } else {
                        privileges_str.append(";");
                        privileges_str.append(privilege);
                    }
                }
                model.addAttribute("privileges", privileges_str);
                request.getSession().setAttribute("user", user_str);
            }
        } catch (NullPointerException | LazyInitializationException np) {
            int dsfg=0;
        }
        return "index";
    }

    @GetMapping("/confirm")
    public String confirm(WebRequest request, Model model, @RequestParam("token") String token) {
        Locale locale = request.getLocale();

        VerificationToken verificationToken = userService.getVerificationToken(token);
        if (verificationToken == null) {
            String message = messages.getMessage("auth.message.invalidToken", null, locale);
            model.addAttribute("error", "Invalid token.");
            return "redirect:/login?lang=" + locale.getLanguage();
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            String messageValue = messages.getMessage("auth.message.expired", null, locale);
            model.addAttribute("error", "Your registration token has expired. Please register again.");
            return "redirect:/login?lang=" + locale.getLanguage();
        }

        user.setConfirmed(true);
        userService.change(user);
//        securityService.autologin(user.getUsername(), user.getPassword());
        return "redirect:/";
//        return "redirect:/index?lang=" + request.getLocale().getLanguage();
    }

    @GetMapping("/resend")
    public String resend(Model model) {
        try {
            User user = userService.findUser(securityService.findLoggedInUsername());
            if (null==user) {
                user = userService.findUser(request.getUserPrincipal().getName());
            }
            if (user.getEmail().isEmpty()) {
                return "redirect:/";
            }
            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnEmailConfirmEvent
                    (user, request.getLocale(), appUrl));
        } catch (Exception me) {
            return "redirect:/";
        }

        model.addAttribute("confirm", "true");
        return "redirect:/";
    }

    @GetMapping("/addemail")
    public String addemail(Model model) {
        try {
            User user = userService.findUser(securityService.findLoggedInUsername());
            if (null==user) {
                user = userService.findUser(request.getUserPrincipal().getName());
            }
            model.addAttribute("userForm", user);
        } catch (NullPointerException np) {
            return "index";
        }
        return "addemail";
    }

//    @RequestMapping(value = "/addemail", method = RequestMethod.POST)
    @PostMapping("/addemail")
    public String addemail(@ModelAttribute("userForm") User userForm, HttpServletRequest request, BindingResult bindingResult, Model model) {
        emailValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "addemail";
        }

        try {
            User user = userService.findUser(securityService.findLoggedInUsername());
            if (null==user) {
                user = userService.findUser(request.getUserPrincipal().getName());
            }
            user.setEmail(userForm.getEmail());
            userService.change(user);
            try {
                String appUrl = request.getContextPath();
                eventPublisher.publishEvent(new OnEmailConfirmEvent
                        (user, request.getLocale(), appUrl));
            } catch (Exception me) {
                return "addemail";
            }
        } catch (NullPointerException np) {
            return "addemail";
        }

//        securityService.autologin(userForm.getUsername(), userForm.getPasswordConfirm());

        return "redirect:/";
    }

//    @RequestMapping(value = "/changedata", method = RequestMethod.GET)
    @GetMapping("/changedata")
    public String changedata(Model model) {
        try {
            User user = userService.findUser(securityService.findLoggedInUsername());
            if (null==user) {

                user = userService.findUser(request.getUserPrincipal().getName());
            }
            model.addAttribute("userForm", user);
        } catch (NullPointerException np) {
            return "index";
        }
        return "changedata";
    }

    @PostMapping("/changedata")
//    @RequestMapping(value = "/changedata", method = RequestMethod.POST)
    public String changedata(@ModelAttribute("userForm") User userForm, HttpServletRequest request, BindingResult bindingResult, Model model) {
        dataValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "changedata";
        }

        try {
            User user = userService.findUser(securityService.findLoggedInUsername());
            if (null==user) {
                user = userService.findUser(request.getUserPrincipal().getName());
            }
            user.setUsername(userForm.getUsername());
            request.getSession().setAttribute("user",userForm.getUsername());
            if (!userForm.getEmail().equals(user.getEmail())) {
                user.setConfirmed(false);
                user.setEmail(userForm.getEmail());
            }
            userService.change(user);
            try {
                String appUrl = request.getContextPath();
                eventPublisher.publishEvent(new OnEmailConfirmEvent
                        (user, request.getLocale(), appUrl));
            } catch (Exception me) {
                return "changedata";
            }
        } catch (NullPointerException np) {
            return "changedata";
        }

//        securityService.autologin(userForm.getUsername(), userForm.getPassword());

        return "redirect:/index?user="+ userForm.getUsername();
    }

    @GetMapping("/forgotpassword")
//    @RequestMapping(value = "/forgotpassword", method = RequestMethod.GET)
    public String forgotpassword(Model model) {
        model.addAttribute("forgotForm", new User());
        return "forgotpassword";
    }

    @PostMapping("/forgotpassword")
//    @RequestMapping(value = "/forgotpassword", method = RequestMethod.POST)
    public String forgotpassword(@ModelAttribute("forgotForm") User forgotForm, HttpServletRequest request, BindingResult bindingResult, Model model) {
        forgotValidator.validate(forgotForm, bindingResult);

//        if (user.getEmail().isEmpty()) {
//            bindingResult.rejectValue("username", "NotFound.forgetForm.email");
//        }

        if (bindingResult.hasErrors()) {
            return "forgotpassword";
        }

        try {
            User user = userService.findUser(forgotForm.getUsername());
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            mailSender.send(constructResetTokenEmail(getAppUrl(), request.getLocale(), token, user));
        } catch (NullPointerException np) {
            return "forgotpassword";
        }

        bindingResult.rejectValue("email", "forgetForm.emailsent");
        return "forgotpassword";
    }

    @GetMapping("/changepassword")
//    @RequestMapping(value = "/changepassword", method = RequestMethod.GET)
    public String changepassword(WebRequest request, Model model) {
        Locale locale = request.getLocale();
        User user = userService.findUser(securityService.findLoggedInUsername());
        if (null==user) {
            try {
                user = userService.findUser(request.getUserPrincipal().getName());
            } catch (NullPointerException np) {
                return "redirect:/";
            }
        }
        model.addAttribute("token","0");
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, Arrays.asList(new SimpleGrantedAuthority("READ_PRIVILEGE"),new SimpleGrantedAuthority("CHANGE_PASSWORD_PRIVILEGE")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        user.setPassword("");
        model.addAttribute("userForm", user);
        return "changepassword";
    }

    @PostMapping("/changepassword")
//    @RequestMapping(value = "/changepassword", method = RequestMethod.POST)
    public String changepassword(@ModelAttribute("userForm") User userForm, HttpServletRequest request, BindingResult bindingResult, Model model) {
        changePasswordValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "changepassword";
        }

        User user = userService.findUser(userForm.getUsername());

        if (securityService.autologin(userForm.getUsername(), userForm.getPasswordOld())) {
            user.setPassword(userForm.getPassword());
            userService.save(user);
        } else {
            bindingResult.rejectValue("passwordOld", "NotMatch.loginForm.password");
            return "login?error=bedpassword";
        }
        securityService.autologin(userForm.getUsername(), userForm.getPasswordConfirm());

        return "redirect:/";
    }

    @GetMapping("/resetpassword")
//    @RequestMapping(value = "/resetpassword", method = RequestMethod.GET)
    public String resetpassword(WebRequest request, Model model, @RequestParam("token") String token) {
        Locale locale = request.getLocale();
        PasswordResetToken passwordResetToken = userService.getPasswordResetToken(token);
        if (passwordResetToken == null) {
            String message = messages.getMessage("auth.message.invalidToken", null, locale);
            model.addAttribute("error", "Invalid token.");
            return "redirect:/login?lang=" + locale.getLanguage();
        }

        User user = passwordResetToken.getUser();
        user.setConfirmed(true);
        userService.change(user);

        Calendar cal = Calendar.getInstance();
        if ((passwordResetToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            String messageValue = messages.getMessage("auth.message.expired", null, locale);
            model.addAttribute("error", "Your registration token has expired. Please register again.");
            return "redirect:/login?lang=" + locale.getLanguage();
        }
        model.addAttribute("token","1");
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, Arrays.asList(new SimpleGrantedAuthority("READ_PRIVILEGE"),new SimpleGrantedAuthority("CHANGE_PASSWORD_PRIVILEGE")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        user.setPassword("");
        model.addAttribute("userForm", user);
        return "resetpassword";
    }

    @PostMapping("/resetpassword")
//    @RequestMapping(value = "/resetpassword", method = RequestMethod.POST)
    public String resetpassword(@ModelAttribute("userForm") User userForm, HttpServletRequest request, BindingResult bindingResult, Model model) {
        resetPasswordValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "resetpassword";
        }

        User user = userService.findUser(userForm.getUsername());

        user.setPassword(userForm.getPassword());
        userService.save(user);
        securityService.autologin(userForm.getUsername(), userForm.getPasswordConfirm());

        return "redirect:/";
    }

    @GetMapping("/askforrole")
    public String askforrole(@RequestParam(required = false) String askfor, Model model) {
        try {
            String user_str = getUser();

            if (null!=user_str) {

//                if ("audit".equals(request.getParameter("askfor"))) {
                if ("audit".equals(askfor)) {
                    userService.addRoles(user_str,"ROLE_AUDIT");
                    User user = userService.findUser(user_str);
                    if (!user.isConfirmed()) {
                        model.addAttribute("error","Your email is not confirmed! You will have audotor's rights till logout.");
                    }
//                    userService.getPrivilegesByRole(Collections.singleton("ROLE_AUDIT")).forEach(privilege->securityService.addAuthority(privilege));
//                    securityService.addAuthorities(userService.getPrivilegesByRole(Collections.singleton("ROLE_AUDIT")));
//                } else if ("admin".equals(request.getParameter("askfor"))) {
                } else if ("admin".equals(askfor)) {
                    User user = userService.findUser(user_str);
                    if (user.isConfirmed()) {
                        /**
                         * ToDo с помощью Cockies скрывать ссылку, если уже запросил права админа
                         */
                        mailSender.send(constructEmailForAdminsRights("Ask for ROLE_ADMIN", "Пользователь "+ user_str+" запросил права администратора."));
                        model.addAttribute("message","Request for Admin's privileges sent to administrator.");
                    } else {
                        model.addAttribute("error","Your email is not confirmed! Please provide real email and confirm it.");
                    }
//                    userService.getPrivilegesByRole(Arrays.asList("ROLE_AUDIT","ROLE_ADMIN")).forEach(privilege->securityService.addAuthority(privilege));
//                    securityService.addAuthorities(userService.getPrivilegesByRole(Arrays.asList("ROLE_AUDIT","ROLE_ADMIN")));
                }

                model.addAttribute("user", user_str);
            }


        } catch (NullPointerException | LazyInitializationException np) {
            int dsfg=0;
        }
        return "askforrole";
    }

    private SimpleMailMessage constructResetTokenEmail(String contextPath, Locale locale, String token, User user) {
        String url = contextPath + "/resetpassword?id=" +
                user.getId() + "&token=" + token;
        String message = messages.getMessage("message.resetPassword",
                null, locale);
        return constructEmail("Reset Password", message + " \r\n" + url, user);
    }

    private SimpleMailMessage constructEmail(String subject, String body, User user) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom(((JavaMailSenderImpl)mailSender).getUsername());
        return email;
    }

    private SimpleMailMessage constructEmailForAdminsRights(String subject, String body) {
    SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(env.getProperty("spring.mail.support"));
        email.setFrom(((JavaMailSenderImpl)mailSender).getUsername());
        return email;
    }

    private String getAppUrl() {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    private String getClientIP() {
        RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
        if (null!=attribs) {
            HttpServletRequest request = ((ServletRequestAttributes) attribs).getRequest();
            final String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader == null) {
                return request.getRemoteAddr();
            }
            return xfHeader.split(",")[0];
        }
        return "0.0.0.0.0.0";
    }

    private String getUser() {
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
        return user_str;
    }
}