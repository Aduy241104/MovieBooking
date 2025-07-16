package com.example.demo.repository;

import com.example.demo.model.Account;
import com.example.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByAccountOrderByCreatedAtDesc(Account account);

    List<Notification> findByAccountAndCreatedAtBetweenOrderByCreatedAtDesc(Account account, LocalDateTime from,
            LocalDateTime to);

    List<Notification> findByAccountAndCreatedAtAfterOrderByCreatedAtDesc(Account account, LocalDateTime from);

    List<Notification> findByAccountAndCreatedAtBeforeOrderByCreatedAtDesc(Account account, LocalDateTime to);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.account.accountId = :accountId AND n.createdAt >= :from AND n.createdAt <= :to")
    int deleteAllByAccountIdAndCreatedAtBetween(@Param("accountId") Long accountId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.account.accountId = :accountId AND n.createdAt >= :from")
    int deleteAllByAccountIdAndCreatedAtAfter(@Param("accountId") Long accountId,
            @Param("from") LocalDateTime from);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.account.accountId = :accountId AND n.createdAt <= :to")
    int deleteAllByAccountIdAndCreatedAtBefore(@Param("accountId") Long accountId,
            @Param("to") LocalDateTime to);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.account.accountId = :accountId")
    int deleteAllByAccountId(@Param("accountId") Long accountId);
}
