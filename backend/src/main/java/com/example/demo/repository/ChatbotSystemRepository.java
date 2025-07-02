package com.example.demo.repository;

import com.example.demo.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotSystemRepository extends JpaRepository<Account, Long> {

    // Thông tin liên hệ - lấy thông tin admin
    @Query("SELECT a FROM Account a JOIN a.role r WHERE r.roleName = 'ADMIN' AND a.isDeleted = false")
    List<Account> findAdminContacts();

    // Kiểm tra học sinh/sinh viên theo email
    @Query("SELECT a FROM Account a WHERE LOWER(a.email) LIKE '%@student.%' " +
            "OR LOWER(a.email) LIKE '%@sv.%' " +
            "OR LOWER(a.email) LIKE '%@fpt.edu.vn%' " +
            "AND a.isDeleted = false")
    List<Account> findStudentAccounts();

    // Thống kê số lượng khách hàng
    @Query("SELECT COUNT(a) FROM Account a JOIN a.role r WHERE r.roleName = 'CUSTOMER' AND a.isDeleted = false")
    Long countActiveUsers();

    // Thống kê tổng số booking
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingStatus = 'PAID'")
    Long countSuccessfulBookings();

    // Kiểm tra tài khoản theo số điện thoại (để hỗ trợ)
    @Query("SELECT a FROM Account a WHERE a.phoneNumber = :phone AND a.isDeleted = false")
    Account findByPhone(@Param("phone") String phone);
}
