package com.fdt.alerts.service;

import static javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING;

import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.BindingType;

import org.apache.cxf.annotations.WSDLDocumentation;
import org.springframework.beans.factory.annotation.Autowired;

import com.fdt.alerts.dto.AlertNameHyperLinkKeyMap;
import com.fdt.alerts.entity.UserAlert;
import com.fdt.ecom.entity.NodeConfiguration;
import com.fdt.email.dto.EMailDTO;

@WebService(endpointInterface = "com.fdt.alerts.service.AlertFacadeService", serviceName ="AlertFacadeService")
@BindingType(value = SOAP12HTTP_BINDING)
@WSDLDocumentation(value="ACCEPT SOAP 1.2 Services for Notifications", placement = WSDLDocumentation.Placement.TOP)
public class AlertFacadeServiceImpl implements AlertFacadeService {

    @Autowired
    private AlertService alertService = null;

    public List<UserAlert> getUserAlerts(String nodeName, int firstResult) {
        return this.alertService.getUserAlerts(nodeName, firstResult);
    }

    public void sendEmail(EMailDTO emailDTO, List<AlertNameHyperLinkKeyMap> alertNameHyperLinkKeyMap) {
        this.alertService.sendEmail(emailDTO, alertNameHyperLinkKeyMap);
    }

    public NodeConfiguration getNodeConfiguration(String nodeName) {
        return this.alertService.getNodeConfiguration(nodeName);
    }

}
