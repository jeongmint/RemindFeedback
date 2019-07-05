package com.remindfeedbackproject.Etc;

public class NotificationModel {
    public String to;
    public Notification notification;

    public static class Notification {
        public String title;
        public String text;
        public String requestUserEmail;
        public String requestFeedbackKey;
    }
}
