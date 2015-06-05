/*
 * Copyright 2004.
 */

package net.javacoding.xsearch.utility;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.ConfigConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Action helpers.
 *
 * @author Michael Jouravlev, 2003-2004
 */
public class ActionTools {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.utility.ActionTools");

    /**
     * Updates ActionForward object with URL parameters.
     *
     * @param actionMapping  action mapping object
     * @param forwardName    mapping name
     * @param urlParams      array of "key=value" strings which
     *                       should be added to actionForward path
     *                       as HTTP GET parameters
     *
     * @return modified ActionForward object with updated GET parameters
     */
    public static ActionForward goForward(ActionMapping actionMapping,
                                          String forwardName,
                                          String[] urlParams) {

        /*
         * Find ActionForward object, defined in struts-config.xml
         */
        ActionForward actionForward = actionMapping.findForward(forwardName);
        if (actionForward == null) return null;

        /*
         * Do not use URL modification on forward,
         * ActionForm fields should be used instead.
         */
        if (!actionForward.getRedirect()) return actionForward;

        /*
         * Build URL parameters necessary on redirect because
         * HTTPRequest object will be destroyed, ActionForm fields
         * can be reset as well if form scope is "request".
         */
        StringBuffer actionPath = new StringBuffer(actionForward.getPath());
        if (actionForward.getPath() != null) {
            for (int i = 0; i < urlParams.length; i++) {
                actionPath.append(i==0 ? "?" : "&");
                actionPath.append(urlParams[i]);
            }
        }

        /*
         * Create new ActionForward object. Stuts does not
         * allow to modify ActionForward objects, statically
         * defined in struts-config.xml
         */
        ActionForward actionRedirect =
                new ActionForward(actionForward.getName(),
                                  actionPath.toString(),
                                  true /* REDIRECT */
                );

        return actionRedirect;
    }

    /**
     * Saves the specified error messages keys into the appropriate request
     * attribute for use by the &lt;html:errors&gt; tag, if any messages
     * are required. Otherwise, ensure that the errors request attribute is
     * not created.
     *
     * This method is taken directly from Action.java class, so it can be
     * used not only in actions, but in forms as well.
     *
     * @param request The servlet request we are processing
     * @param errors  Error messages object
     */
    public static void saveErrors(HttpServletRequest request,
                                  ActionErrors errors) {
        /*
         * Remove any error messages attribute if none are required
         */
        if ((errors == null) || errors.isEmpty()) {
            request.removeAttribute(Globals.ERROR_KEY);
        } else {
            ActionErrors requestErrors =
              (ActionErrors) request.getAttribute(Globals.ERROR_KEY);

            /*
             * Save the error messages we need
             */
            if (requestErrors != null) {
                requestErrors.add(errors);
            } else {
                request.setAttribute(Globals.ERROR_KEY, errors);
            }
        }
    }

    /**
     * <p>Save the specified error messages keys into the appropriate session
     * attribute for use by the &lt;html:errors&gt; tag, if any messages
     * are required. Otherwise, ensure that the session attribute is not
     * created.</p>
     *
     * @param session The servlet session we are processing
     * @param errors Error messages object
     */
    public static void saveErrors(HttpSession session, ActionMessages errors) {

        // Remove any error messages attribute if none are required
        if ((errors == null) || errors.isEmpty()) {
            session.removeAttribute(Globals.ERROR_KEY);
            return;
        }

        // Save the error messages we need
        session.setAttribute(Globals.ERROR_KEY, errors);

    }

    /**
     * Retrieves any existing messages placed in the session by previous actions.  This method could be called instead
     * of creating a <code>new ActionMessages()<code> at the beginning of an <code>Action<code>
     * This will prevent saveMessages() from wiping out any existing Messages
     *
     * @return the Messages that already exist in the session, or a new ActionMessages object if empty.
     * @param session The servlet session we are processing
     */
    public static ActionMessages getMessages(HttpSession session) {
        ActionMessages messages =
                (ActionMessages) session.getAttribute(Globals.MESSAGE_KEY);
        // Don't want to keep messages in the session
        session.removeAttribute(Globals.MESSAGE_KEY);

        if (messages == null) {
            messages = new ActionMessages();
        }
        return messages;
    }

    /**
     * Retrieves any existing errors placed in the session by previous actions.  This method could be called instead
     * of creating a <code>new ActionMessages()<code> at the beginning of an <code>Action<code>
     * This will prevent saveErrors() from wiping out any existing Errors
     *
     * @return the Errors that already exist in the session, or a new ActionMessages object if empty.
     * @param session The servlet session we are processing
     */
    public static ActionMessages getErrors(HttpSession session) {
        ActionMessages errors =
            (ActionMessages) session.getAttribute(Globals.ERROR_KEY);
        // Don't want to keep errors in the session
        session.removeAttribute(Globals.ERROR_KEY);

        if (errors == null) {
            errors = new ActionMessages();
        }
        return errors;
    }

    // ----------------------------------------------------- Validation Methods

    public static boolean validateTemplateName(String name, ActionMessages errors) {
        if (name == null || "".equals(name)) {
            errors.add("error", new ActionMessage("errors.required", "Template name"));
            return false;
        }
        if (!name.matches("[\\w\\-_\\.]+")) {
            errors.add("error", new ActionMessage("errors.templatename.pattern"));
            return false;
        }
        if (name.length() > 16) {
            errors.add("error", new ActionMessage("errors.templatename.length"));
            return false;
        }

        return true;
    }
    
    public static ActionMessage getMessage(int msgId, Object name0, Object value0) {
        if (msgId == ConfigConstants.ERROR_INDEXNAME_REQUIRED)
            return new ActionMessage("errors.required", name0);
        else if (msgId == ConfigConstants.ERROR_INDEXNAME_PATTERN)
            return new ActionMessage("errors.indexname.pattern1");
        else if (msgId == ConfigConstants.ERROR_INDEXNAME_LENGTH)
            return new ActionMessage("errors.indexname.length");
        else if (msgId == ConfigConstants.ERROR_INDEXNAME_DUPLICATE)
            return new ActionMessage("errors.indexname.duplicate", value0);
        else return null;
    }
    
}
