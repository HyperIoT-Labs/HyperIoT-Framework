package it.acsoftware.hyperiot.mail.api;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.mail.model.MailTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * @author Aristide Cittadino Interface component for MailApi. This interface
 * defines methods for additional operations.
 */
public interface MailApi extends HyperIoTBaseEntityApi<MailTemplate> {

    void sendMail(String from, List<String> recipients, List<String> ccRecipients,
                         List<String> bccRecipients, String subject, String content, List<byte[]> attachments);

    String generateTextFromTemplate(String templateName, HashMap<String, Object> params) throws IOException;

}
