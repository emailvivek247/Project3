package com.fdt.email;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fdt.common.util.SystemUtil;
import com.fdt.email.dto.EComAdminEMailDTO;
import com.fdt.security.entity.EComAdminUserEvent;

@Service
public class EmailProducer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired(required=false)
    private JmsTemplate jmsTemplate;

    @Autowired(required=false)
    private Queue destinationQueue;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void sendMailUsingTemplate(final EComAdminUserEvent userEvent) {
        String hyperLink = userEvent.getControllerURL() + SystemUtil.encrypt(userEvent.getToken()) 	+ "&userName="	+
            		SystemUtil.encrypt(userEvent.getUser().getUsername());
        Map<String, Object> data =  new HashMap<String, Object>();
        data.put("activationLink", hyperLink);
        data.put("userFirstName", userEvent.getUser().getFirstName());
        data.put("userLastName", userEvent.getUser().getLastName());
        this.sendMailUsingTemplate(userEvent.getFromEMailAddress(), userEvent.getUser().getUsername(), userEvent.getSubject(),
                userEvent.getEmailTemplateFile(), data);
    }

    public void sendEmail(String fromEmailId, String toEmailId, String subject, String txtMessage) {
        Map<String, Object> data = new HashMap<String, Object>();
        EComAdminEMailDTO eComAdminEMailDTO = new EComAdminEMailDTO();
        eComAdminEMailDTO.setFromEmailId(fromEmailId);
        eComAdminEMailDTO.setToEmailId(toEmailId);
        eComAdminEMailDTO.setSubject(subject);
        eComAdminEMailDTO.setText(txtMessage);
        eComAdminEMailDTO.setMapData(data);
        this.sendMessageToQueue(eComAdminEMailDTO);
    }

    public void sendMailUsingTemplate(String fromEmailId, String toEmailId, String subject, String emailTemplateName,
        Map<String, Object> data) {
        EComAdminEMailDTO eComAdminEMailDTO = new EComAdminEMailDTO();
        eComAdminEMailDTO.setFromEmailId(fromEmailId);
        eComAdminEMailDTO.setToEmailId(toEmailId);
        eComAdminEMailDTO.setSubject(subject);
        eComAdminEMailDTO.setEmailTemplateName(emailTemplateName);
        eComAdminEMailDTO.setMapData(data);
        this.sendMessageToQueue(eComAdminEMailDTO);
    }

    private void sendMessageToQueue(final EComAdminEMailDTO emailDTO) {
        if (logger.isDebugEnabled()) {
            logger.debug("Coming Inside sendMessageToQueue To Send Messages To The Queue");
        }
        this.jmsTemplate.send(this.destinationQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(emailDTO);
            }
        });
    }
}
