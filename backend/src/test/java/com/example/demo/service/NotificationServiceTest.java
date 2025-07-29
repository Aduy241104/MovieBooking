package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.Notification;
import com.example.demo.model.Role;
import com.example.demo.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService
 * Testing notification management operations, real-time messaging, and data
 * retrieval
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private NotificationService notificationService;

    private Account testAccount;
    private Notification testNotification;
    private List<Notification> testNotificationList;
    private List<Account> testAccountList;

    @BeforeEach
    void setUp() {
        // Setup test account
        Role testRole = new Role(1L, "CUSTOMER");
        testAccount = Account.builder()
                .accountId(1L)
                .email("test@example.com")
                .fullName("Test User")
                .role(testRole)
                .build();

        // Setup test notification
        testNotification = Notification.builder()
                .id(1L)
                .account(testAccount)
                .title("Test Notification")
                .content("This is a test notification")
                .type("SYSTEM")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Setup test notification list
        testNotificationList = new ArrayList<>();
        testNotificationList.add(testNotification);
        testNotificationList.add(Notification.builder()
                .id(2L)
                .account(testAccount)
                .title("Second Notification")
                .content("This is another test notification")
                .type("MOVIE")
                .isRead(true)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build());

        // Setup test account list
        testAccountList = new ArrayList<>();
        testAccountList.add(testAccount);
        testAccountList.add(Account.builder()
                .accountId(2L)
                .email("user2@example.com")
                .fullName("User Two")
                .role(testRole)
                .build());
    }

    // ========== NOTIFY TESTS ==========

    @Test
    void testNotify_whenValidNotification_shouldSaveAndSendRealtime() {
        // Arrange
        String title = "New Notification";
        String content = "This is a new notification";
        String type = "SYSTEM";

        Notification savedNotification = Notification.builder()
                .id(3L)
                .account(testAccount)
                .title(title)
                .content(content)
                .type(type)
                .isRead(false)
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // Act
        notificationService.notify(testAccount, title, content, type);

        // Assert
        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSend(eq("/queue/notify-1"), any(Notification.class));
    }

    @Test
    void testNotify_whenAccountIsNull_shouldThrowNullPointerException() {
        // Arrange
        String title = "Test Title";
        String content = "Test Content";
        String type = "SYSTEM";

        // Act & Assert
        assertThrows(NullPointerException.class, () -> notificationService.notify(null, title, content, type));
    }

    @Test
    void testNotify_whenTitleIsNull_shouldSaveWithNullTitle() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.notify(testAccount, null, "content", "SYSTEM");

        // Assert
        verify(notificationRepository).save(argThat(notification -> notification.getTitle() == null &&
                notification.getContent().equals("content") &&
                notification.getType().equals("SYSTEM") &&
                notification.getAccount().equals(testAccount) &&
                !notification.getIsRead()));
        verify(messagingTemplate).convertAndSend(eq("/queue/notify-1"), any(Notification.class));
    }

    @Test
    void testNotify_whenContentIsEmpty_shouldSaveWithEmptyContent() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.notify(testAccount, "title", "", "SYSTEM");

        // Assert
        verify(notificationRepository).save(argThat(notification -> notification.getTitle().equals("title") &&
                notification.getContent().equals("") &&
                notification.getType().equals("SYSTEM")));
        verify(messagingTemplate).convertAndSend(eq("/queue/notify-1"), any(Notification.class));
    }

    // ========== SEND NOTIFICATION TO USERS TESTS ==========

    @Test
    void testSendNotificationToUsers_whenValidAccountIds_shouldSendToAllUsers() {
        // Arrange
        List<Long> accountIds = Arrays.asList(1L, 2L);
        String title = "Bulk Notification";
        String content = "This is a bulk notification";
        String type = "MOVIE";

        Account account2 = Account.builder()
                .accountId(2L)
                .email("user2@example.com")
                .fullName("User Two")
                .build();

        when(accountService.fetchAccountById(1L)).thenReturn(testAccount);
        when(accountService.fetchAccountById(2L)).thenReturn(account2);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.sendNotificationToUsers(accountIds, title, content, type);

        // Assert
        verify(accountService).fetchAccountById(1L);
        verify(accountService).fetchAccountById(2L);
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Notification.class));
    }

    @Test
    void testSendNotificationToUsers_whenSomeAccountsNotFound_shouldSkipInvalidAccounts() {
        // Arrange
        List<Long> accountIds = Arrays.asList(1L, 999L, 2L);
        String title = "Bulk Notification";
        String content = "This is a bulk notification";
        String type = "SYSTEM";

        when(accountService.fetchAccountById(1L)).thenReturn(testAccount);
        when(accountService.fetchAccountById(999L)).thenReturn(null);
        when(accountService.fetchAccountById(2L)).thenReturn(testAccountList.get(1));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.sendNotificationToUsers(accountIds, title, content, type);

        // Assert
        verify(accountService).fetchAccountById(1L);
        verify(accountService).fetchAccountById(999L);
        verify(accountService).fetchAccountById(2L);
        verify(notificationRepository, times(2)).save(any(Notification.class)); // Only 2 valid accounts
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Notification.class));
    }

    @Test
    void testSendNotificationToUsers_whenEmptyAccountIdsList_shouldNotSendAnyNotifications() {
        // Arrange
        List<Long> emptyAccountIds = new ArrayList<>();
        String title = "Test";
        String content = "Test";
        String type = "SYSTEM";

        // Act
        notificationService.sendNotificationToUsers(emptyAccountIds, title, content, type);

        // Assert
        verify(accountService, never()).fetchAccountById(anyLong());
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Notification.class));
    }

    @Test
    void testSendNotificationToUsers_whenNullAccountIdsList_shouldHandleGracefully() {
        // Arrange
        String title = "Test";
        String content = "Test";
        String type = "SYSTEM";

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> notificationService.sendNotificationToUsers(null, title, content, type));
    }

    // ========== SEND NOTIFICATION TO ALL USERS TESTS ==========

    @Test
    void testSendNotificationToAllUsers_whenUsersExist_shouldSendToAllUsers() {
        // Arrange
        String title = "Global Notification";
        String content = "This is a global notification";
        String type = "SYSTEM";

        when(accountService.getAllAccount()).thenReturn(testAccountList);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.sendNotificationToAllUsers(title, content, type);

        // Assert
        verify(accountService).getAllAccount();
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Notification.class));
    }

    @Test
    void testSendNotificationToAllUsers_whenNoUsersExist_shouldNotSendAnyNotifications() {
        // Arrange
        String title = "Global Notification";
        String content = "This is a global notification";
        String type = "SYSTEM";

        when(accountService.getAllAccount()).thenReturn(new ArrayList<>());

        // Act
        notificationService.sendNotificationToAllUsers(title, content, type);

        // Assert
        verify(accountService).getAllAccount();
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Notification.class));
    }

    @Test
    void testSendNotificationToAllUsers_whenAccountServiceReturnsNull_shouldHandleGracefully() {
        // Arrange
        String title = "Global Notification";
        String content = "This is a global notification";
        String type = "SYSTEM";

        when(accountService.getAllAccount()).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> notificationService.sendNotificationToAllUsers(title, content, type));
        verify(accountService).getAllAccount();
    }

    // ========== GET USER NOTIFICATIONS TESTS ==========

    @Test
    void testGetUserNotifications_whenNotificationsExist_shouldReturnNotifications() {
        // Arrange
        when(notificationRepository.findByAccountOrderByCreatedAtDesc(testAccount))
                .thenReturn(testNotificationList);

        // Act
        List<Notification> result = notificationService.getUserNotifications(testAccount);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Notification", result.get(0).getTitle());
        assertEquals("Second Notification", result.get(1).getTitle());
        verify(notificationRepository).findByAccountOrderByCreatedAtDesc(testAccount);
    }

    @Test
    void testGetUserNotifications_whenNoNotificationsExist_shouldReturnEmptyList() {
        // Arrange
        when(notificationRepository.findByAccountOrderByCreatedAtDesc(testAccount))
                .thenReturn(new ArrayList<>());

        // Act
        List<Notification> result = notificationService.getUserNotifications(testAccount);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(notificationRepository).findByAccountOrderByCreatedAtDesc(testAccount);
    }

    @Test
    void testGetUserNotifications_whenAccountIsNull_shouldCallRepository() {
        // Arrange
        when(notificationRepository.findByAccountOrderByCreatedAtDesc(null))
                .thenReturn(new ArrayList<>());

        // Act
        List<Notification> result = notificationService.getUserNotifications(null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(notificationRepository).findByAccountOrderByCreatedAtDesc(null);
    }

    // ========== GET USER NOTIFICATIONS BY TIME TESTS ==========

    @Test
    void testGetUserNotificationsByTime_whenBothFromAndToAreNull_shouldReturnAllNotifications() {
        // Arrange
        when(notificationRepository.findByAccountOrderByCreatedAtDesc(testAccount))
                .thenReturn(testNotificationList);

        // Act
        List<Notification> result = notificationService.getUserNotificationsByTime(testAccount, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(notificationRepository).findByAccountOrderByCreatedAtDesc(testAccount);
        verify(notificationRepository, never()).findByAccountAndCreatedAtBetweenOrderByCreatedAtDesc(any(), any(),
                any());
    }

    @Test
    void testGetUserNotificationsByTime_whenBothFromAndToProvided_shouldReturnNotificationsBetween() {
        // Arrange
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();

        when(notificationRepository.findByAccountAndCreatedAtBetweenOrderByCreatedAtDesc(testAccount, from, to))
                .thenReturn(testNotificationList);

        // Act
        List<Notification> result = notificationService.getUserNotificationsByTime(testAccount, from, to);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(notificationRepository).findByAccountAndCreatedAtBetweenOrderByCreatedAtDesc(testAccount, from, to);
    }

    @Test
    void testGetUserNotificationsByTime_whenOnlyFromProvided_shouldReturnNotificationsAfter() {
        // Arrange
        LocalDateTime from = LocalDateTime.now().minusDays(1);

        when(notificationRepository.findByAccountAndCreatedAtAfterOrderByCreatedAtDesc(testAccount, from))
                .thenReturn(testNotificationList);

        // Act
        List<Notification> result = notificationService.getUserNotificationsByTime(testAccount, from, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(notificationRepository).findByAccountAndCreatedAtAfterOrderByCreatedAtDesc(testAccount, from);
    }

    @Test
    void testGetUserNotificationsByTime_whenOnlyToProvided_shouldReturnNotificationsBefore() {
        // Arrange
        LocalDateTime to = LocalDateTime.now();

        when(notificationRepository.findByAccountAndCreatedAtBeforeOrderByCreatedAtDesc(testAccount, to))
                .thenReturn(testNotificationList);

        // Act
        List<Notification> result = notificationService.getUserNotificationsByTime(testAccount, null, to);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(notificationRepository).findByAccountAndCreatedAtBeforeOrderByCreatedAtDesc(testAccount, to);
    }

    // ========== MARK AS READ TESTS ==========

    @Test
    void testMarkAsRead_whenNotificationExists_shouldMarkAsRead() {
        // Arrange
        Long notificationId = 1L;
        Notification notification = Notification.builder()
                .id(notificationId)
                .isRead(false)
                .build();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.markAsRead(notificationId);

        // Assert
        assertTrue(notification.getIsRead());
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(notification);
    }

    @Test
    void testMarkAsRead_whenNotificationNotExists_shouldNotThrowException() {
        // Arrange
        Long notificationId = 999L;
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // Act
        notificationService.markAsRead(notificationId);

        // Assert
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarkAsRead_whenNotificationAlreadyRead_shouldStillSave() {
        // Arrange
        Long notificationId = 1L;
        Notification notification = Notification.builder()
                .id(notificationId)
                .isRead(true)
                .build();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.markAsRead(notificationId);

        // Assert
        assertTrue(notification.getIsRead());
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(notification);
    }

    // ========== DELETE NOTIFICATION TESTS ==========

    @Test
    void testDeleteNotification_whenValidId_shouldDeleteNotification() {
        // Arrange
        Long notificationId = 1L;

        // Act
        notificationService.deleteNotification(notificationId);

        // Assert
        verify(notificationRepository).deleteById(notificationId);
    }

    @Test
    void testDeleteNotification_whenNullId_shouldPassToRepository() {
        // Act
        notificationService.deleteNotification(null);

        // Assert
        verify(notificationRepository).deleteById(null);
    }

    // ========== DELETE NOTIFICATIONS BY TIME TESTS ==========

    @Test
    void testDeleteNotificationsByTime_whenBothFromAndToProvided_shouldDeleteBetween() {
        // Arrange
        LocalDateTime from = LocalDateTime.now().minusDays(2);
        LocalDateTime to = LocalDateTime.now().minusDays(1);
        when(notificationRepository.deleteAllByAccountIdAndCreatedAtBetween(1L, from, to)).thenReturn(5);

        // Act
        notificationService.deleteNotificationsByTime(testAccount, from, to);

        // Assert
        verify(notificationRepository).deleteAllByAccountIdAndCreatedAtBetween(1L, from, to);
        verify(notificationRepository, never()).deleteAllByAccountIdAndCreatedAtAfter(anyLong(), any());
        verify(notificationRepository, never()).deleteAllByAccountIdAndCreatedAtBefore(anyLong(), any());
        verify(notificationRepository, never()).deleteAllByAccountId(anyLong());
    }

    @Test
    void testDeleteNotificationsByTime_whenOnlyFromProvided_shouldDeleteAfter() {
        // Arrange
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        when(notificationRepository.deleteAllByAccountIdAndCreatedAtAfter(1L, from)).thenReturn(3);

        // Act
        notificationService.deleteNotificationsByTime(testAccount, from, null);

        // Assert
        verify(notificationRepository).deleteAllByAccountIdAndCreatedAtAfter(1L, from);
        verify(notificationRepository, never()).deleteAllByAccountIdAndCreatedAtBetween(anyLong(), any(), any());
        verify(notificationRepository, never()).deleteAllByAccountIdAndCreatedAtBefore(anyLong(), any());
        verify(notificationRepository, never()).deleteAllByAccountId(anyLong());
    }

    @Test
    void testDeleteNotificationsByTime_whenOnlyToProvided_shouldDeleteBefore() {
        // Arrange
        LocalDateTime to = LocalDateTime.now().minusDays(1);
        when(notificationRepository.deleteAllByAccountIdAndCreatedAtBefore(1L, to)).thenReturn(2);

        // Act
        notificationService.deleteNotificationsByTime(testAccount, null, to);

        // Assert
        verify(notificationRepository).deleteAllByAccountIdAndCreatedAtBefore(1L, to);
        verify(notificationRepository, never()).deleteAllByAccountIdAndCreatedAtBetween(anyLong(), any(), any());
        verify(notificationRepository, never()).deleteAllByAccountIdAndCreatedAtAfter(anyLong(), any());
        verify(notificationRepository, never()).deleteAllByAccountId(anyLong());
    }

    @Test
    void testDeleteNotificationsByTime_whenBothAreNull_shouldDeleteAll() {
        // Arrange
        when(notificationRepository.deleteAllByAccountId(1L)).thenReturn(10);

        // Act
        notificationService.deleteNotificationsByTime(testAccount, null, null);

        // Assert
        verify(notificationRepository).deleteAllByAccountId(1L);
        verify(notificationRepository, never()).deleteAllByAccountIdAndCreatedAtBetween(anyLong(), any(), any());
        verify(notificationRepository, never()).deleteAllByAccountIdAndCreatedAtAfter(anyLong(), any());
        verify(notificationRepository, never()).deleteAllByAccountIdAndCreatedAtBefore(anyLong(), any());
    }

    // ========== EDGE CASES AND ERROR HANDLING TESTS ==========

    @Test
    void testNotify_whenRepositoryThrowsException_shouldPropagateException() {
        // Arrange
        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.notify(testAccount, "title", "content", "SYSTEM"));
        assertEquals("Database error", exception.getMessage());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testSendNotificationToUsers_whenAccountServiceThrowsException_shouldPropagateException() {
        // Arrange
        List<Long> accountIds = Arrays.asList(1L);
        when(accountService.fetchAccountById(1L)).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.sendNotificationToUsers(accountIds, "title", "content", "SYSTEM"));
        assertEquals("Service error", exception.getMessage());
        verify(accountService).fetchAccountById(1L);
    }

    @Test
    void testGetUserNotifications_whenRepositoryThrowsException_shouldPropagateException() {
        // Arrange
        when(notificationRepository.findByAccountOrderByCreatedAtDesc(testAccount))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.getUserNotifications(testAccount));
        assertEquals("Database error", exception.getMessage());
        verify(notificationRepository).findByAccountOrderByCreatedAtDesc(testAccount);
    }

    @Test
    void testDeleteNotificationsByTime_whenRepositoryThrowsException_shouldPropagateException() {
        // Arrange
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        when(notificationRepository.deleteAllByAccountIdAndCreatedAtBetween(1L, from, to))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.deleteNotificationsByTime(testAccount, from, to));
        assertEquals("Database error", exception.getMessage());
        verify(notificationRepository).deleteAllByAccountIdAndCreatedAtBetween(1L, from, to);
    }
}
