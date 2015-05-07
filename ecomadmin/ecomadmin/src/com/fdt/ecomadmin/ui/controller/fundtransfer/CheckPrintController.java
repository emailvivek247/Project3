package com.fdt.ecomadmin.ui.controller.fundtransfer;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_FUNDS_TRANSFER_HISTORY;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_FUNDS_TRANSFER_PRINT_CHQ;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_FUNDS_TRANSFER;
import static com.fdt.ecomadmin.ui.controller.ReportConstants.CHECK_RPT;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.EXCEL;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.PDF;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_ACCESS_DENIED;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_CHECK_DETAILS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_CHECK_HISTORY;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_CHECK_PRINT;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

import org.jmesa.view.component.Column;
import org.jmesa.view.component.Row;
import org.jmesa.view.component.Table;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.editor.NumberCellEditor;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;
import org.jmesa.view.html.editor.HtmlCellEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.achtx.entity.CheckHistory;
import com.fdt.common.export.ExcelExport;
import com.fdt.common.export.PDFCell;
import com.fdt.common.export.PDFExport;
import com.fdt.common.ui.breadcrumbs.Link;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.enums.PaymentType;
import com.fdt.ecomadmin.ui.controller.util.ExportConstants;

@Controller
public class CheckPrintController extends AbstractBaseController {

	@Value("${email.useractivation.requesturl}")
	protected String requestURL = null;

	@Autowired
	protected DataSource datasource = null;

	private static int[] PDF_USER_COLUMN_WIDTHS = new int[] {8,13,8,9,9,7,11,20,20,8,8,10};

	@Link(label="View Check History", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/history_Check.admin")
	public ModelAndView viewCheckHistory(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, VIEW_CHECK_HISTORY, TOP_FUNDS_TRANSFER,
				SUB_FUNDS_TRANSFER_HISTORY);

		if (!this.isFeatureEnabledForUser(request, "CheckHistory")) {
			modelAndView.setViewName(REDIRECT_ACCESS_DENIED);
			return modelAndView;
		}

		List<Site> sites = this.getAssignedSites(request);

		modelAndView.addObject("sites", sites);
		return modelAndView;
	}


	@RequestMapping(value="/getCheckHistory.admin", produces="application/json")
    @ResponseBody
	public List<CheckHistory> getCheckHistory(HttpServletRequest request,
			@RequestParam(required = false) Long siteId,
			@RequestParam(required = false) String dateRange,
			@RequestParam(required = false) String checkNum,
			@RequestParam(required = false) Double checkAmt) {

		List<CheckHistory> checkHistory = null;
		if (!this.isFeatureEnabledForUser(request, "CheckHistory")) {
			return checkHistory;
		}

		if (siteId != null) {
			if (!checkLoggedinUserSiteValidity(request, siteId)) {
				return checkHistory;
			}
		}

		List<Site> sites = this.getAssignedSites(request);
		if ((siteId != null) || (dateRange != null && dateRange != "")) {
			if (!this.isInternalUser(request)) {
				for (Site site : sites) {
					siteId = site.getId();
				}
			}
			checkHistory = this.getServiceStub().getCheckHistories(siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), checkNum, checkAmt);
		}
		return checkHistory;
	}


	@RequestMapping(value="/viewcheckhistoryexport.admin", method=RequestMethod.GET,  produces="application/json")
    @ResponseBody
	public void getUsersExport(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(required = false) Long siteId,
			@RequestParam(required = false) String dateRange,
			@RequestParam(required = false) String siteName,
			@RequestParam(required = false) String checkNum,
			@RequestParam(required = false) Double checkAmt,
			@RequestParam(required = false) String exportType) {
		if (!this.isInternalUser(request)) {
			List<Site> sites = this.getAssignedSites(request);
			for (Site site : sites) {
				siteId = site.getId();
			}
		}
		List<CheckHistory> checkHistoryList = new LinkedList<CheckHistory>();
		if ((siteId != null) || (dateRange != null && dateRange != "")) {
			checkHistoryList = this.getServiceStub().getCheckHistories(siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), checkNum, checkAmt);
			if (checkHistoryList == null || checkHistoryList.size() == 0) {

			}
		}
		byte[] outputBytes = new byte[1];
        String fileExtension = "";
        try{
	        if(exportType.equals(EXCEL)){
	        	List<List<String>> checkHistoryRowList = new ArrayList<List<String>>();
	        	for(CheckHistory checkHistory : checkHistoryList){
	    			checkHistoryRowList.add(this.getCheckHistoryRowForExcel(checkHistory));
	    		}
	        	ExcelExport export = new ExcelExport();
	        	outputBytes = export.exportToExcel(ExportConstants.getCheckHistoryHeaders(), checkHistoryRowList);
	        	fileExtension = "xls";
	        } else if (exportType.equals(PDF)){
	        	// Create PDF file contents
	        	List<List<PDFCell>> checkHistoryRowList = new ArrayList<List<PDFCell>>();
	        	for(CheckHistory checkHistory : checkHistoryList){
	        		checkHistoryRowList.add(this.getCheckHistoryRowForPDF(checkHistory));
	    		}
	        	PDFExport export = new PDFExport(PDF_USER_COLUMN_WIDTHS.length, PDF_USER_COLUMN_WIDTHS);
	        	outputBytes = export.exportToPDF(ExportConstants.getCheckHistoryHeaders(), checkHistoryRowList);
	        	fileExtension = "pdf";
	        }
	        response.reset();
			response.resetBuffer();
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition",  "attachment; filename=CheckHistory." + fileExtension);
			response.setHeader("Expires", " 0");
			response.setHeader("Cache-Control", " must-revalidate, post-check=0, pre-check=0" );
			response.setHeader("Pragma" , "public");
			ByteArrayOutputStream outputStream =  new ByteArrayOutputStream();
			outputStream.write(outputBytes);
			outputStream.writeTo(response.getOutputStream());
			response.getOutputStream().flush();
        } catch(Exception e){
        	logger.error("Error while writing exporting find user " , e);
        }

    }

	private List<String> getCheckHistoryRowForExcel(CheckHistory checkHistory){
		List<String> columns = new ArrayList<String>();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");
		String createdDateText = checkHistory.getCreatedDate() != null ? df.format(checkHistory.getCreatedDate()) : "";
		columns.add(checkHistory.getCheckNumber() != null ? String.valueOf(checkHistory.getCheckNumber()) : "");
		columns.add(createdDateText);
		columns.add(checkHistory.getSiteName() != null ? StringUtils.capitalize(checkHistory.getSiteName()) : "");
		columns.add(checkHistory.getBankName() != null ? StringUtils.capitalize(checkHistory.getBankName()) : "");
		columns.add(checkHistory.getPaymentType() != null ? checkHistory.getPaymentType().toString() : "");
		columns.add(checkHistory.getTotalTransactions() != null ? String.valueOf(checkHistory.getTotalTransactions()) : "");
		columns.add(checkHistory.getMachineName() != null ? checkHistory.getMachineName() : "");
		columns.add(checkHistory.getModifiedBy() != null ? checkHistory.getModifiedBy() : "");
		columns.add(checkHistory.getComments() != null ? checkHistory.getComments() : "");
		columns.add(checkHistory.getAmount() != null ? String.valueOf(checkHistory.getAmount()) : "");
		columns.add(!checkHistory.isEcheck() ? "No" : "Yes");
		columns.add(checkHistory.isVoided() ? "Voided" : "Processed");
		return columns;
    }

	private  List<PDFCell> getCheckHistoryRowForPDF(CheckHistory checkHistory){
		List<PDFCell> cells = new ArrayList<PDFCell>();

		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");
		String createdDateText = checkHistory.getCreatedDate() != null ? df.format(checkHistory.getCreatedDate()) : "";
		boolean wrap = true;
		cells.add(new PDFCell(checkHistory.getCheckNumber() != null ? String.valueOf(checkHistory.getCheckNumber()) : "", !wrap));
		cells.add(new PDFCell(createdDateText, !wrap));
		cells.add(new PDFCell(checkHistory.getSiteName() != null ? StringUtils.capitalize(checkHistory.getSiteName()) : "", !wrap));
		cells.add(new PDFCell(checkHistory.getBankName() != null ? StringUtils.capitalize(checkHistory.getBankName()) : "", wrap));
		cells.add(new PDFCell(checkHistory.getPaymentType() != null ? checkHistory.getPaymentType().toString() : "", !wrap));
		cells.add(new PDFCell(checkHistory.getTotalTransactions() != null ? String.valueOf(checkHistory.getTotalTransactions()) : "", !wrap));
		cells.add(new PDFCell(checkHistory.getMachineName() != null ? checkHistory.getMachineName() : "", !wrap));
		cells.add(new PDFCell(checkHistory.getModifiedBy() != null ? checkHistory.getModifiedBy() : "", !wrap));
		cells.add(new PDFCell(checkHistory.getComments() != null ? checkHistory.getComments() : "", wrap));
		cells.add(new PDFCell(checkHistory.getAmount() != null ? String.valueOf(checkHistory.getAmount()) : "", !wrap));
		cells.add(new PDFCell(!checkHistory.isEcheck() ? "No" : "Yes", !wrap));
		cells.add(new PDFCell(checkHistory.isVoided() ? "Voided" : "Processed", !wrap));
		return cells;
	}

	@Link(label="View Check History", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/confirmVoidCheck.admin")
	public ModelAndView confirmVoidCheck(HttpServletRequest request,  @RequestParam(required = false) Long checkNum) {
		ModelAndView modelAndView = this.getModelAndView(request, VIEW_CHECK_DETAILS, TOP_FUNDS_TRANSFER,
				SUB_FUNDS_TRANSFER_PRINT_CHQ);
		CheckHistory checkDetails = null;
		if (this.isInternalUser(request) || this.isFeatureEnabledForUser(request, "CheckPrinting")) {
			checkDetails = this.getServiceStub().getCheckHistory(checkNum);
			modelAndView.addObject("checkDetails", checkDetails);
			modelAndView.addObject("voidCheckNum", checkNum);
		} else {
			modelAndView.setViewName(REDIRECT_ACCESS_DENIED);
		}
		return modelAndView;
	}

	@Link(label="Check History", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/voidCheck.admin")
	public ModelAndView voidCheck(HttpServletRequest request, @RequestParam(required = false) Long checkNum, @RequestParam(required = false) String comments) {
		ModelAndView modelAndView = this.getModelAndView(request, VIEW_CHECK_DETAILS, TOP_FUNDS_TRANSFER,
				SUB_FUNDS_TRANSFER_PRINT_CHQ);
		CheckHistory checkDetails = null;
		if (this.isInternalUser(request) || this.isFeatureEnabledForUser(request, "CheckPrinting")) {
			try {
				if (this.getServiceStub().doVoidCheck(checkNum, comments)) {
					checkDetails = this.getServiceStub().getCheckHistory(checkNum);
					modelAndView.addObject("status", "success");
				} else {
					modelAndView.addObject("status", "error");
				}
				modelAndView.addObject("checkDetails", checkDetails);
			} catch (Exception exception) {
				modelAndView.addObject("status", "error");
				logger.error("Error Occured When Voiding the Check: " + checkNum,  exception);
			}
			modelAndView.addObject("voidCheckNum", checkNum);
		} else {
			modelAndView.setViewName(REDIRECT_ACCESS_DENIED);
		}
		return modelAndView;
	}

	/* Initial Recurring Transaction Form Load for Viewing Recurring Transactions report */
	@Link(label="View Check", family="CheckController", parent = "Home" )
	@RequestMapping(value="/viewCheck.admin")
	public ModelAndView viewCheckPrint(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, VIEW_CHECK_PRINT, TOP_FUNDS_TRANSFER,
				SUB_FUNDS_TRANSFER_PRINT_CHQ);

		if (!this.isFeatureEnabledForUser(request, "CheckPrinting")) {
			modelAndView.setViewName(REDIRECT_ACCESS_DENIED);
			return modelAndView;
		}

		List<Site> sites = this.getAssignedSites(request);
		List<PaymentType> paymentTypes = new LinkedList<PaymentType>();
		for (PaymentType transactionMode : PaymentType.values()) {
			paymentTypes.add(transactionMode);
		}

		modelAndView.addObject("sites", sites);
		modelAndView.addObject("paymentTypes", paymentTypes);
		modelAndView.addObject("displayFlag", false);
		return modelAndView;
	}

	/* Called When Recurring Transaction Form is Submitted */
	@Link(label="View Check", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/displayViewCheck.admin")
	public ModelAndView displayViewCheckPrint(HttpServletRequest request,
											 @RequestParam(required = false) Long siteId,
											 @RequestParam(required = false) String paymentType,
											 @CookieValue("JSESSIONID") String jsessionId) {

		if (!checkLoggedinUserSiteValidity(request, siteId)) {
			ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
			return modelAndView;
		}

		ModelAndView modelAndView = this.getModelAndView(request, VIEW_CHECK_PRINT, TOP_FUNDS_TRANSFER,
				SUB_FUNDS_TRANSFER_PRINT_CHQ);
		if (!this.isFeatureEnabledForUser(request, "CheckPrinting")) {
			modelAndView.setViewName(REDIRECT_ACCESS_DENIED);
			return modelAndView;
		}


		List<Site> sites = this.getAssignedSites(request);
		List<PaymentType> paymentTypes = new LinkedList<PaymentType>();
		for (PaymentType paymentTypeItem : PaymentType.values()) {
			paymentTypes.add(paymentTypeItem);
		}
		modelAndView.addObject("selectedSiteId", siteId);
		modelAndView.addObject("selectedPaymentType", paymentType);
		modelAndView.addObject("sites", sites);
		modelAndView.addObject("paymentTypes", paymentTypes);
		modelAndView.addObject("sessionId", jsessionId);
		modelAndView.addObject("displayFlag", true);
		modelAndView.addObject("reportURL", this.requestURL);
		modelAndView.addObject("modUserId", request.getUserPrincipal().getName());
		modelAndView.addObject("machineName", request.getRemoteHost());
		return modelAndView;
	}

	/* Called From Applet to Load report. If a Specific Site is chosen, then RecurringTransactionReport_SiteUser.jasper
	 * is rendered, otherwise RecurringTransactionReport_PSOUser.jasper is rendered.  */
	@Link(label="View Check", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/renderCheck.admin", method = RequestMethod.POST)
	public ModelAndView renderCheck(HttpServletRequest request,
											HttpServletResponse response,
											@RequestParam(required = false) Long siteId,
											@RequestParam(required = false) String paymentType) throws Exception {

		if (!this.isFeatureEnabledForUser(request, "CheckPrinting")) {
			ModelAndView modelAndView = this.getModelAndView(request, REDIRECT_ACCESS_DENIED);
			return modelAndView;
		}

		if (!checkLoggedinUserSiteValidity(request, siteId)) {
			ModelAndView modelAndView = this.getModelAndView(request, REDIRECT_ACCESS_DENIED);
			return modelAndView;
		}

		Connection reportConn = null;
		String reportName = CHECK_RPT;
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
			InputStream signature = this.getClass().getResourceAsStream("/signature.png");
			InputStream logoImage = this.getClass().getResourceAsStream("/logo.gif");
			reportParameters.put("SIGNATURE", signature);
			reportParameters.put("BANKLOGO", logoImage);
			reportParameters.put("REQUEST_URL", this.requestURL);
			reportParameters.put("BaseDir", reportFile.getParentFile());
			reportParameters.put("SITE_ID", siteId);
			reportParameters.put("PAYMENT_TYPE", paymentType);
			reportParameters.put("MOD_USER_ID", request.getUserPrincipal().getName());
			reportParameters.put("IP_ADDRESS", request.getRemoteHost());
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

	private Table getCheckHistoryHtmlExportTable(String caption) {
		HtmlTable table = new HtmlTable().caption(caption);
		HtmlRow row = new HtmlRow();
		table.setRow(row);
		HtmlColumn checkNumber = new HtmlColumn("checkNumber").title("Check Number");
		row.addColumn(checkNumber);
		HtmlColumn createdDate = new HtmlColumn("createdDate").title("Check Date");
		row.addColumn(createdDate);
		HtmlColumn siteName = new HtmlColumn("siteName").title("Site Name");
		row.addColumn(siteName);
		HtmlColumn bankName = new HtmlColumn("bankName").title("Bank Name");
		row.addColumn(bankName);
		HtmlColumn paymentType = new HtmlColumn("paymentType").title("Payment Type");
		paymentType.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
            	String value = new HtmlCellEditor().getValue(item, "paymentType", rowcount).toString();
            	return StringUtils.capitalize(value.toLowerCase());
            }
        });
		row.addColumn(paymentType);
		HtmlColumn totalTransactions = new HtmlColumn("totalTransactions").title("Total Transactions");
		row.addColumn(totalTransactions);
		HtmlColumn machineName = new HtmlColumn("machineName").title("Client IP Address");
		row.addColumn(machineName);
		HtmlColumn modifiedBy = new HtmlColumn("modifiedBy").title("Check Generated By");
		row.addColumn(modifiedBy);
		HtmlColumn comments = new HtmlColumn("comments").title("Comments");
		row.addColumn(comments);
		HtmlColumn checkAmount = new HtmlColumn("amount").title("Check Amount");
		checkAmount.setCellEditor(new NumberCellEditor("$###,##0.00"));
		row.addColumn(checkAmount);
		HtmlColumn echeck = new HtmlColumn("echeck").title("E-Check");
		echeck.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
            	Object echeckVal = new HtmlCellEditor().getValue(item, "echeck", rowcount);
            	String cValue = "No";
            	if (echeckVal.equals("true")) {
            		cValue = "Yes";
            	} else {
            		cValue = "No";
            	}
            	return cValue;
            }
        });
		row.addColumn(echeck);
		HtmlColumn voided = new HtmlColumn("voided").title("Voided");
		voided.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
            	Object voidedVal = new HtmlCellEditor().getValue(item, "voided", rowcount);
            	String cValue = "No";
            	if (voidedVal.equals("true")) {
            		cValue = "Yes";
            	} else {
            		cValue = "No";
            	}
            	return cValue;
            }
        });
		row.addColumn(voided);
		return table;
	}

    private Table getCheckHistoryExportTable(String caption) {
    	Table table = new Table().caption(caption);
		Row row = new Row();
		table.setRow(row);
		Column checkNumber = new Column("checkNumber").title("Check Number");
		row.addColumn(checkNumber);
		Column createdDate = new Column("createdDate").title("Check Date");
		row.addColumn(createdDate);
		Column siteName = new Column("siteName").title("Site Name");
		row.addColumn(siteName);
		Column bankName = new Column("bankName").title("Bank Name");
		row.addColumn(bankName);
		Column paymentType = new Column("paymentType").title("Payment Type");
		paymentType.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
            	String value = new HtmlCellEditor().getValue(item, "paymentType", rowcount).toString();
            	return StringUtils.capitalize(value.toLowerCase());
            }
        });
		row.addColumn(paymentType);
		Column totalTransactions = new Column("totalTransactions").title("Total Transactions");
		row.addColumn(totalTransactions);
		Column machineName = new Column("machineName").title("Client IP Address");
		row.addColumn(machineName);
		Column modifiedBy = new Column("modifiedBy").title("Check Generated By");
		row.addColumn(modifiedBy);
		Column comments = new Column("comments").title("Comments");
		row.addColumn(comments);
		Column checkAmount = new Column("amount").title("Check Amount");
		checkAmount.setCellEditor(new NumberCellEditor("$###,##0.00"));
		row.addColumn(checkAmount);
		Column echeck = new Column("echeck").title("ECheck");
		echeck.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
            	Object echeckVal = new HtmlCellEditor().getValue(item, "echeck", rowcount);
            	String cValue = "No";
            	if (echeckVal.equals("true")) {
            		cValue = "Yes";
            	} else {
            		cValue = "No";
            	}
            	return cValue;
            }
        });
		row.addColumn(echeck);
		Column voided = new Column("voided").title("Voided");
		voided.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
            	Object voidedVal = new HtmlCellEditor().getValue(item, "voided", rowcount);
            	String cValue = "No";
            	if (voidedVal.equals("true")) {
            		cValue = "Yes";
            	} else {
            		cValue = "No";
            	}
            	return cValue;
            }
        });
		row.addColumn(voided);
		return table;
    }
}