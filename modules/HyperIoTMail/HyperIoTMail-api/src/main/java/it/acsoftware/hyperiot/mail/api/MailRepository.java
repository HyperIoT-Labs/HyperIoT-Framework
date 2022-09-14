package it.acsoftware.hyperiot.mail.api;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseRepository;

import it.acsoftware.hyperiot.mail.model.MailTemplate;
import org.osgi.framework.Bundle;

/**
 *
 * @author Aristide Cittadino Interface component for Mail Repository.
 *         It is used for CRUD operations,
 *         and to interact with the persistence layer.
 *
 */
public interface MailRepository extends HyperIoTBaseRepository<MailTemplate> {
    void createTemplate(Bundle bundle,String templatePath);
    MailTemplate findByName(String name);
}
