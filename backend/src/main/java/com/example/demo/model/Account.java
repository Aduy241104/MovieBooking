package com.example.demo.model;

import com.example.demo.utils.SecurityUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "identity_card")
    private String identityCard;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "register_date")
    private LocalDate registerDate;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "update_by")
    private String updateBy;

    @Column(name = "score")
    private Integer score;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "account_status")
    private Integer status = 1;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "social_account_type")
    private String socialAccountType;

    @PrePersist
    public void handleBeforeCreate() {
        this.registerDate = LocalDate.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}
