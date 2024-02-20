package com.carel.backendapp.validation;


import com.carel.backendapp.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="validations_users")
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String code;
    private Instant activation;
    private Instant createdAt;
    private Instant expiresAt;

    @OneToOne(cascade = CascadeType.ALL)
    private User user;
}
