package com.dnsabr.vad.mysite.service;

import com.dnsabr.vad.mysite.model.Privilege;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.BeanIds;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;
import org.springframework.stereotype.Service;
import com.dnsabr.vad.mysite.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class SecurityServiceImpl implements SecurityService{

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private HttpServletRequest request;

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

    @Override
    public String findLoggedInUsername() {
        String username = "";
        try {
            username = request.getSession().getAttribute("user").toString();
        } catch (NullPointerException np) {}
        if (null==username || username.isEmpty()) {
            Object userDetails = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userDetails instanceof UserDetails) {
                return ((UserDetails)userDetails).getUsername();
            }
        } else if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof String){
            request.getSession().removeAttribute("user");
            User user = userService.findUser(username);
            user.setPasswordOld(null);
            return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } else {
            return username;
        }
        return null;
    }

    @Override
    public boolean autologin(String username, String password) {
        UserDetails userDetails = userService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());

        authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        if (usernamePasswordAuthenticationToken.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            request.getSession().setAttribute("user",username);
            logger.debug(String.format("Auto login %s successfully!", username));
            return true;
        }
        return false;
    }

    public boolean addAuthority(Privilege... privileges) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(auth.getAuthorities());
        for (Privilege privilege : privileges) {
            updatedAuthorities.add(new SimpleGrantedAuthority(privilege.getName())); //add your role here [e.g., new SimpleGrantedAuthority("ROLE_NEW_ROLE")]
//            updatedAuthorities.add(new SwitchUserGrantedAuthority()); //add your role here [e.g., new SimpleGrantedAuthority("ROLE_NEW_ROLE")]
        }
        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);

        SecurityContextHolder.getContext().setAuthentication(newAuth);
        return true;
    }

    public boolean addAuthorities(Collection<Privilege> privileges) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(auth.getAuthorities());
        for (Privilege privilege : privileges) {
            updatedAuthorities.add(new SimpleGrantedAuthority(privilege.getName())); //add your role here [e.g., new SimpleGrantedAuthority("ROLE_NEW_ROLE")]
//            updatedAuthorities.add(new SwitchUserGrantedAuthority()); //add your role here [e.g., new SimpleGrantedAuthority("ROLE_NEW_ROLE")]
        }
        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);

        SecurityContextHolder.getContext().setAuthentication(newAuth);
        return true;
    }
}