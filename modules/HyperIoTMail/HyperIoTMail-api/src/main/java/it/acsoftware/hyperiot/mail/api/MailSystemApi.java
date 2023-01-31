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
