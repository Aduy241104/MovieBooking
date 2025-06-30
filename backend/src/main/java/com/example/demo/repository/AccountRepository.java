package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.demo.model.Role;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailAndStatus(String email, Integer status);

    Account findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    @Query(value = """
            SELECT
                DATE_TRUNC('month', register_date) AS month,
                COUNT(*) AS new_users,
                SUM(COUNT(*)) OVER (ORDER BY DATE_TRUNC('month', register_date)) AS total_users
            FROM account
            WHERE role_id = 3
                AND register_date >= :fromDate
                AND register_date < :toDate
            GROUP BY month
            ORDER BY month
            """, nativeQuery = true)
    List<Object[]> getUserRegistrationsByMonth(LocalDateTime fromDate, LocalDateTime toDate);
// <<< THÊM PHƯƠNG THỨC NÀY VÀO >>>
    /**
     * Tìm tài khoản theo ID và khóa dòng đó lại để ghi (sử dụng cho việc cập nhật điểm).
     * Điều này ngăn chặn các giao dịch khác sửa đổi tài khoản cùng một lúc.
     * @param accountId ID của tài khoản.
     * @return Optional chứa tài khoản nếu tìm thấy.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findWithLockingByAccountId(Long accountId);
}
