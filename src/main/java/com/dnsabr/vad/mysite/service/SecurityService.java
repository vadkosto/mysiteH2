package com.dnsabr.vad.mysite.service;

import com.dnsabr.vad.mysite.model.Privilege;

import java.util.Collection;

public interface SecurityService {
    String findLoggedInUsername();

    boolean autologin(String username, String password);

    boolean addAuthority(Privilege... privileges);

    boolean addAuthorities(Collection<Privilege> privileges);
}