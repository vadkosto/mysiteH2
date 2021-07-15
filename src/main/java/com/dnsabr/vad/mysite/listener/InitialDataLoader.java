package com.dnsabr.vad.mysite.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.dnsabr.vad.mysite.model.*;
import com.dnsabr.vad.mysite.repository.*;

import java.util.*;

@Component
public class InitialDataLoader implements
        ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (alreadySetup) {
            return;
        }
        Set<Privilege> starterPrivileges = new HashSet<>();
        starterPrivileges.add(createPrivilegeIfNotFound("STARTER_PRIVILEGE"));
        createRoleIfNotFound("ROLE_USER", starterPrivileges);

        Set<Privilege> auditPrivileges = new HashSet<>(starterPrivileges);
        auditPrivileges.add(createPrivilegeIfNotFound("AUDIT_PRIVILEGE"));
        createRoleIfNotFound("ROLE_AUDIT", auditPrivileges);

        Set<Privilege> adminPrivileges = new HashSet<>(auditPrivileges);
        adminPrivileges.add(createPrivilegeIfNotFound("ADMIN_PRIVILEGE"));
        createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);

        alreadySetup = true;
    }

    @Transactional
    Privilege createPrivilegeIfNotFound(String name) {

        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege(name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    void createRoleIfNotFound(String name, Set<Privilege> privileges) {

        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);
            role.setPrivileges(privileges);
            roleRepository.save(role);
        }
//        return role;
    }
}