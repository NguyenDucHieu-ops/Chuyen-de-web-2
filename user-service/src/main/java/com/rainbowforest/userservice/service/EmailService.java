package com.rainbowforest.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private String formatVND(double amount) {
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyVN = NumberFormat.getCurrencyInstance(localeVN);
        return currencyVN.format(amount);
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("FIXT STORE <noreply@fixtstore.com>");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
        }
    }

    // 1. EMAIL QUÊN MẬT KHẨU
    public void sendForgotPasswordEmail(String toEmail, String userName, String newPassword) {
        String subject = "🔑 Đặt lại mật khẩu - FIXT STORE";
        String body = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 16px;'>"
                + "<h2 style='color: #2563eb; text-align: center;'>FIXT STORE</h2>"
                + "<p>Xin chào <b>" + userName + "</b>,</p>"
                + "<p>Hệ thống đã tạo một mật khẩu mới cho tài khoản của bạn. Vui lòng sử dụng mật khẩu dưới đây để đăng nhập, sau đó hãy đổi lại mật khẩu để đảm bảo an toàn:</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<span style='background-color: #f1f5f9; color: #0f172a; padding: 14px 28px; border-radius: 8px; font-weight: bold; font-size: 20px; letter-spacing: 2px;'>"
                + newPassword + "</span>"
                + "</div>"
                + "<p style='color: #64748b; font-size: 14px;'>Nếu bạn không thực hiện yêu cầu này, vui lòng liên hệ ngay với chúng tôi.</p>"
                + "</div>";
        sendHtmlEmail(toEmail, subject, body);
    }

    // 2. EMAIL ĐỔI MẬT KHẨU THÀNH CÔNG
    public void sendPasswordChangedEmail(String toEmail, String userName) {
        String subject = "✅ Đổi mật khẩu thành công - FIXT STORE";
        String body = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 16px;'>"
                + "<h2 style='color: #10b981; text-align: center;'>THÀNH CÔNG</h2>"
                + "<p>Xin chào <b>" + userName + "</b>,</p>"
                + "<p>Mật khẩu tài khoản FIXT STORE của bạn vừa được thay đổi thành công.</p>"
                + "<hr style='border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;'/>"
                + "<p style='font-size: 12px; color: #94a3b8; text-align: center;'>Bộ phận bảo mật FIXT STORE</p>"
                + "</div>";
        sendHtmlEmail(toEmail, subject, body);
    }
}