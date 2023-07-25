package com.example.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reset_password_token")
public class ResetPasswordToken {
    private static final int EXPIRATION_MINUTE = 24 * 60;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private LocalDateTime expiryTime;

    @ManyToOne
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid")
    private User user;

    public ResetPasswordToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.setExpiryTime(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTE));
    }
}
