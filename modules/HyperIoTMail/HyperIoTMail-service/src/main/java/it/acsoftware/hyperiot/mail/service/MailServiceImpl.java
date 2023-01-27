/*
 * Copyright 2019-2023 ACSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.mail.service;

import it.acsoftware.hyperiot.base.security.annotations.AllowGenericPermissions;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntityServiceImpl;
import it.acsoftware.hyperiot.mail.actions.HyperIoTMailAction;
import it.acsoftware.hyperiot.mail.api.MailApi;
import it.acsoftware.hyperiot.mail.api.MailSystemApi;
import it.acsoftware.hyperiot.mail.model.MailTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


/**
 * @author Aristide Cittadino Implementation class of MailApi interface. It is
 * used to implement all additional methods in order to interact with
 * the system layer.
 */
@Component(service = MailApi.class, immediate = true)
public final class MailServiceImpl extends HyperIoTBaseEntityServiceImpl<MailTemplate>
    implements MailApi {

    /**
     * Injecting the MailSystemApi
     */
    private MailSystemApi systemService;

    /**
     * Constructor for a MailServiceImpl
     */
    public MailServiceImpl() {
        super(MailTemplate.class);
    }

    /**
     * @return The current MailSystemApi
     */
    protected MailSystemApi getSystemService() {
        getLog().debug( "invoking getSystemService, returning: {}", systemService);
        return systemService;
    }

    /**
     * @param mailSystemService Injecting via OSGi DS current systemService
     */
    @Reference
    protected void setSystemService(MailSystemApi mailSystemService) {
        getLog().debug( "invoking setSystemService, setting: {}", systemService);
        systemService = mailSystemService;
    }

    @Override
    @AllowGenericPermissions(actions = HyperIoTMailAction.Names.SEND_EMAIL)
    public void sendMail(String from, List<String> recipients, List<String> ccRecipients,
                         List<String> bccRecipients, String subject, String content, List<byte[]> attachments) {
        systemService.sendMail(from, recipients, ccRecipients, bccRecipients, subject, content,
            attachments);

    }

    @Override
    @AllowGenericPermissions(actions = HyperIoTMailAction.Names.GENERATE_TEXT_FROM_TEMPLATE)
    public String generateTextFromTemplate(String templateName, HashMap<String, Object> params) throws IOException {
        return systemService.generateTextFromTemplate(templateName, params);
    }

}
