package com.fdt.alerts.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.fdt.alerts.dto.AlertNameHyperLinkKeyMap;
import com.fdt.alerts.entity.UserAlert;
import com.fdt.ecom.entity.NodeConfiguration;
import com.fdt.email.dto.EMailDTO;

@WebService
public interface AlertFacadeService {

    @WebMethod
    public List<UserAlert> getUserAlerts(@WebParam(name="nodeName") String nodeName,
                                         @WebParam(name="firstResult") int firstResult);
    @WebMethod
    public void sendEmail(@WebParam(name="emailDTO") EMailDTO emailDTO,
            @WebParam(name="alertNameHyperLinkKeyMap") List<AlertNameHyperLinkKeyMap> alertNameHyperLinkKeyMap);

    @WebMethod
    public NodeConfiguration getNodeConfiguration(@WebParam(name="nodeName") String nodeName);

}
