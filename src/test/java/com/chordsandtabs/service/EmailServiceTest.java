package com.chordsandtabs.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void shouldSendVerificationEmail() {
        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.sendVerificationEmail("test@test.pl", "token123");

        verify(mailSender).send(message);
    }

    @Test
    void shouldThrowOnMailFailure() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail creation failed"));

        assertThrows(RuntimeException.class,
                () -> emailService.sendVerificationEmail("test@test.pl", "token"));
    }
}
