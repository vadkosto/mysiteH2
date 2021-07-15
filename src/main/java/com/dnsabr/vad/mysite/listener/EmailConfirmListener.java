package com.dnsabr.vad.mysite.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import com.dnsabr.vad.mysite.model.User;
import com.dnsabr.vad.mysite.model.VerificationToken;
import com.dnsabr.vad.mysite.service.UserServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@Component
public class EmailConfirmListener implements ApplicationListener<OnEmailConfirmEvent> {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    @Qualifier("messageSource")
    private MessageSource messages;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private JavaMailSender mailSender;
 
    @Override
    public void onApplicationEvent(OnEmailConfirmEvent event) {
        this.confirmEmail(event);
    }
 
    private void confirmEmail(OnEmailConfirmEvent event) {
        User user = event.getUser();
        if (user.isConfirmed()) {
            return;
        }
//        List<VerificationToken> oldTokens = userService.getVerificationTokenByUser(user);
        String token = UUID.randomUUID().toString();
        userService.createVerificationToken(user, token);
         
        String recipientAddress = user.getEmail();
        if (recipientAddress.length()>0) {
            String subject = "Email Confirmation";
            String confirmationUrl
                    = event.getAppUrl() + "/confirm?token=" + token;
            String message = messages.getMessage("message.regEmailSucc", null, event.getLocale());

            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(((JavaMailSenderImpl)mailSender).getUsername());
            email.setTo(recipientAddress);
            email.setSubject(subject);
            email.setText(message + " \r\n" + getAppUrl() + confirmationUrl);
//            mailSender.send(email);
            (new Thread(()->mailSender.send(email))).start();
        }
    }

    private String getAppUrl() {
        return "http://" + request.getServerName() + ":" + request.getServerPort() /*+ request.getContextPath()*/;
    }
}