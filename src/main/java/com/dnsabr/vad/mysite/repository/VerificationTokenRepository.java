package com.dnsabr.vad.mysite.repository;

import com.dnsabr.vad.mysite.model.User;
import com.dnsabr.vad.mysite.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
 
    VerificationToken findByToken(String token);
 
    List<VerificationToken> findByUser(User user);
}