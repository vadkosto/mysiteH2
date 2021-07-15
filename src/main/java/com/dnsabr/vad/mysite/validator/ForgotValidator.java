package com.dnsabr.vad.mysite.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import com.dnsabr.vad.mysite.model.User;
import com.dnsabr.vad.mysite.service.UserServiceImpl;

@Component
public class ForgotValidator implements Validator {
    @Autowired
    private UserServiceImpl userService;

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        User user = (User) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "NotEmpty");
        if (user.getUsername().length() < 6) {
            errors.rejectValue("username", "Size.userForm.username");
        }
        try {
            User user1 = userService.findUser(user.getUsername());
            if (user1.getEmail().isEmpty()) {
                errors.rejectValue("username", "NotFound.forgetForm.email");
            }
        } catch (NullPointerException np) {
            errors.rejectValue("username", "NotFound.loginForm.user");
        }

//        if (userService.findByUsername(user.getUsername()) == null) {
//            if (userService.findByEmail(user.getUsername()) == null) {
//                errors.rejectValue("username", "NotFound.loginForm.user");
//            }
//        }
    }
}