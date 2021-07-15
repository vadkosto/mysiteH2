package com.dnsabr.vad.mysite.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.format.FormatterRegistry;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.config.annotation.*;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Properties;

@Configuration
@EnableWebMvc
@ComponentScan
public class SpringWebConfig implements WebMvcConfigurer {

    @Autowired
    private Environment env;

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/bank/open")/*.setViewName("open")*/;
        registry.addViewController("/bank/transfer");
        registry.addViewController("/bank/buy");
        registry.addViewController("/bank/loan");
        registry.addViewController("/bank/deposit");
        registry.addViewController("/bank/history");
        registry.addViewController("/bank/cart");
    }

     @Bean
     public JavaMailSender javaMailSender() {
         JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
         javaMailSender.setHost(env.getProperty("spring.mail.host"));
         javaMailSender.setPort(Integer.parseInt(env.getProperty("spring.mail.port")));
         javaMailSender.setProtocol(env.getProperty("spring.mail.protocol"));
         javaMailSender.setUsername(env.getProperty("spring.mail.username"));
         javaMailSender.setPassword(env.getProperty("spring.mail.password"));
         Properties properties = javaMailSender.getJavaMailProperties();
         properties.setProperty("mail.smtp.auth", env.getProperty("spring.mail.properties.mail.smtp.auth"));
         properties.setProperty("mail.smtp.starttls.enable", env.getProperty("spring.mail.properties.mail.smtp.starttls.enable"));
         properties.setProperty("mail.debug", "false");
         return javaMailSender;
     }

     @Bean
     RequestContextListener requestContextListener() {
         return new RequestContextListener();
     }

//    private ApplicationContext applicationContext;
//
//    public SpringWebConfig() {
//    }
//
//    public void setApplicationContext(final ApplicationContext applicationContext)
//            throws BeansException {
//        this.applicationContext = applicationContext;
//    }
//
// *******************************************************************
//
//  GENERAL CONFIGURATION ARTIFACTS
//
//  Static Resources, i18n Messages, Formatters (Conversion Service)
//
// *******************************************************************
//
//
//    @Override
//    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
////        super.addResourceHandlers(registry);
//        registry.addResourceHandler("/images/**").addResourceLocations("/images/");
//        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
//        registry.addResourceHandler("/js/**").addResourceLocations("/js/");
//    }
//
//    @Bean
//    public ResourceBundleMessageSource messageSource() {
//        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
//        messageSource.setBasename("Messages");
//        return messageSource;
//    }
//
//    @Override
//    public void addFormatters(final FormatterRegistry registry) {
////        super.addFormatters(registry);
//        registry.addFormatter(dateFormatter());
//    }
//
//    @Bean
//    public DateFormatter dateFormatter() {
//        return new DateFormatter();
//    }
//
// ****************************************************************
//
//  THYMELEAF-SPECIFIC ARTIFACTS
//
//  TemplateResolver <- TemplateEngine <- ViewResolver
//
// ****************************************************************
//
//    @Bean
//    public SpringResourceTemplateResolver templateResolver(){
//        // SpringResourceTemplateResolver automatically integrates with Spring's own
//        // resource resolution infrastructure, which is highly recommended.
//        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
//        templateResolver.setApplicationContext(this.applicationContext);
//        templateResolver.setPrefix("/templates/");
//        templateResolver.setSuffix(".html");
//        // HTML is the default value, added here for the sake of clarity.
//        templateResolver.setTemplateMode(TemplateMode.HTML);
//        // Template cache is true by default. Set to false if you want
//        // templates to be automatically updated when modified.
//        templateResolver.setCacheable(true);
//        return templateResolver;
//    }
//
//    @Bean
//    public SpringTemplateEngine templateEngine(){
//        // SpringTemplateEngine automatically applies SpringStandardDialect and
//        // enables Spring's own MessageSource message resolution mechanisms.
//        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
//        templateEngine.setTemplateResolver(templateResolver());
//        // Enabling the SpringEL compiler with Spring 4.2.4 or newer can
//        // speed up execution in most scenarios, but might be incompatible
//        // with specific cases when expressions in one template are reused
//        // across different data types, so this flag is "false" by default
//        // for safer backwards compatibility.
//        templateEngine.setEnableSpringELCompiler(true);
//        return templateEngine;
//    }
//
//    @Bean
//    public ThymeleafViewResolver viewResolver(){
//        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
//        viewResolver.setTemplateEngine(templateEngine());
//        // NOTE 'order' and 'viewNames' are optional
//        viewResolver.setOrder(1);
//        viewResolver.setViewNames(new String[] {".html", ".xhtml"});
//        return viewResolver;
//    }


}
