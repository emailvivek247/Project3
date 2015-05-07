package com.fdt.email;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.fdt.alerts.dto.AlertNameHyperLinkKeyMap;
import com.fdt.alerts.entity.UserAlert;
import com.fdt.common.util.SystemUtil;
import com.fdt.email.dto.EMailDTO;
import com.fdt.security.entity.UserEvent;

@Service
public class EmailProducer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired(required=false)
    private JmsTemplate jmsTemplate;

    @Autowired(required=false)
    private Queue destinationQueue;

    @Value("${ecommerce.serverurl}")
    private String ecomServerURL = null;

    public void sendMailUsingTemplate(UserEvent userEvent, String password) {
        String hyperLink = userEvent.getControllerURL() + SystemUtil.encrypt(userEvent.getToken()) + "&userName="
            + SystemUtil.encrypt(userEvent.getUser().getUsername());
        Map<String, Object> data =  new HashMap<String, Object>();
        data.put("activationLink", hyperLink);
        data.put("currentDate", new Date());
        data.put("userFirstName", userEvent.getUser().getFirstName());
        data.put("userLastName", userEvent.getUser().getLastName());
        data.put("serverUrl", this.ecomServerURL);
        if(!StringUtils.isBlank(password)){
            data.put("password",  password);
        }
        this.sendMailUsingTemplate(userEvent.getFromEMailAddress(), userEvent.getUser().getUsername(), userEvent.getSubject(),
                userEvent.getEmailTemplateFile(), data);
    }


    public void sendEmail(String fromEmailId, String toEmailId, String subject, String txtMessage) {
        Map<String, Object> data = new HashMap<String, Object>();
        EMailDTO emailDTO = new EMailDTO();
        emailDTO.setFromEmailId(fromEmailId);
        emailDTO.setToEmailId(toEmailId);
        emailDTO.setSubject(subject);
        emailDTO.setText(txtMessage);
        emailDTO.setMapData(data);
        this.sendMessageToQueue(emailDTO);
    }

    public void sendEmail(EMailDTO eMailDTO, List<AlertNameHyperLinkKeyMap> alertNameHyperLinkKeyMap) {
        Map<String, UserAlert> linkMap = new HashMap<String, UserAlert>();
        for(AlertNameHyperLinkKeyMap alertNameHyperLink: alertNameHyperLinkKeyMap) {
            linkMap.put(alertNameHyperLink.getKey(), alertNameHyperLink.getValue());
        }
        Map<String, Object> dataMap = eMailDTO.getMapData();
        dataMap.put("currentDate", new Date());
        dataMap.put("linkMap", linkMap);
        dataMap.put("serverUrl", this.ecomServerURL);
        eMailDTO.setMapData(dataMap);
        this.sendMessageToQueue(eMailDTO);
    }

    /*
     * This is the final method for sending emails
     *
     */
    public void sendMailUsingTemplate(String fromEmailId, String toEmailId, String subject, String emailTemplateName,
        Map<String, Object> data) {
        data.put("fromEmailAddress", fromEmailId);
        data.put("currentDate", new Date());
        data.put("serverUrl", this.ecomServerURL);
        EMailDTO emailDTO = new EMailDTO();
        emailDTO.setFromEmailId(fromEmailId);
        emailDTO.setToEmailId(toEmailId);
        emailDTO.setSubject(subject);
        emailDTO.setEmailTemplateName(emailTemplateName);
        emailDTO.setMapData(data);
        this.sendMessageToQueue(emailDTO);
    }

    private void sendMessageToQueue(final EMailDTO emailDTO) {
        logger.debug("Coming Inside sendMessageToQueue");
        this.jmsTemplate.send(this.destinationQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(emailDTO);
            }
        });
    }
}
