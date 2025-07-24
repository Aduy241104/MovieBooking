package com.example.demo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.demo.DTO.response.dashboard.UserRegistrationsResponse;
import com.example.demo.model.Role;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailAndStatus(String email, Integer status);

    Account findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndAccountIdNot(String phoneNumber, Long accountId);

    long countByRole_RoleName(String roleName);

    List<Account> findByRole_RoleName(String roleName);

    @Query("""
                SELECT new com.example.demo.DTO.response.dashboard.UserRegistrationsResponse(
                    DATE_TRUNC('month', a.registerDate),
                    COUNT(a.accountId),
                    0L
                )
                FROM Account a
                WHERE a.role.roleId = 3
                  AND a.registerDate >= :fromDate
                  AND a.registerDate < :toDate
                GROUP BY DATE_TRUNC('month', a.registerDate)
                ORDER BY DATE_TRUNC('month', a.registerDate)
            """)
    List<UserRegistrationsResponse> getUserRegistrationsByMonth(@Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // Count users by role and register date before a specific date
    Long countByRoleRoleIdAndRegisterDateBefore(Long roleId, LocalDate beforeDate);

    /**
     * Tìm tài khoản theo ID và khóa dòng đó lại để ghi (sử dụng cho việc cập nhật
     * điểm).
     * Điều này ngăn chặn các giao dịch khác sửa đổi tài khoản cùng một lúc.
     *
     * @param accountId ID của tài khoản.
     * @return Optional chứa tài khoản nếu tìm thấy.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findWithLockingByAccountId(Long accountId);
}
