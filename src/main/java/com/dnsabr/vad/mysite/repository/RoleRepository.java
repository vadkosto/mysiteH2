package com.dnsabr.vad.mysite.repository;

import com.dnsabr.vad.mysite.model.Role;
import com.dnsabr.vad.mysite.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);

    Set<Role> findAllByNameIsIn(Collection<String> names);

    @Override
    void delete(Role privilege);

    Set<Role> findByUsers(User user);
}