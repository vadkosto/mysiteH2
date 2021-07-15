package com.dnsabr.vad.mysite.repository;

import com.dnsabr.vad.mysite.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Интерфейс взаимодействия с таблицей user базы данных
 * Используются унаследованные стандартные методы
 */
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);

    User findByEmail(String email);
}