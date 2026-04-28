package com.ledger.ledgerservice.model.dto.request.email;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class EmailSendRequest {
    private String from;
    private String to;
    private List<String> cc;
    private List<String> bcc;
    private String replyTo;
    private String subject;
    private String htmlBody;
    private String textBody;
    private List<EmailAttachment> attachments;
    private List<EmailInlineResource> inlineResources;

    public List<String> safeCc() {
        return cc != null ? cc : new ArrayList<>();
    }

    public List<String> safeBcc() {
        return bcc != null ? bcc : new ArrayList<>();
    }

    public List<EmailAttachment> safeAttachments() {
        return attachments != null ? attachments : new ArrayList<>();
    }

    public List<EmailInlineResource> safeInlineResources() {
        return inlineResources != null ? inlineResources : new ArrayList<>();
    }
}
