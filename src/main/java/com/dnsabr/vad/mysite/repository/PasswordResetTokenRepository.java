package com.dnsabr.vad.mysite.repository;

import com.dnsabr.vad.mysite.model.PasswordResetToken;
import com.dnsabr.vad.mysite.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);

    List<PasswordResetToken> findByUser(User user);
}