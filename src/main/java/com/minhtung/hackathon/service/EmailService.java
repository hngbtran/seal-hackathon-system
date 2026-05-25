package com.minhtung.hackathon.service;

import com.google.gson.JsonObject;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value ;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class EmailService {
  private final JavaMailSender mailSender;

  @Value("${app.base-url}")
  private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public boolean sendVerificationEmail(String email, String token) {
    String verifLink = baseUrl + "/auth/verify?token=" + token;

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom("mtung638@gmail.com");
      helper.setTo(email);
      helper.setSubject("Xác  nhận đăng kí tài khoản");

      helper.setText(
              "<h2>Xác nhận tài khoản</h2>" +
                      "<p>Click vào nút bên dưới để kích hoạt:</p>" +
                      "<a href='" + verifLink + "' style='padding:12px 24px;" +
                      "background:#4F46E5;color:white;text-decoration:none;" +
                      "border-radius:6px;display:inline-block'> Xác nhận Email</a>" +
                      "<p>Link hết hạn sau <b>15 phút</b>.</p>",
              true
      );
      mailSender.send(message);
      return true;


    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
