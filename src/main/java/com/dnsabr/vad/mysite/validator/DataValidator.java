package com.dnsabr.vad.mysite.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import com.dnsabr.vad.mysite.model.User;
import com.dnsabr.vad.mysite.service.SecurityService;
import com.dnsabr.vad.mysite.service.UserServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DataValidator implements Validator {
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private HttpServletRequest request;

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$";

    @Override
    public void validate(Object o, Errors errors) {
        User user = (User) o;

        try {
            User userBefore = userService.findUser(securityService.findLoggedInUsername());
            if (null==userService) {
                userBefore = userService.findUser(request.getUserPrincipal().getName());
            }

            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "NotEmpty");
            if (user.getUsername().length() < 6 || user.getUsername().length() > 32) {
                errors.rejectValue("username", "Size.userForm.username");
            }
            if (!userBefore.getUsername().equals(user.getUsername())) {
                if (userService.findByUsername(user.getUsername()) != null) {
                    errors.rejectValue("username", "Duplicate.userForm.username");
                }
            }

            if (!user.getEmail().isEmpty()) {
                Pattern pattern = Pattern.compile(EMAIL_PATTERN);
                Matcher matcher = pattern.matcher(user.getEmail());
                if (!matcher.matches()) {
                    errors.rejectValue("email", "Diff.userForm.email");
                }
                if (!userBefore.getEmail().equals(user.getEmail())) {
                    if (userService.findByEmail(user.getEmail()) != null) {
                        errors.rejectValue("email", "Duplicate.userForm.email");
                    }
                }
            }

/*            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty");
            if (user.getPassword().length() < 8 || user.getPassword().length() > 32) {
                errors.rejectValue("password", "Size.userForm.password");
            }*/

        } catch (NullPointerException np) {
//            np.printStackTrace();
            errors.rejectValue("username", "Session.error");
            errors.rejectValue("email", "Session.error");
        }
    }
}
