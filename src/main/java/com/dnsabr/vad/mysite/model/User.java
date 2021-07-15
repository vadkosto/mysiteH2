package com.dnsabr.vad.mysite.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private long id;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Transient
    private String passwordConfirm;
    @Transient
    private String passwordOld;
    private String email;
    private boolean confirmed;
    private boolean enabled;
    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    public User() {
        super();
        this.enabled = true;
        this.confirmed = false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final List<GrantedAuthority> privileges = new ArrayList<>();
        roles.forEach(role -> privileges.addAll(role.getPrivileges()));
        return privileges;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Возвращает строку с названиями и значениями полей объекта
     * исключая информацию о коллекциях объекта
     * @return строка с названиями и значениями полей объекта
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
//                ", password='" + password + '\'' +
//                ", passwordConfirm='" + passwordConfirm + '\'' +
//                ", passwordOld='" + passwordOld + '\'' +
                ", email='" + email + '\'' +
                ", confirmed=" + confirmed +
                ", enabled=" + enabled +
//                ", roles=" + roles +
                '}';
    }

    /**
     * Возвращает hash-код объекта
     * @return hash-код
     */
    @Override
    public int hashCode() {
        return id != 0 ? Objects.hashCode(id) : 0;
    }

    /**
     * Проверяет на эквивалентность переданный объект с этим объектом
     * @param obj объект для проверки на эквивалентность этому объекту
     * @return {@code true} если ключевое поле переданного объекта эквивалентно такому полю у текущего
     *         {@code false} иначе
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return this.id ==user.id;
    }

    /**
     * Проверяет на эквивалентность переданный объект с этим объектом.
     * @param obj объект для проверки на эквивалентность этому объекту
     * @return {@code true} если у переданного объекта все поля и все поля объектов эквивалентены
     * всем полям и полям всех объектов этого объекта {@code false} иначе
     */
    public boolean deepEquals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return this.id==user.id && this.username.equals(user.username) && this.email.equals(user.email);
    }
}