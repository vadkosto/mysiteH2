package com.dnsabr.vad.mysite.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import com.dnsabr.vad.mysite.model.User;
import com.dnsabr.vad.mysite.service.UserServiceImpl;

@Component
public class LoginValidator implements Validator {
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
        if (userService.findUser(user.getUsername()) == null) {
            errors.rejectValue("username", "NotFound.loginForm.user");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty");
        if (user.getPassword().length() < 8 || user.getPassword().length() > 32) {
            errors.rejectValue("password", "Size.userForm.password");
        }
    }
}