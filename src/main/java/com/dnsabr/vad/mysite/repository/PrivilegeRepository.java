package com.dnsabr.vad.mysite.repository;

import com.dnsabr.vad.mysite.model.Privilege;
import com.dnsabr.vad.mysite.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Set;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

    Privilege findByName(String name);

    @Override
    void delete(Privilege privilege);

    Collection<Privilege> findAllByRolesIn(Collection<Role> roles);

}