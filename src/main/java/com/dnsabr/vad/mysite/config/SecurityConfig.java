package com.dnsabr.vad.mysite.config;

import com.dnsabr.vad.mysite.handler.MyLogoutSuccessHandler;
//import com.dnsabr.vad.mysite.service.MyUserDetailsService;
//import com.dnsabr.vad.mysite.service.UserDetailsServiceImpl;
import com.dnsabr.vad.mysite.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    MyLogoutSuccessHandler myLogoutSuccessHandler;
    @Autowired
    UserServiceImpl userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers( "/","/index","/registration","/forgotpassword","/h2cons","/h2cons/*").permitAll() //,"*","/*","/*/*","/*/*/*"
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .permitAll()
                .and()
                .logout()
                .logoutSuccessHandler(myLogoutSuccessHandler)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
                .and()
                .headers().frameOptions().disable();
    }

    @Autowired
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder());
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

//    @Bean
//    public MyUserDetailsService userDetailsService() {
//        return new UserDetailsServiceImpl();
//    }

    @Bean/*(name = BeanIds.AUTHENTICATION_MANAGER)*/
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

//    @Bean
//    public AuthenticationManager authenticationManager() {
//        return new AuthenticationManager() {
//            @Override
//            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//                UserDetails user = userDetailsService().loadUserByUsername(authentication.getName());
//                authentication.setAuthenticated(null==user);
//                return authentication;
//            }
//        };
//    }

//    @Bean
//    @Override
//    public UserDetailsService userDetailsService() {
//
//        UserDetails user =
//                User.withUsername("user")
//                        .password(getPasswordEncoder().encode("password"))
//                        .roles("USER")
//                        .build();
//
//        return new InMemoryUserDetailsManager(user);
//    }
//
//    public PasswordEncoder getPasswordEncoder(){
//        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
//    }
}