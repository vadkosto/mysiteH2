package com.dnsabr.vad.mysite.listener;

import java.util.Locale;

import org.springframework.context.ApplicationEvent;
import com.dnsabr.vad.mysite.model.User;

public class OnEmailConfirmEvent extends ApplicationEvent {

    private String appUrl;
    private Locale locale;
    private User user;

    public OnEmailConfirmEvent(final User user, final Locale locale, final String appUrl) {
        super(user);
        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}