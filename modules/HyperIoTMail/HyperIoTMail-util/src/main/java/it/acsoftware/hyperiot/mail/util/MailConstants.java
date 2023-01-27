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

package it.acsoftware.hyperiot.mail.util;

public class MailConstants {
	public static String MAIL_SMTP_HOST = "it.acsoftware.hyperiot.mail.smtp.host";
	public static String MAIL_SMTP_PORT = "it.acsoftware.hyperiot.mail.smtp.port";
	public static String MAIL_USERNAME = "it.acsoftware.hyperiot.mail.username";
	public static String MAIL_PASSWORD = "it.acsoftware.hyperiot.mail.password";
	public static String MAIL_EVENT_SENDER = "it.acsoftware.hyperiot.mail.alertSender";
	public static String MAIL_SMTP_AUTH = "it.acsoftware.hyperiot.mail.smtp.auth";
	public static String MAIL_SMTP_START_TTLS_ENABLED = "it.acsoftware.hyperiot.mail.smtp.starttls.enable";
	public static String MAIL_SMTP_TEST_RECIPIENTS = "it.acsoftware.hyperiot.mail.testRecipients";
	
	public static String MAIL_TEMPLATE_TEST = "test.ftl";
	public static String MAIL_TEMPLATE_REGISTRATION = "registrationConfirmation.ftl";
	public static String MAIL_TEMPLATE_PWD_RESET = "passwordReset.ftl";
	public static String MAIL_TEMPLATE_DEVICE_PWD_RESET = "devicePasswordReset.ftl";
	public static String MAIL_TEMPLATE_ACCOUNT_DELETION = "accountDeletion.ftl";
	
}
