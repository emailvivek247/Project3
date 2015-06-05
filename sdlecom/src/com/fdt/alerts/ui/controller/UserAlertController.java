package com.fdt.alerts.ui.controller;

import static com.fdt.alerts.ui.AlertsViewConstants.ALERTS_GET_USER_ALERTS;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmesa.model.TableModel;
import org.jmesa.view.component.Row;
import org.jmesa.view.component.Table;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.editor.DateCellEditor;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;
import org.jmesa.view.html.editor.HtmlCellEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.alerts.entity.UserAlert;
import com.fdt.common.entity.ErrorCode;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.security.entity.User;
import com.fdt.security.exception.DuplicateAlertException;
import com.fdt.security.exception.MaximumNumberOfAlertsReachedException;
import com.fdt.security.exception.UserNameNotFoundException;

@Controller
public class UserAlertController extends AbstractBaseController {

	@RequestMapping(value="/createAlert.admin", produces="application/json")
	@ResponseBody
	public List<ErrorCode> createAlert(HttpServletRequest request,
												@RequestParam("alertName") String alertName,
												@RequestParam("templateName") String templateName,
												@RequestParam("indexName") String indexName,
												@RequestParam("alertQuery") String alertQuery,
												@RequestParam("comments") String comments) {
		List<ErrorCode> errors = new LinkedList<ErrorCode>();
		UserAlert userAlert = new UserAlert();
		userAlert.setAlertName(alertName);
		userAlert.setTemplateName(templateName);
		userAlert.setIndexName(indexName);
		userAlert.setAlertQuery(alertQuery);
		userAlert.setUsername(request.getRemoteUser());
		userAlert.setBaseURL(this.ecomClientURL);
		userAlert.setNodeName(this.nodeName);
		userAlert.setActive(Boolean.TRUE);
		userAlert.setComments(comments);
		userAlert.setSiteName(this.nodeName);
		if (comments.length() > 250) {
			ErrorCode error = new ErrorCode();
			error.setCode("ERROR");
			error.setDescription(this.getMessage("user.alert.commentsLength") + comments.length() + " " + this.getMessage("user.alert.commentsLengthCharacters"));
			errors.add(error);
			return errors;
		}
		try {
			this.getService().saveUserAlert(userAlert);
			ErrorCode error = new ErrorCode();
			error.setCode("SUCCESS");
			error.setDescription(this.getMessage("user.alert.createdSuccessfully"));
			errors.add(error);
		} catch (DuplicateAlertException duplicateAlertException) {
			ErrorCode error = new ErrorCode();
			error.setCode("ERROR");
			error.setDescription(duplicateAlertException.getDescription());
			errors.add(error);
		} catch (UserNameNotFoundException userNameNotFoundException) {
			ErrorCode error = new ErrorCode();
			error.setCode("ERROR");
			error.setDescription(userNameNotFoundException.getDescription());
			errors.add(error);
		} catch (MaximumNumberOfAlertsReachedException e) {
			ErrorCode error = new ErrorCode();
			error.setCode("ERROR");
			error.setDescription(e.getMessage());
			errors.add(error);
		}
		return errors;
	}

	@RequestMapping(value="/getUserAlerts.admin", method=RequestMethod.GET)
	public ModelAndView getUserAlerts(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, ALERTS_GET_USER_ALERTS);
		User user = this.getUser(request);
		String userAlerts = null;
		userAlerts = getUserAlertAjax(request);
		modelAndView.addObject("user", user);
		modelAndView.addObject("userAlerts", userAlerts);
		return modelAndView;
	}

	@RequestMapping(value="/viewUserAlerts.admin")
	public String viewUserAlerts(HttpServletRequest request,
												HttpServletResponse response) throws IOException {
		String userAlerts =  null;
		userAlerts = getUserAlertAjax(request);
		byte[] contents = userAlerts.getBytes();
		response.getOutputStream().write(contents);
		return null;
	}

	 private String getUserAlertAjax(HttpServletRequest request) {
		List<UserAlert> userAlerts =  null;
		userAlerts = this.getService().getUserAlertsByUserName(request.getRemoteUser(), this.nodeName);
		if(userAlerts == null || userAlerts.size() == 0){
			userAlerts = new LinkedList<UserAlert>();
		}
		TableModel tableModel = new TableModel("recurringTransactions", request);
		tableModel.setItems(userAlerts);
		tableModel.setExportTypes();
		tableModel.setStateAttr("restore");
		Table table = new Table();
		Row row = new Row();
		table.setRow(row);
		tableModel.setTable(getUsersAlertsHtmlExportTable(request));
		String webTransactionHTML = tableModel.render();
		return webTransactionHTML;
	 }

	 private Table getUsersAlertsHtmlExportTable(HttpServletRequest request) {
		Table table = new HtmlTable();
		Row row = new HtmlRow();
		table.setRow(row);
		HtmlColumn siteName = new HtmlColumn("siteName").title("Site Name");
		siteName.setFilterable(false);
		row.addColumn(siteName);
		HtmlColumn alertName = new HtmlColumn("alertName").title("Alert Name");
		alertName.setFilterable(false);
		row.addColumn(alertName);
		HtmlColumn alertQuery = new HtmlColumn("alertQuery").title("Criteria Specified For Alert");
		alertQuery.setFilterable(false);
		row.addColumn(alertQuery);
		HtmlColumn comments = new HtmlColumn("comments").title("Comments");
		comments.setFilterable(false);
		row.addColumn(comments);
		HtmlColumn createdDate = new HtmlColumn("createdDate").title("Created On");
		createdDate.setFilterable(false);
		createdDate.setCellEditor(new DateCellEditor("MM-dd-yyyy HH:mm:ss z"));
		row.addColumn(createdDate);
		HtmlColumn action = new HtmlColumn().title("Action");
		action.setFilterable(false);
		action.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
            	Object id = new HtmlCellEditor().getValue(item, "id", rowcount);
            	String cValue = "<a class='deleteAlert' href='deleteUserAlert.admin?id=" + id.toString() + "'><img title='Delete Alert' src='resources/images/remove.gif'></a>";
            	return cValue;
            }
        });
		row.addColumn(action);
		return table;
	}

}
