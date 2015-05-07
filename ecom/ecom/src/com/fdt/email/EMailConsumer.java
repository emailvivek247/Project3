package com.fdt.email;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;
import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.JmsUtils;

import com.fdt.email.dto.EMailDTO;

import freemarker.template.TemplateException;

public class EMailConsumer implements SessionAwareMessageListener  {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EMailUtil eMailUtil;

    public void onMessage(Message message, Session session) {
        ObjectMessage objectMessage = (ObjectMessage)message;
        EMailDTO emailDTO;
        try {
            emailDTO = (EMailDTO)objectMessage.getObject();
            if (emailDTO.getEmailTemplateName() != null) {
                eMailUtil.sendMailUsingTemplate(emailDTO.getFromEmailId(), emailDTO.getToEmailId(), emailDTO.getSubject(),
                    emailDTO.getEmailTemplateName(), emailDTO.getMapData());
            } else {
                eMailUtil.sendTextMessage(emailDTO.getFromEmailId(), emailDTO.getToEmailId(), emailDTO.getSubject(),
                    emailDTO.getText());
            }
        } catch (MessagingException messageException) {
            logger.error(NOTIFY_ADMIN, "Error in Sending Email " + messageException);
            throw new RuntimeException(messageException);
        } catch (IOException ioException) {
            logger.error(NOTIFY_ADMIN, "Error in reading the EMail Templates " + ioException);
            throw new RuntimeException(ioException);
        } catch (TemplateException templateException) {
            logger.error(NOTIFY_ADMIN, "Error in reading the Templates " + templateException);
            throw new RuntimeException(templateException);
        } catch (JMSException jmsException) {
            logger.error(NOTIFY_ADMIN, "JMS Exception " + jmsException);
            throw JmsUtils.convertJmsAccessException(jmsException);
        } catch (RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "RuntimeException " + runtimeException);
            throw runtimeException;
        }
    }
}
