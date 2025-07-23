package com.example.demo.service;

import com.example.demo.exception.InternalServerException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    public void sendSimpleEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Xác thực tài khoản");
        message.setText("Mã OTP xác thực của bạn là: " + otp);
        javaMailSender.send(message);
    }

    public void sendForgotPasswordLink(String toEmail, String otp) {
        String link = "http://localhost:3000/reset-password?email=" + toEmail + "&otp=" + otp;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Reset Password");
        message.setText("Click the link to reset password: " + link);
        javaMailSender.send(message);
    }

    @Async
    public void sendHtmlEmailWithInlineImage(String to, String subject, String templateName, Context context, String imageCid, byte[] imageBytes, String imageContentType) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            String htmlContent = templateEngine.process(templateName, context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            if (imageCid != null && imageBytes != null && imageContentType != null) {
                helper.addInline(imageCid, new ByteArrayResource(imageBytes), imageContentType);
            }

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new InternalServerException("Không thể gửi email xác nhận. Vui lòng thử lại sau.", e);
        }
    }
}