package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "review", uniqueConstraints = @UniqueConstraint(columnNames = {"movie_id", "account_id"}))
@Where(clause = "is_deleted = false") // Tự động lọc bỏ bản ghi bị xóa mềm
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(name = "is_approved")
    private Boolean approved = false;

    @Column(name = "spoiler_alert")
    private Boolean spoilerAlert = false;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false; // Cột xóa mềm

    @PrePersist
    public void handleBeforeCreate() {
        this.reviewDate = LocalDateTime.now();
    }
}