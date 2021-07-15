package com.dnsabr.vad.mysite.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.dnsabr.vad.mysite.model.*;
import com.dnsabr.vad.mysite.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PrivilegeRepository privilegeRepository;
    @Autowired
    private LoginAttemptService loginAttemptService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private VerificationTokenRepository tokenRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final String ip = getClientIP();
        if (loginAttemptService.isBlocked(ip)) {
            throw new RuntimeException("blocked");
        }

        User user = findUser(username);

        if (null==user) {
            throw new UsernameNotFoundException("User not found!");
        }

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.isEnabled(), true, true, true, user.getAuthorities());
    }

    public String getClientIP() {
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

//    @Override
    @Transactional(readOnly = true)
    public Set<String> getRolesByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (null==user) {
            user = userRepository.findByEmail(username);
        }

        if (null==user) {
            throw new UsernameNotFoundException("User not found!");
        }

        Set<String> roles = new TreeSet<>();
        for (Role role : user.getRoles()) {
            roles.add(role.getName());
        }
        return roles;
    }

    @Transactional(readOnly = true)
    public Set<Role> getRolesByUser(User user) {
        return roleRepository.findByUsers(user);
    }

    @Transactional
    public Collection<Privilege> getPrivilegesByRole(Collection<String> roleNames) {
        Collection<Role> roles = roleRepository.findAllByNameIsIn(roleNames);
        return privilegeRepository.findAllByRolesIn(roles);
    }

//    @Override
    @Transactional(readOnly = true)
    public Set<String> getPrivilegesByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (null==user) {
            user = userRepository.findByEmail(username);
        }

        if (null==user) {
            throw new UsernameNotFoundException("User not found!");
        }

        Set<Role> roles = user.getRoles();
        Set<String> privileges = new TreeSet<>();
        for (Role role : roles) {
            for (Privilege privilege : role.getPrivileges()) {
                privileges.add(privilege.getName());
            }
        }

        return privileges;
    }

//    @Override
    public void save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
//        user.setRoles(Collections.singletonList(roleRepository.findByName("ROLE_USER")));
        userRepository.save(user);
    }

    @Transactional
    public void saveNew(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(roleRepository.findByName("ROLE_USER")));
        userRepository.save(user);
    }

//    @Override
    public void change(User user) {
        userRepository.save(user);
    }

    /**
     * Добавляет роли текущему пользователю
     * @param userName
     * @param roleNames
     * @return (@code true) навсегда (@code false) до завершения сессии
     */
    @Transactional(rollbackFor = Throwable.class)
    public boolean addRoles(String userName, String ... roleNames) {
        securityService.addAuthorities(getPrivilegesByRole(Arrays.asList(roleNames)));
        User user = findByUsername(userName);
        if (user.isConfirmed()) {
            Set<Role> roles = user.getRoles();
            for (String name : roleNames) {
                Role role = roleRepository.findByName(name);
                roles.add(role);
            }
//        roleRepository.saveAll(roles);
            userRepository.save(user);
            return true;
        }
        return false;
    }

//    @Override
    public User findUser(String loginOrEmail) {
        User user = findByUsername(loginOrEmail);
        if (null==user) {
            user = findByEmail(loginOrEmail);
        }
        return user;
    }

//    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

//    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

//    @Override
    public VerificationToken getVerificationToken(String VerificationToken) {
        return tokenRepository.findByToken(VerificationToken);
    }

//    @Override
    public List<VerificationToken> getVerificationTokenByUser(User user) {
        return tokenRepository.findByUser(user);
    }

//    @Override
    public void createVerificationToken(User user, String token) {
        VerificationToken myToken;
        if (getVerificationTokenByUser(user).size()>0) {
            myToken = getVerificationTokenByUser(user).get(0);
            myToken.setToken(token);
            myToken.setExpiryDate();
        } else {
            myToken = new VerificationToken(token, user);
        }
        tokenRepository.save(myToken);
    }

//    @Override
    public PasswordResetToken getPasswordResetToken(String PasswordResetToken) {
        return passwordResetTokenRepository.findByToken(PasswordResetToken);
    }

//    @Override
    public List<PasswordResetToken> getPasswordResetTokenByUser(User user) {
        return passwordResetTokenRepository.findByUser(user);
    }

//    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken myToken;
        if (getPasswordResetTokenByUser(user).size()>0) {
            myToken = getPasswordResetTokenByUser(user).get(0);
            myToken.setToken(token);
            myToken.setExpiryDate();
        } else {
            myToken = new PasswordResetToken(token, user);
        }
        passwordResetTokenRepository.save(myToken);
    }
}
