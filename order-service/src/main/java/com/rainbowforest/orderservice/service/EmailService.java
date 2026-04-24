package com.rainbowforest.orderservice.service;

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

    @Async
    public void sendOrderSuccessEmail(String toEmail, String customerName, Long orderId, double totalAmount,
            String paymentMethod) {
        String subject = "🎉 Đặt hàng thành công! Đơn hàng #" + orderId + " - FIXT STORE";
        String pMethodText = "VNPAY".equalsIgnoreCase(paymentMethod) ? "Trực tuyến qua VNPAY (Đã thanh toán)"
                : "Thanh toán khi nhận hàng (COD)";

        String body = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 16px;'>"
                + "<div style='text-align: center; margin-bottom: 20px;'>"
                + "<h1 style='color: #2563eb; margin: 0;'>FIXT STORE</h1>"
                + "<p style='color: #10b981; font-weight: bold; font-size: 18px;'>CẢM ƠN BẠN ĐÃ MUA SẮM!</p>"
                + "</div>"
                + "<p>Xin chào <b>" + customerName + "</b>,</p>"
                + "<p>Chúng tôi đã nhận được đơn hàng của bạn và đang tiến hành xử lý. Dưới đây là thông tin tóm tắt đơn hàng:</p>"
                + "<div style='background-color: #f8fafc; padding: 20px; border-radius: 12px; margin: 20px 0;'>"
                + "<p style='margin: 5px 0;'><b>Mã đơn hàng:</b> #" + orderId + "</p>"
                + "<p style='margin: 5px 0;'><b>Phương thức thanh toán:</b> " + pMethodText + "</p>"
                + "<p style='margin: 5px 0; font-size: 18px;'><b>Tổng thanh toán: <span style='color: #ef4444;'>"
                + formatVND(totalAmount) + "</span></b></p>"
                + "</div>"
                + "<p>Bạn có thể kiểm tra chi tiết trạng thái giao hàng trong mục <b>Lịch sử đơn hàng</b> trên website.</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='http://localhost:5173/profile' style='background-color: #0f172a; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 8px; font-weight: bold;'>XEM ĐƠN HÀNG CỦA BẠN</a>"
                + "</div>"
                + "</div>";
        sendHtmlEmail(toEmail, subject, body);
    }
}