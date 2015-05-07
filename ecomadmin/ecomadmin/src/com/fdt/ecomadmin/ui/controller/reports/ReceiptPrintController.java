package com.fdt.ecomadmin.ui.controller.reports;

import static com.fdt.ecomadmin.ui.controller.ReportConstants.RECEIPT_RPT;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.RECEIPT_REPORT;

import java.io.File;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.controller.AbstractBaseController;

@Controller
public class ReceiptPrintController extends AbstractBaseController {

	@Value("${email.useractivation.requesturl}")
	protected String requestURL = null;

	@Autowired
	protected DataSource datasource = null;

	@RequestMapping(value="/viewReceipt.admin")
	public ModelAndView viewReceipt(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, RECEIPT_REPORT);
		modelAndView.addObject("displayFlag", false);
		return modelAndView;
	}

	@RequestMapping(value="/submitReceiptForm.admin")
	public ModelAndView submitReceiptForm(HttpServletRequest request,
											 @RequestParam(required = false) String txRefNum,
											 @CookieValue("JSESSIONID") String sessionId) {
		Long siteId = null;
		return this.populateReceiptView(request, RECEIPT_REPORT, siteId, txRefNum, sessionId);

	}

	@RequestMapping(value="/renderReceipt.admin", method = RequestMethod.POST)
	public ModelAndView renderReceipt(HttpServletRequest request,	HttpServletResponse response,
											@RequestParam(required = false) Long siteId,
											@RequestParam(required = false) String txRefNum) throws Exception {
		Connection reportConn = null;
		String reportName = RECEIPT_RPT;
		try	{
			ServletContext servletContext = this.getServletContext();
			File reportFile = null;
			JasperReport jasperReport = null;
			Map<String, Object> reportParameters = new HashMap<String, Object>();
			JasperPrint jasperPrint = null;
			ServletOutputStream ouputStream = null;
			ObjectOutputStream oos = null;

			reportFile = new File(servletContext.getRealPath(reportName));

			if (!reportFile.exists()) {
				throw new JRRuntimeException("The report design must be compiled first.");
			}
			reportParameters.put("REQUEST_URL", this.requestURL);
			reportParameters.put("BaseDir", reportFile.getParentFile());
			reportParameters.put("SITE_ID", siteId);
			reportParameters.put("TX_REF_NUM", txRefNum);
			reportConn = this.datasource.getConnection();

			jasperReport = (JasperReport) JRLoader.loadObjectFromFile(reportFile.getPath());
 			jasperPrint  =  JasperFillManager.fillReport(jasperReport, reportParameters, reportConn);
			if (jasperPrint != null) {
				response.setContentType("application/octet-stream");
				ouputStream = response.getOutputStream();
				oos = new ObjectOutputStream(ouputStream);
				oos.writeObject(jasperPrint);
				oos.flush();
				oos.close();
				ouputStream.flush();
				ouputStream.close();
			}
		} catch (Exception exception) {
			logger.error("Error Occured When Rendering the Applet Report",  exception);
			throw exception;
		} finally {
			try {
				if (reportConn != null) {
					reportConn.close();
				}
			} catch (SQLException sQLException) {
				logger.error("Error Occured in Closing the Connection in the Applet Report",  sQLException);
			}
		}
		return null;
	}

	private ModelAndView populateReceiptView(HttpServletRequest request, String viewName, Long siteId, String txRefNum,
			String sessionId) {
		ModelAndView modelAndView = this.getModelAndView(request, viewName);
		modelAndView.addObject("siteId", siteId);
		modelAndView.addObject("txRefNum", txRefNum);
		modelAndView.addObject("sessionId", sessionId);
		modelAndView.addObject("displayFlag", true);
		modelAndView.addObject("reportURL", requestURL);
		return modelAndView;
	}
}