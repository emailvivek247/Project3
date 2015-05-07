package com.fdt.email;

import java.io.IOException;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Service
public class EMailUtil {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**Stores the Mail Sender **/
    @Autowired
    private JavaMailSender mailSender;

    /**Stores the FreeMarker Configuration **/
    @Autowired
    private FreeMarkerConfigurationFactoryBean freemarkerConfiguration;

    private void sendMail(String fromEmailId, String toEmailId, String subject, String text, boolean isMime)
            throws MessagingException {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(fromEmailId);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setTo(toEmailId);
            mimeMessageHelper.setText(text, isMime);
            mailSender.send(mimeMessage);
        } catch (MessagingException messagingException) {
            logger.error("Error While Sending the E-Mail in sendMimeMessage", messagingException);
            throw messagingException;
        }
    }

    public void sendMailUsingTemplate(String fromEmailId, String toEmailId, String subject,
            String templateName, Map<String, Object> data) throws MessagingException, IOException, TemplateException {
        this.sendMail(fromEmailId, toEmailId, subject, this.getTextFromTemplate(templateName, data), true);
    }

    public void sendMimeMessage(String fromEmailId, String toEmailId, String subject, String text)
            throws MessagingException {
        this.sendMail(fromEmailId, toEmailId, subject, text, true);
    }

    public void sendTextMessage(String fromEmailId, String toEmailId, String subject, String text)
            throws MessagingException {
        this.sendMail(fromEmailId, toEmailId, subject, text, false);
    }

    private String getTextFromTemplate(String templateName, Map<String, Object> data) throws IOException, TemplateException {
        Configuration fc;
        String text = null;
        fc = freemarkerConfiguration.createConfiguration();
        Template template = fc.getTemplate(templateName);
        text = FreeMarkerTemplateUtils.processTemplateIntoString(template, data);
        return text;
    }
}
