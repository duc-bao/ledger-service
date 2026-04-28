package com.ledger.ledgerservice.service.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.ledgerservice.model.dto.request.email.*;
import jakarta.annotation.PostConstruct;
import jakarta.activation.DataHandler;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {
    private static final String TEMPLATE_FILE = "seed/email-templates.json";
    private final ObjectMapper objectMapper;
    private final JavaMailSender mailSender;
    private final Map<String, EmailTemplateDefinition> templateByCode = new HashMap<>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        try {
            ClassPathResource resource = new ClassPathResource(TEMPLATE_FILE);
            if (!resource.exists()) {
                log.warn("Email template file not found: {}", TEMPLATE_FILE);
                return;
            }
            try (InputStream inputStream = resource.getInputStream()) {
                EmailTemplateModel wrapper = objectMapper.readValue(inputStream, EmailTemplateModel.class);
                if (wrapper == null || wrapper.getTemplates() == null) {
                    return;
                }
                for (EmailTemplateDefinition template : wrapper.getTemplates()) {
                    if (StringUtils.hasText(template.getCode())) {
                        templateByCode.put(template.getCode().toUpperCase(), template);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Failed to initialize email templates", ex);
        }
    }

    @Async("businessLogExecutor")
    public void sendAsync(EmailSendRequest request) {
        try {
            MimeMessage mimeMessage = buildMimeMessage(request);
            mailSender.send(mimeMessage);
        } catch (Exception ex) {
            log.error("Send email failed to={}", request != null ? request.getTo() : null, ex);
        }
    }

    @Async("businessLogExecutor")
    public void sendByTemplateAsync(String templateCode, Map<String, String> values, EmailSendRequest baseRequest) {
        String subject = renderSubject(templateCode, values);
        String body = renderBody(templateCode, values);
        EmailSendRequest request = EmailSendRequest.builder()
                .from(baseRequest.getFrom())
                .to(baseRequest.getTo())
                .cc(baseRequest.getCc())
                .bcc(baseRequest.getBcc())
                .replyTo(baseRequest.getReplyTo())
                .subject(subject)
                .htmlBody(body)
                .textBody(baseRequest.getTextBody())
                .attachments(baseRequest.getAttachments())
                .inlineResources(baseRequest.getInlineResources())
                .build();
        sendAsync(request);
    }

    public String renderSubject(String code, Map<String, String> values) {
        EmailTemplateDefinition template = templateByCode.get(code.toUpperCase());
        if (template == null) {
            return "";
        }
        return applyValues(template.getSubject(), values);
    }

    public String renderBody(String code, Map<String, String> values) {
        EmailTemplateDefinition template = templateByCode.get(code.toUpperCase());
        if (template == null) {
            return "";
        }
        return applyValues(template.getTemplate(), values);
    }

    private String applyValues(String text, Map<String, String> values) {
        if (!StringUtils.hasText(text) || values == null || values.isEmpty()) {
            return text;
        }
        String result = text;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            result = result.replace(key, entry.getValue() == null ? "" : entry.getValue());
        }
        return result;
    }

    private MimeMessage buildMimeMessage(EmailSendRequest request) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        if (StringUtils.hasText(request.getFrom())) {
            mimeMessage.setFrom(new InternetAddress(request.getFrom()));
        }
        mimeMessage.setRecipients(Message.RecipientType.TO, new Address[]{new InternetAddress(request.getTo())});
        setRecipients(mimeMessage, Message.RecipientType.CC, request.safeCc());
        setRecipients(mimeMessage, Message.RecipientType.BCC, request.safeBcc());
        if (StringUtils.hasText(request.getReplyTo())) {
            mimeMessage.setReplyTo(new Address[]{new InternetAddress(request.getReplyTo())});
        }
        mimeMessage.setSubject(request.getSubject(), "UTF-8");

        Multipart mixed = new MimeMultipart("mixed");
        MimeBodyPart contentPart = new MimeBodyPart();
        Multipart related = new MimeMultipart("related");
        MimeBodyPart htmlPart = new MimeBodyPart();
        String htmlBody = StringUtils.hasText(request.getHtmlBody()) ? request.getHtmlBody() : "";
        htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
        related.addBodyPart(htmlPart);

        if (StringUtils.hasText(request.getTextBody())) {
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(request.getTextBody(), "UTF-8");
            related.addBodyPart(textPart);
        }

        for (EmailInlineResource inline : request.safeInlineResources()) {
            if (inline.getContent() == null || !StringUtils.hasText(inline.getContentId())) {
                continue;
            }
            MimeBodyPart inlinePart = new MimeBodyPart();
            inlinePart.setDataHandler(new DataHandler(new ByteArrayDataSource(inline.getContent(), inline.getContentType())));
            inlinePart.setHeader("Content-ID", "<" + inline.getContentId() + ">");
            inlinePart.setDisposition(MimeBodyPart.INLINE);
            related.addBodyPart(inlinePart);
        }

        contentPart.setContent(related);
        mixed.addBodyPart(contentPart);

        for (EmailAttachment attachment : request.safeAttachments()) {
            if (attachment.getContent() == null || !StringUtils.hasText(attachment.getFileName())) {
                continue;
            }
            MimeBodyPart filePart = new MimeBodyPart();
            filePart.setDataHandler(new DataHandler(new ByteArrayDataSource(attachment.getContent(), attachment.getContentType())));
            filePart.setFileName(attachment.getFileName());
            mixed.addBodyPart(filePart);
        }

        mimeMessage.setContent(mixed);
        mimeMessage.saveChanges();
        return mimeMessage;
    }

    private void setRecipients(MimeMessage mimeMessage, Message.RecipientType type, List<String> emails) throws Exception {
        if (emails == null || emails.isEmpty()) {
            return;
        }
        Address[] addresses = emails.stream()
                .filter(StringUtils::hasText)
                .map(this::toAddress)
                .toArray(Address[]::new);
        if (addresses.length > 0) {
            mimeMessage.setRecipients(type, addresses);
        }
    }

    private Address toAddress(String email) {
        try {
            return new InternetAddress(email);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid email address: " + email, ex);
        }
    }
}
