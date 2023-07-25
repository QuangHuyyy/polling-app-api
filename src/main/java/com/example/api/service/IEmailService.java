package com.example.api.service;

import com.example.api.model.EmailDetail;

public interface IEmailService {
    void sendInviteVotePollMail(EmailDetail detail, String token, String pollUuid);
    void sendResetPasswordMail(String emailRecipient, String name, String token);
}
