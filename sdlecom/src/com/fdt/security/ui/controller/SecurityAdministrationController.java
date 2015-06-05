package com.fdt.security.ui.controller;

import static com.fdt.security.ui.SecurityViewConstants.SECURITY_VIEW_LOGGED_IN_USERS;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.jmesa.model.TableModel;
import org.jmesa.util.ItemUtils;
import org.jmesa.view.component.Row;
import org.jmesa.view.component.Table;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;
import org.jmesa.view.html.editor.HtmlCellEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.security.entity.User;
import com.fdt.security.spring.SDLHttpSessionEventPublisher;
import com.fdt.security.ui.form.LoggedinUserForm;

@Controller
public class SecurityAdministrationController extends AbstractBaseController {

	@Autowired
	private SessionRegistry sessionRegistry;

	@RequestMapping(value="/admin/viewLoggedInUsers.admin")
	public ModelAndView viewLoggedInUsers(HttpServletRequest request) throws Exception {
		ModelAndView modelAndView = this.getModelAndView(request, SECURITY_VIEW_LOGGED_IN_USERS);
		String activeUsers = getActiveUserList(request);
		modelAndView.addObject("activeUsers", activeUsers);
		return modelAndView;
	}

	@RequestMapping(value="/admin/viewLoggedInUsersList.admin")
	public String viewLoggedInUsersList(HttpServletRequest request,  HttpServletResponse response) throws IOException {
		String activeUsers =  null;
		activeUsers = getActiveUserList(request);
		byte[] contents = activeUsers.getBytes();
		response.getOutputStream().write(contents);
		return null;
	}

	private String getActiveUserList (HttpServletRequest request) {
		Map<User, Date> loggedInUsersMap = new HashMap<User, Date>();
		List<LoggedinUserForm> loggedinUsers = new LinkedList<LoggedinUserForm>();
		for(Object principal: sessionRegistry.getAllPrincipals()) {
			for(SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
				if(loggedInUsersMap.get(principal) == null) {
					loggedInUsersMap.put((User)principal, session.getLastRequest());
					LoggedinUserForm loggedinUserForm = new LoggedinUserForm();
					loggedinUserForm.setUser((User)principal);
					loggedinUserForm.setSessionId(session.getSessionId());
					loggedinUserForm.setLastRequestTime(session.getLastRequest());
					loggedinUserForm.setLastRequestTime(session.getLastRequest());
					loggedinUsers.add(loggedinUserForm);
				} else {
					Date prevLastRequest = loggedInUsersMap.get(principal);
					if(session.getLastRequest().after(prevLastRequest)) {
						loggedInUsersMap.put((User)principal, session.getLastRequest());
						LoggedinUserForm loggedinUserForm = new LoggedinUserForm();
						loggedinUserForm.setUser((User)principal);
						loggedinUserForm.setSessionId(session.getSessionId());
						loggedinUserForm.setLastRequestTime(session.getLastRequest());
						loggedinUsers.add(loggedinUserForm);
					}
				}
			}
		}
		TableModel tableModel = new TableModel("webTransactions", request);
		tableModel.setItems(loggedinUsers);
		tableModel.setExportTypes();
		tableModel.setStateAttr("restore");
		Table table = new Table();
		Row row = new Row();
		table.setRow(row);
		tableModel.setTable(getActiveUsersHtmlExportTable());
		String webTransactionHTML = tableModel.render();
		return webTransactionHTML;
    }


	 private Table getActiveUsersHtmlExportTable() {
		 	HtmlTable table = new HtmlTable();
		 	HtmlRow row = new HtmlRow();
			table.setRow(row);
			HtmlColumn userName = new HtmlColumn("user.username").title("User Name");
			userName.setFilterable(false);
			row.addColumn(userName);
			HtmlColumn currentLoggedInTime = new HtmlColumn("user.currentLoginTime").title("Current Logged In Time");
			currentLoggedInTime.setCellEditor(new CellEditor() {
	            public Object getValue(Object item, String property, int rowcount) {
	            	Object lastRequestTime = new HtmlCellEditor().getValue(item, "lastRequestTime", rowcount);
	                String strLastRequestTime = lastRequestTime.toString();
	                return strLastRequestTime;
	            }
	        });
			currentLoggedInTime.setFilterable(false);
			row.addColumn(currentLoggedInTime);
			HtmlColumn siteName = new HtmlColumn().title("Actions");
			siteName.setCellEditor(new CellEditor() {
	            public Object getValue(Object item, String property, int rowcount) {
	            	Object sessionId = new HtmlCellEditor().getValue(item, "sessionId", rowcount);
	                Object userName = new HtmlCellEditor().getValue(item, "user.username", rowcount);
	                String actionLinks = "<button type='button' class='logoff' title='Logoff User' value='sessionId=" + sessionId.toString() + "'>Logoff User</button><button type='button' class='locklogoff' title='Lock and Logoff User' value='sessionId=" + sessionId.toString() + "&username=" + userName + "'>Lock & Logoff User</button>";
	                return actionLinks;
	            }
	        });
			siteName.setFilterable(false);
			row.addColumn(siteName);

			return table;
		}

	@RequestMapping(value="/admin/logoutUser.admin", method=RequestMethod.GET,  produces="application/json")
	@ResponseBody
	@SuppressWarnings("unchecked")
	public Boolean logoutUsers(HttpServletRequest request, @RequestParam("sessionId") String sessionId) {
		boolean status = false;
		try {
			sessionRegistry.removeSessionInformation(sessionId);
			ServletContext servletContext = request.getServletContext();
			Map<String, HttpSession> activeUsers = (Map<String, HttpSession>)servletContext.getAttribute
				(SDLHttpSessionEventPublisher.ACTIVE_USERS);
	        HttpSession session = (HttpSession)activeUsers.get(sessionId);
	        session.invalidate();
			status = true;
		} catch (Exception e) {
			status = false;
		}
		return status;
	}

	@RequestMapping(value="/admin/lockandLogoutUser.admin", method=RequestMethod.GET,  produces="application/json")
	@ResponseBody
	@SuppressWarnings("unchecked")
	public Boolean lockandLogoutUser(HttpServletRequest request,
									@RequestParam String username,
									@RequestParam String sessionId,
									@RequestParam(defaultValue="false", required=false) String sendnotification,
									@RequestParam(required=false) String comments) {
		boolean status = false;
		boolean isSendnotification = false;
		try {
			if (sendnotification.equalsIgnoreCase("true")) {
				isSendnotification = true;
			}
			String modifiedBy = request.getRemoteUser();
			if(StringUtils.isBlank(modifiedBy)) {
				modifiedBy = "Administrator";
			}
			this.getService().lockUnLockUser(username, true, modifiedBy, isSendnotification, this.nodeName,
					comments);
			sessionRegistry.removeSessionInformation(sessionId);
			ServletContext servletContext = request.getServletContext();
	        Map<String, HttpSession> activeUsers = (Map<String, HttpSession>)servletContext.getAttribute
	        	(SDLHttpSessionEventPublisher.ACTIVE_USERS);
	        HttpSession session = (HttpSession)activeUsers.get(sessionId);
	        session.invalidate();
			status = true;
		} catch (Exception e) {
			status = false;
		}
		return status;
	}
}