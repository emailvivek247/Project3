package com.fdt.security.spring;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.security.web.session.HttpSessionEventPublisher;

public class SDLHttpSessionEventPublisher extends HttpSessionEventPublisher {
	
	public static String ACTIVE_USERS = "ACTIVE_USERS";
    /**
     * Handles the HttpSessionEvent by publishing a {@link HttpSessionCreatedEvent} to the application
     * appContext.
     *
     * @param event HttpSessionEvent passed in by the container
     */
	@SuppressWarnings("unchecked")
    public void sessionCreated(HttpSessionEvent event) {
    	HttpSession session = event.getSession();  
        ServletContext context = session.getServletContext();
        Map<String, HttpSession> activeUsers = (Map<String, HttpSession>)context.getAttribute(ACTIVE_USERS);
        if (activeUsers == null) {
        	activeUsers =  new HashMap<String, HttpSession>();
        	context.setAttribute(ACTIVE_USERS, activeUsers);
        }
        activeUsers.put(session.getId(), session);
        super.sessionCreated(event);
    }

    /**
     * Handles the HttpSessionEvent by publishing a {@link HttpSessionDestroyedEvent} to the application
     * appContext.
     *
     * @param event The HttpSessionEvent pass in by the container
     */
    @SuppressWarnings("unchecked")
    public void sessionDestroyed(HttpSessionEvent event) {
    	HttpSession session = event.getSession();  
        ServletContext context = session.getServletContext();
        
		Map<String, HttpSession> activeUsers = (Map<String, HttpSession>)context.getAttribute(ACTIVE_USERS);
		 if (activeUsers != null) {
			 activeUsers.remove(session.getId());
		 }
    	super.sessionDestroyed(event);
    }
}
