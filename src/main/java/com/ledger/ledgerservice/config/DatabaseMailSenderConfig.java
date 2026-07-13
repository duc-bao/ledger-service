package com.ledger.ledgerservice.config;

import com.ledger.ledgerservice.model.entity.EmailConfigEntity;
import com.ledger.ledgerservice.repository.EmailConfigRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class DatabaseMailSenderConfig {

    private final EmailConfigRepository emailConfigRepository;

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return new DatabaseAwareJavaMailSender(emailConfigRepository);
    }

    private static final class DatabaseAwareJavaMailSender implements JavaMailSender {
        private final EmailConfigRepository emailConfigRepository;

        private DatabaseAwareJavaMailSender(EmailConfigRepository emailConfigRepository) {
            this.emailConfigRepository = emailConfigRepository;
        }

        @Override
        public MimeMessage createMimeMessage() {
            return buildDelegate().createMimeMessage();
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) {
            return buildDelegate().createMimeMessage(contentStream);
        }

        @Override
        public void send(MimeMessage mimeMessage) {
            buildDelegate().send(mimeMessage);
        }

        @Override
        public void send(MimeMessage... mimeMessages) {
            buildDelegate().send(mimeMessages);
        }

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) {
            buildDelegate().send(mimeMessagePreparator);
        }

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) {
            buildDelegate().send(mimeMessagePreparators);
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) {
            buildDelegate().send(simpleMessage);
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
            buildDelegate().send(simpleMessages);
        }

        private JavaMailSenderImpl buildDelegate() {
            EmailConfigEntity activeConfig = emailConfigRepository
                    .findByEnabledTrueOrderByIsDefaultDescNameAsc()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No enabled email config found in database"));

            JavaMailSenderImpl delegate = new JavaMailSenderImpl();
            delegate.setHost(activeConfig.getHost());
            if (activeConfig.getPort() != null) {
                delegate.setPort(activeConfig.getPort());
            }
            if (StringUtils.hasText(activeConfig.getUsername())) {
                delegate.setUsername(activeConfig.getUsername());
            }
            if (activeConfig.getPassword() != null) {
                delegate.setPassword(activeConfig.getPassword());
            }
            delegate.setProtocol(StringUtils.hasText(activeConfig.getProtocol()) ? activeConfig.getProtocol() : "smtp");
            delegate.setDefaultEncoding(StringUtils.hasText(activeConfig.getEncoding()) ? activeConfig.getEncoding() : StandardCharsets.UTF_8.name());

            Properties javaMailProperties = delegate.getJavaMailProperties();
            javaMailProperties.put("mail.smtp.auth", Boolean.toString(activeConfig.getAuthEnabled()));
            javaMailProperties.put("mail.smtp.starttls.enable", Boolean.toString(activeConfig.getStarttlsEnabled()));
            javaMailProperties.put("mail.smtp.ssl.enable", Boolean.toString(activeConfig.getSslEnabled()));
            javaMailProperties.put("mail.debug", Boolean.toString(activeConfig.getDebugEnabled()));

            if (activeConfig.getTimeoutMs() != null) {
                javaMailProperties.put("mail.smtp.timeout", activeConfig.getTimeoutMs().toString());
            }
            if (activeConfig.getConnectionTimeoutMs() != null) {
                javaMailProperties.put("mail.smtp.connectiontimeout", activeConfig.getConnectionTimeoutMs().toString());
            }
            if (activeConfig.getWriteTimeoutMs() != null) {
                javaMailProperties.put("mail.smtp.writetimeout", activeConfig.getWriteTimeoutMs().toString());
            }

            return delegate;
        }
    }
}