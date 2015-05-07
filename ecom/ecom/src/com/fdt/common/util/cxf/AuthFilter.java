package com.fdt.common.util.cxf;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author smani
 *
 * This class Acts as an Filter to Authenticate incoming REST Calls. It usess the Header  "USERNAME" and "PASSWORD" to get
 * the user Name and Password to authenticate the incoming request.
 *
 */
public class AuthFilter implements ContainerRequestFilter {

    @Value("${ecomadmin.webservice.username}")
    private String userName = null;

    @Value("${ecomadmin.webservice.password}")
    private String password = null;

    private static String USERNAME_HEADER = "USERNAME";

    private static String PASSWORD_HEADER = "PASSWORD";
    
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		MultivaluedMap<String, String> headers = requestContext.getHeaders();
		 
		List<String> userNameList = headers.get(USERNAME_HEADER);
		List<String> passwordList = headers.get(PASSWORD_HEADER);
		/** Check Whether the USERNAME and PASSWORD Headers are NULL **/
		if (userNameList == null  || passwordList == null) {
    	   requestContext.abortWith(Response
                   .status(Response.Status.FORBIDDEN)
                   .entity("Missing USERNAME_HEADER/PASSWORD_HEADER Token.")
                   .build());
		} else {
           /** Check Whether the USERNAME and PASSWORD Matches What is defined in the property Files **/
			String incomingUserName = userNameList.get(0);
			String incomingPassword = passwordList.get(0);
			if (!incomingUserName.equalsIgnoreCase(userName) || !incomingPassword.equalsIgnoreCase(password)) {
				requestContext.abortWith(Response
						.status(Response.Status.UNAUTHORIZED)
						.entity("Invalid UserName/Password.")
						.build());
           }
       }
	}    

}