package it.acsoftware.hyperiot.mail.api;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.mail.model.MailTemplate;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * @author Aristide Cittadino Interface component for %- projectSuffixUC
 * SystemApi. This interface defines methods for additional operations.
 */
public interface MailSystemApi extends HyperIoTBaseEntitySystemApi<MailTemplate> {
    void createTemplate(Bundle bundle,String templatePath);
    void sendMail(String from, List<String> recipients, List<String> ccRecipients,
                  List<String> bccRecipients, String subject, String content, List<byte[]> attachments);
    String generateTextFromStringTemplate(String templateText, HashMap<String, Object> params);
    String generateTextFromTemplate(String templateName, HashMap<String, Object> params) throws IOException;

}
