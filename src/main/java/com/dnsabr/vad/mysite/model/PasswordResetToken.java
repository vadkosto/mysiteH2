package com.dnsabr.vad.mysite.model;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "passwordResetToken")
public class PasswordResetToken {

    private static final int EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private long id;
    @Column(nullable = false)
    private String token;
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
    private Date expiryDate;

    public PasswordResetToken() {
        super();
    }

    public PasswordResetToken(final String token) {
        super();

        this.token = token;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    public PasswordResetToken(final String token, final User user) {
        super();

        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    public void setExpiryDate() {
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    private Date calculateExpiryDate(final int expiryTimeInMinutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", user=" + user +
                ", expiryDate=" + expiryDate +
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
        PasswordResetToken passwordResetToken = (PasswordResetToken) obj;
        return this.id ==passwordResetToken.id;
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
        PasswordResetToken passwordResetToken = (PasswordResetToken) obj;
        return this.id==passwordResetToken.id && this.token.equals(passwordResetToken.token)
                && this.user.equals(passwordResetToken.user)
                && this.expiryDate.getTime()==passwordResetToken.expiryDate.getTime();
    }
}