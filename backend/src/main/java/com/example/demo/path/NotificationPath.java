package com.example.demo.path;

public class NotificationPath {
    public static final String GET_MY_NOTIFICATIONS = "/filter";
    public static final String MARK_AS_READ = "/{id}/read";
    public static final String DELETE_BY_TIME = "/range";
    public static final String DELETE_NOTIFICATION = "/{id}";
    public static final String SEND_NOTIFICATION = "/send";
    public static final String SEND_NOTIFICATION_TO_ALL = "/send-all";
}
