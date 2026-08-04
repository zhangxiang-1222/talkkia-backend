package com.talkkia.api.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.sender.email}")
    private String senderEmail;

    @Value("${mail.sender.name}")
    private String senderName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendOtpEmail(String toEmail, int otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject("Your Talkkia OTP Verification Code");
            helper.setText(buildOtpEmailTextContent(otp), buildOtpEmailContent(otp));

            log.info("Sending email to: {}", toEmail);
            log.info("From: {} <{}>", senderName, senderEmail);

            mailSender.send(message);

            log.info("Email sent successfully to: {}", toEmail);
            return true;

        } catch (MessagingException e) {
            log.error("Failed to create or send email message", e);
            return false;
        } catch (MailException e) {
            log.error("Mail server error occurred while sending email", e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error occurred while sending email", e);
            return false;
        }
    }

    private String buildOtpEmailTextContent(int otp) {
        return "Dear User,\n\n" +
                "Your Talkkia verification code is: " + otp + "\n\n" +
                "Please use this code to complete your verification process.\n\n" +
                "If you didn't request this code, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Talkkia Support Team";
    }

    private String buildOtpEmailContent(int otp) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head><body>" +
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<div style='background-color: #f8f9fa; padding: 30px; border-radius: 10px; text-align: center;'>" +
                "<img src='https://i.imgur.com/Lhk0TS5.png' alt='Talkkia' style='height: 100px; margin-bottom: 20px;'/>" +
                "<h2 style='color: #333; margin-bottom: 20px;'>Talkkia OTP Verification</h2>" +
                "<p style='color: #555; font-size: 16px; margin-bottom: 20px;'>Hello,</p>" +
                "<p style='color: #555; font-size: 16px; margin-bottom: 20px;'>To complete your verification, please use the OTP code below:</p>" +
                "<div style='background-color: #6cad29; color: white; font-size: 32px; font-weight: bold; " +
                "padding: 15px 30px; border-radius: 8px; display: inline-block; letter-spacing: 5px;'>" +
                otp + "</div>" +
                "<p style='color: #777; font-size: 14px; margin-top: 30px;'>This code is valid once a time. Please do not share it with anyone.</p>" +
                "<p style='color: #999; font-size: 12px; margin-top: 10px;'>If you didn’t request this code, please disregard this email.</p>" +
                "<p style='color: #666; font-size: 14px; margin-top: 30px;'>— The Talkkia Team</p>" +
                "</div></div></body></html>";
    }
}