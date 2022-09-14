package it.acsoftware.hyperiot.mail.service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import java.util.stream.Collectors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntitySystemServiceImpl;
import it.acsoftware.hyperiot.mail.api.MailRepository;
import it.acsoftware.hyperiot.mail.api.MailSystemApi;
import it.acsoftware.hyperiot.mail.model.MailTemplate;
import it.acsoftware.hyperiot.mail.util.MailUtil;

/**
 * @author Aristide Cittadino Implementation class of the MailSystemApi
 * interface. This class is used to implements all additional methods to
 * interact with the persistence layer.
 */
@Component(service = MailSystemApi.class, immediate = true)
public final class MailSystemServiceImpl extends HyperIoTBaseEntitySystemServiceImpl<MailTemplate>
    implements MailSystemApi {

    /**
     * Mail Template engine
     */
    private Configuration freemarkerConf;

    /**
     * Freemarker Template loader
     */
    private StringTemplateLoader templateLoader;

    /**
     * Injecting the MailRepository to interact with persistence layer
     */
    private MailRepository repository;

    /**
     * Constructor for a MailSystemServiceImpl
     */
    public MailSystemServiceImpl() {
        super(MailTemplate.class);
    }

    /**
     * Return the current repository
     */
    protected MailRepository getRepository() {
        getLog().debug( "invoking getRepository, returning: {}", this.repository);
        return repository;
    }

    /**
     * @param mailRepository The current value of MailRepository to interact with
     *                       persistence layer
     */
    @Reference
    protected void setRepository(MailRepository mailRepository) {
        getLog().debug( "invoking setRepository, setting:{}", mailRepository);
        this.repository = mailRepository;
    }

    @Activate
    public void init() {
        freemarkerConf = new Configuration(Configuration.VERSION_2_3_28);
        freemarkerConf.setDefaultEncoding("UTF-8");
        freemarkerConf.setLocale(Locale.US);
        freemarkerConf.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        reloadCustomTemplates();
    }

    private void reloadCustomTemplates() {
        templateLoader = new StringTemplateLoader();
        Collection<MailTemplate> templates = findAll(null, null);
        for (MailTemplate t : templates) {
            templateLoader.putTemplate(t.getName(), t.getContent());
        }
        freemarkerConf.setTemplateLoader(templateLoader);
    }

    @Override
    public void sendMail(String from, List<String> recipients, List<String> ccRecipients,
                         List<String> bccRecipients, String subject, String content, List<byte[]> attachments) {
        try {
            Message m = this.createMimeMessage(from, recipients, ccRecipients, bccRecipients,
                subject, content, attachments);
            sendMail(m);
        } catch (Throwable e) {
            getLog().error( e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String generateTextFromStringTemplate(String templateText, HashMap<String, Object> params) {
        final String templateName = "custom";
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate(templateName, templateText);

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setTemplateLoader(stringLoader);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        Template template = null;
        try {
            template = cfg.getTemplate(templateName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // For the sake of example, also write output into a file:
        Writer stringWriter = new StringWriter();
        try {
            template.process(params, stringWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            getLog().error( e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                stringWriter.close();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public String generateTextFromTemplate(String templateName, HashMap<String, Object> params) throws IOException {
        Template template = freemarkerConf.getTemplate(templateName);
        // For the sake of example, also write output into a file:
        Writer stringWriter = new StringWriter();
        try {
            template.process(params, stringWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            getLog().error( e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            stringWriter.close();
        }
    }

    public Session getMailSession() {
        // creates a new session with an authenticator
        String username = MailUtil.getUsername();
        String password = MailUtil.getPassword();
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        Properties props = new Properties();
        props.put("mail.smtp.host", MailUtil.getSmtpHostname());
        props.put("mail.smtp.port", MailUtil.getSmtpPort());
        props.put("mail.smtp.auth", MailUtil.getSmtpAuth());
        props.put("mail.smtp.starttls.enable", MailUtil.getStartTTLSEnabled());
        Session session = Session.getInstance(props, auth);
        return session;
    }

    private Message createMimeMessage(String from, List<String> toAddressesList,
                                      List<String> ccAddressesList, List<String> bccAddressesList, String subject,
                                      String content, List<byte[]> attachFiles) throws MessagingException {
        Message msg = new MimeMessage(this.getMailSession());

        msg.setFrom(new InternetAddress(from));
        InternetAddress[] toAddresses = new InternetAddress[toAddressesList.size()];
        toAddressesList.stream().map(addressStr -> {
            try {
                return new InternetAddress(addressStr);
            } catch (AddressException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList()).toArray(toAddresses);
        msg.setRecipients(Message.RecipientType.TO, toAddresses);

        if (ccAddressesList != null) {
            InternetAddress[] ccAddresses = new InternetAddress[ccAddressesList.size()];
            ccAddressesList.stream().map(addressStr -> {
                try {
                    return new InternetAddress(addressStr);
                } catch (AddressException e) {
                    // TODO Auto-generated catch block
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()).toArray(ccAddresses);
            msg.setRecipients(Message.RecipientType.CC, ccAddresses);
        }

        if (bccAddressesList != null) {
            InternetAddress[] bccAddresses = new InternetAddress[bccAddressesList.size()];
            bccAddressesList.stream().map(addressStr -> {
                try {
                    return new InternetAddress(addressStr);
                } catch (AddressException e) {
                    // TODO Auto-generated catch block
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()).toArray(bccAddresses);
            msg.setRecipients(Message.RecipientType.BCC, bccAddresses);
        }

        msg.setSubject(subject);
        msg.setSentDate(new Date());

        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(content, "text/html");

        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // adds attachments
        if (attachFiles != null && attachFiles.size() > 0) {
            // TO DO: manage attachments from bytes
//
//			for (String filePath : attachFiles) {
//				MimeBodyPart attachPart = new MimeBodyPart();
//
//				try {
//					attachPart.attachFile(filePath);
//				} catch (IOException ex) {
//					ex.printStackTrace();
//				}
//
//				multipart.addBodyPart(attachPart);
//			}
        }

        // sets the multi-part as e-mail's content
        msg.setContent(multipart);
        return msg;
    }

    private void sendMail(Message message) throws MessagingException {
        Transport.send(message);
    }

    @Override
    public void createTemplate(Bundle bundle,String templatePath) {
        this.repository.createTemplate(bundle,templatePath);
    }
}
