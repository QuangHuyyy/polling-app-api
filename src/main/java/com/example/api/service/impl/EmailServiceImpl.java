package com.example.api.service.impl;

import com.example.api.exception.BadRequestException;
import com.example.api.model.EmailDetail;
import com.example.api.service.IEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${application.client.url}")
    String clientUrl = "";

    @Override
    public void sendInviteVotePollMail(EmailDetail detail, String token, String pollUuid) {
        try {
            String ownerName = detail.getOwnerName();
            String ownerEmail = detail.getOwnerEmail();
            String linkPoll = clientUrl + "/polls/" + pollUuid + "?token=" + token;

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");
            String htmlMsg = "<div style='margin-top: 8px;'>" +
                    "<div style='background:#f9f9f9'>" +
                    "<div style='margin:0px auto;max-width:640px;background:transparent'>" +
                    "<div style='padding:40px 0px'>" +
                    "<a href='" + clientUrl + "' style='margin: 0 auto;width:208px; display: block;'>" +
                    "<img style='display: block; object-fit: cover; width: 100%;' src='https://ci6.googleusercontent.com/proxy/5qaB8y5STkEM89ERqb5p843ppZHFtCHjQcmGIaL6bsrxO72icwcDy735LQdn0jeB7N6m9sY0Jrm9gyuHg883yKHgwcvgkR67IDMe0kqde5a667DOjw=s0-d-e1-ft#https://strawpoll.com/images/logos/strawpoll-logo-transparent2.png'/>" +
                    "</a>" +
                    "</div>" +
                    "</div>" +
                    "<div style='max-width:640px;margin:0 auto;border-radius:4px;overflow:hidden'>" +
                    "<div style='margin:0px auto 112px;max-width:640px;background:#ffffff'>" +
                    "<div style='padding:40px 50px;border-top:3px solid #f59e0b'>" +
                    "<div style='color:#737f8d;font-size:16px;line-height:24px;text-align:left'>" +
                    "<h2 style='font-weight:500;font-size:20px;color:#4f545c;letter-spacing:0.27px'>Invitation to Vote</h2>" +
                    "<p>" + ownerName +" (<a href='" + ownerEmail + "' target='_blank'>" + ownerEmail + "</a>) invites you to participate in this StrawPoll test.</p>" +
                    "<p>Note: Please do not share this link as it contains your personal voting token.</p>" +
                    "</div>" +
                    "<div style='text-align: center;'>" +
                    "<div style='display: inline-block;word-break:break-word;font-size:0px;padding:10px 25px;padding-top:20px'>" +
                    "<a href='" + linkPoll + "' style='display: block; padding:15px 19px; text-decoration:none;line-height:100%;background:#667eea;color:white;font-size:15px;font-weight:normal;text-transform:none;margin:0px' target='_blank'>Participate now</a>" +
                    "</div>" +
                    "</div>" +
                    "<div style='word-break:break-word;font-size:0px;padding:0px;padding-top:20px'>" +
                    "<div style='color:#737f8d;font-size:16px;line-height:24px;text-align:center'>" +
                    "<p>If the link doesn't work, copy this URL into your browser:</p>" +
                    "<p>" +
                    "<a href='" + linkPoll + "' target='_blank'>" + linkPoll + "</a>" +
                    "</p>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>";
            helper.setFrom(sender);
            helper.setTo(detail.getRecipient());
            helper.setSubject(ownerName + " via StrawPoll");
            helper.setText(htmlMsg, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new BadRequestException("Error while Sending Mail");
        }
    }

    @Override
    public void sendResetPasswordMail(String emailRecipient, String name, String token) {
        try {
            String linkReset = clientUrl + "/auth/new-password/" + token;
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");
            String htmlMsg = "<div style='margin-top: 8px;'>" +
                    "<div style='background:#f9f9f9'>" +
                    "<div style='margin:0px auto;max-width:640px;background:transparent'>" +
                    "<div style='padding:40px 0px'>" +
                    "<a href='" + clientUrl + "' style='margin: 0 auto;width:208px; display: block;'>" +
                    "<img style='display: block; object-fit: cover; width: 100%;' src='https://ci6.googleusercontent.com/proxy/5qaB8y5STkEM89ERqb5p843ppZHFtCHjQcmGIaL6bsrxO72icwcDy735LQdn0jeB7N6m9sY0Jrm9gyuHg883yKHgwcvgkR67IDMe0kqde5a667DOjw=s0-d-e1-ft#https://strawpoll.com/images/logos/strawpoll-logo-transparent2.png'/>" +
                    "</a>" +
                    "</div>" +
                    "</div>" +
                    "<div style='max-width:640px;margin:0 auto;border-radius:4px;overflow:hidden'>" +
                    "<div style='margin:0px auto 112px;max-width:640px;background:#ffffff'>" +
                    "<div style='padding:40px 50px;border-top:3px solid #f59e0b'>" +
                    "<div style='color:#737f8d;font-size:16px;line-height:24px;text-align:left'>" +
                    "<h2 style='font-weight:500;font-size:20px;color:#4f545c;letter-spacing:0.27px'>Reset your StrawPoll password</h2>" +
                    "<p>Hey " + name + ",</p>" +
                    "<p>We received a request to set a new password for your StrawPoll account:</p>" +
                    "</div>" +
                    "<div style='text-align: center;'>" +
                    "<div style='display: inline-block;word-break:break-word;font-size:0px;padding:10px 25px;padding-top:20px'>" +
                    "<a href='" + linkReset + "' style='display: block; padding:15px 19px; text-decoration:none;line-height:100%;background:#667eea;color:white;font-size:15px;font-weight:normal;text-transform:none;margin:0px' target='_blank'>Set a new password</a>" +
                    "</div>" +
                    "</div>" +
                    "<div style='word-break:break-word;font-size:0px;padding:0px;padding-top:20px'>" +
                    "<div style='color:#737f8d;font-size:16px;line-height:24px;text-align:center'>" +
                    "<p>If the link doesn't work, copy this URL into your browser:</p>" +
                    "<p>" +
                    "<a href='" + linkReset + "' target='_blank'>" + linkReset + "</a>" +
                    "</p>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>";
            helper.setFrom(sender);
            helper.setTo(emailRecipient);
            helper.setSubject("Password reset on StrawPoll");
            helper.setText(htmlMsg, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new BadRequestException("Error while Sending Mail");
        }
    }
}
