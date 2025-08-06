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

    /**
     * Delete all notifications for a specific account within a date range.
     *
     * @param accountId ID of the account.
     * @param from      Start time.
     * @param to        End time.
     * @return Number of notifications deleted.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.account.accountId = :accountId AND n.createdAt >= :from AND n.createdAt <= :to")
    int deleteAllByAccountIdAndCreatedAtBetween(@Param("accountId") Long accountId,
                                                @Param("from") LocalDateTime from,
                                                @Param("to") LocalDateTime to);

    /**
     * Delete all notifications for a specific account after a certain date.
     *
     * @param accountId ID of the account.
     * @param from      Start time.
     * @return Number of notifications deleted.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.account.accountId = :accountId AND n.createdAt >= :from")
    int deleteAllByAccountIdAndCreatedAtAfter(@Param("accountId") Long accountId,
                                              @Param("from") LocalDateTime from);

    /*
     * * Delete all notifications for a specific account before a certain date.
     *
     * @param accountId ID of the account.
     *
     * @param to End time.
     *
     * @return Number of notifications deleted.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.account.accountId = :accountId AND n.createdAt <= :to")
    int deleteAllByAccountIdAndCreatedAtBefore(@Param("accountId") Long accountId,
                                               @Param("to") LocalDateTime to);

    /**
     * Delete all notifications for a specific account.
     *
     * @param accountId ID of the account.
     * @return Number of notifications deleted.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.account.accountId = :accountId")
    int deleteAllByAccountId(@Param("accountId") Long accountId);
}
