package com.example.springboot_education.services.mail;

import com.example.springboot_education.exceptions.HttpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Education System");
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực OTP - Education System");

            String htmlContent = buildOtpEmailTemplate(otp, fullName, "đăng ký");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Registration OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send registration OTP email to: {}", toEmail, e);
            throw new HttpException("Failed to send verification email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void sendChangePasswordOtpEmail(String toEmail, String otp, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Education System");
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực thay đổi mật khẩu - Education System");

            String htmlContent = buildChangePasswordOtpTemplate(otp, fullName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Change password OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send change password OTP email to: {}", toEmail, e);
            throw new HttpException("Failed to send change password email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void sendForgotPasswordOtpEmail(String toEmail, String otp, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Education System");
            helper.setTo(toEmail);
            helper.setSubject("Khôi phục mật khẩu - Education System");

            String htmlContent = buildForgotPasswordOtpTemplate(otp, fullName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Forgot password OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send forgot password OTP email to: {}", toEmail, e);
            throw new HttpException("Failed to send forgot password email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String buildOtpEmailTemplate(String otp, String fullName, String purpose) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Xác thực OTP</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; margin-bottom: 30px; }
                    .otp-code { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0; }
                    .otp-number { font-size: 32px; font-weight: bold; color: #28a745; letter-spacing: 5px; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; font-size: 14px; }
                    .warning { background-color: #fff3cd; padding: 15px; border-radius: 5px; color: #856404; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 style="color: #28a745;">Education System</h1>
                        <h2>Xác thực tài khoản của bạn</h2>
                    </div>
                    
                    <p>Xin chào <strong>""" + fullName + """
                        </strong>,</p>
                    
                    <p>Cảm ơn bạn đã đăng ký tài khoản tại Education System. Để hoàn tất quá trình """ + purpose + """
                        , vui lòng nhập mã OTP bên dưới:</p>
                    
                    <div class="otp-code">
                        <p style="margin: 0; color: #666;">Mã xác thực OTP của bạn là:</p>
                        <div class="otp-number">""" + otp + """
                        </div>
                    </div>
                    
                    <div class="warning">
                        <strong>Lưu ý quan trọng:</strong>
                        <ul style="margin: 10px 0; padding-left: 20px;">
                            <li>Mã OTP này có hiệu lực trong <strong>5 phút</strong></li>
                            <li>Không chia sẻ mã này với bất kỳ ai</li>
                            <li>Nếu bạn không yêu cầu """ + purpose + """
                                , vui lòng bỏ qua email này</li>
                        </ul>
                    </div>
                    
                    <p>Nếu bạn gặp bất kỳ vấn đề gì, vui lòng liên hệ với chúng tôi qua email hỗ trợ.</p>
                    
                    <div class="footer">
                        <p>Trân trọng,<br>Education System Team</p>
                        <p style="margin-top: 15px;">
                            <small>Email này được gửi tự động, vui lòng không trả lời.</small>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    private String buildChangePasswordOtpTemplate(String otp, String fullName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Thay đổi mật khẩu</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; margin-bottom: 30px; }
                    .otp-code { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0; }
                    .otp-number { font-size: 32px; font-weight: bold; color: #dc3545; letter-spacing: 5px; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; font-size: 14px; }
                    .warning { background-color: #f8d7da; padding: 15px; border-radius: 5px; color: #721c24; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 style="color: #dc3545;">🔐 Education System</h1>
                        <h2>Thay đổi mật khẩu</h2>
                    </div>
                    
                    <p>Xin chào <strong>""" + fullName + """
                        </strong>,</p>
                    
                    <p>Chúng tôi nhận được yêu cầu thay đổi mật khẩu cho tài khoản của bạn. Để xác nhận thay đổi, vui lòng nhập mã OTP bên dưới:</p>
                    
                    <div class="otp-code">
                        <p style="margin: 0; color: #666;">Mã xác thực OTP của bạn là:</p>
                        <div class="otp-number">""" + otp + """
                        </div>
                    </div>
                    
                    <div class="warning">
                        <strong>⚠️ Lưu ý bảo mật:</strong>
                        <ul style="margin: 10px 0; padding-left: 20px;">
                            <li>Mã OTP này có hiệu lực trong <strong>5 phút</strong></li>
                            <li>Tuyệt đối không chia sẻ mã này với bất kỳ ai</li>
                            <li>Nếu bạn không yêu cầu thay đổi mật khẩu, vui lòng bỏ qua email này và kiểm tra bảo mật tài khoản</li>
                            <li>Sau khi thay đổi mật khẩu thành công, bạn sẽ cần đăng nhập lại</li>
                        </ul>
                    </div>
                    
                    <p>Nếu bạn gặp bất kỳ vấn đề gì, vui lòng liên hệ với chúng tôi ngay lập tức qua email hỗ trợ.</p>
                    
                    <div class="footer">
                        <p>Trân trọng,<br>Education System Team</p>
                        <p style="margin-top: 15px;">
                            <small>Email này được gửi tự động, vui lòng không trả lời.</small>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    private String buildForgotPasswordOtpTemplate(String otp, String fullName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Khôi phục mật khẩu</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; margin-bottom: 30px; }
                    .otp-code { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0; }
                    .otp-number { font-size: 32px; font-weight: bold; color: #fd7e14; letter-spacing: 5px; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; font-size: 14px; }
                    .warning { background-color: #fff3cd; padding: 15px; border-radius: 5px; color: #856404; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 style="color: #fd7e14;">🔑 Education System</h1>
                        <h2>Khôi phục mật khẩu</h2>
                    </div>
                    
                    <p>Xin chào <strong>""" + fullName + """
                        </strong>,</p>
                    
                    <p>Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn. Để tiếp tục quá trình đặt lại mật khẩu, vui lòng nhập mã OTP bên dưới:</p>
                    
                    <div class="otp-code">
                        <p style="margin: 0; color: #666;">Mã xác thực OTP của bạn là:</p>
                        <div class="otp-number">""" + otp + """
                        </div>
                    </div>
                    
                    <div class="warning">
                        <strong>🔒 Hướng dẫn khôi phục:</strong>
                        <ul style="margin: 10px 0; padding-left: 20px;">
                            <li>Mã OTP này có hiệu lực trong <strong>5 phút</strong></li>
                            <li>Sau khi xác thực OTP, bạn sẽ được yêu cầu nhập mật khẩu mới</li>
                            <li>Tuyệt đối không chia sẻ mã này với bất kỳ ai</li>
                            <li>Nếu bạn không yêu cầu khôi phục mật khẩu, vui lòng bỏ qua email này và kiểm tra bảo mật tài khoản</li>
                        </ul>
                    </div>
                    
                    <p><strong>Lưu ý:</strong> Sau khi đặt lại mật khẩu thành công, tất cả các phiên đăng nhập hiện tại sẽ bị hủy và bạn cần đăng nhập lại.</p>
                    
                    <p>Nếu bạn gặp bất kỳ vấn đề gì, vui lòng liên hệ với chúng tôi ngay lập tức qua email hỗ trợ.</p>
                    
                    <div class="footer">
                        <p>Trân trọng,<br>Education System Team</p>
                        <p style="margin-top: 15px;">
                            <small>Email này được gửi tự động, vui lòng không trả lời.</small>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}