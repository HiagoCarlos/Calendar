package br.com.calendar.domain;

import br.com.calendar.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(length = 200)
    private String name;

    @Column(length = 200)
    private String email;

    @Column(name = "email_confirmed")
    private Boolean emailConfirmed;

    private String avatar;

    @ToString.Exclude
    @Column(length = 8)
    private String otp;

    @Column(name = "otp_expiration")
    private LocalDateTime otpExpiration;

    @ToString.Exclude
    @Column(length = 100)
    private String password;
}
