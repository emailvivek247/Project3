package com.fdt.ecomadmin.ui.controller.fundtransfer;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_FUNDS_TRANSFER_RECONCILIATION_REPORT;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_FUNDS_TRANSFER;
import static com.fdt.ecomadmin.ui.controller.ReportConstants.CHECK_RECON_RPT;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.ACCESS_DENIED;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.CHECK_RECONCILIATION_REPORT_HTML_VIEW;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.JASPER_FORWARD_PDF;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.JASPER_FORWARD_RTF;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.JASPER_FORWARD_XSLX;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_CHECK_RECONCILIATION_DETAILS;

import java.io.File;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.j2ee.servlets.BaseHttpServlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.breadcrumbs.Link;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.common.util.SystemUtil;

@Controller
public class CheckReconciliationReportController extends AbstractBaseController {

    @Value("${email.useractivation.requesturl}")
    protected String requestURL = null;

    @Autowired
    protected DataSource datasource = null;

    /* Initial checkreconciliationreport Transaction Form Load for Viewing checkreconciliationreport Transactions report */
    @Link(label="Check Reconciliation", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewcheckreconciliationreport.admin")
    public ModelAndView viewCheckReconciliationReport(HttpServletRequest request) {
        if (this.isInternalUser(request)) {
            return this.viewCheckReconciliationReport(request, CHECK_RECONCILIATION_REPORT_HTML_VIEW);
        } else {
            ModelAndView modelAndView = this.getModelAndView(request, ACCESS_DENIED);
            return modelAndView;
        }
    }

    /* Called When checkreconciliationreport Transaction Form is Submitted */
    @Link(label="Check Reconciliation", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchcheckreconciliationreportapp.admin")
    public ModelAndView searchCheckReconciliationReport(HttpServletRequest request,
                                             @RequestParam(required = false) String fromDate,
                                             @RequestParam(required = false) String toDate,
                                             @CookieValue("JSESSIONID") String sessionId) {
        if (this.isInternalUser(request)) {
            if (fromDate == "" || toDate == "") {
                fromDate = null;
                toDate = null;
            }
            return this.searchTransactions(request, VIEW_CHECK_RECONCILIATION_DETAILS, fromDate, toDate, sessionId, TOP_FUNDS_TRANSFER,
            		SUB_FUNDS_TRANSFER_RECONCILIATION_REPORT);
        } else {
            ModelAndView modelAndView = this.getModelAndView(request, ACCESS_DENIED);
            return modelAndView;
        }

    }

    @Link(label="Check Reconciliation", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchcheckreconciliationreport.admin")
    public ModelAndView viewCheckReconciliationReportHTML(HttpServletRequest request,
                                   @RequestParam(required = false) String fromDate,
                                     @RequestParam(required = false) String toDate,
                                     @RequestParam(required = false) int pageIndex) {
        if (this.isInternalUser(request)) {
            String reportName =  CHECK_RECON_RPT;
            if (fromDate == "" || toDate == "") {
                fromDate = null;
                toDate = null;
            }
            return viewHTML(request, fromDate, toDate, pageIndex, reportName, CHECK_RECONCILIATION_REPORT_HTML_VIEW, TOP_FUNDS_TRANSFER,
            		SUB_FUNDS_TRANSFER_RECONCILIATION_REPORT);
        } else {
            ModelAndView modelAndView = this.getModelAndView(request, ACCESS_DENIED);
            return modelAndView;
        }
    }

    /* Called From Applet to Load report. */
    @Link(label="Check Reconciliation", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewcheckreconciliationreport.admin", method = RequestMethod.POST)
    public ModelAndView viewCheckReconciliationReportJasperReport(HttpServletRequest request,
                                            HttpServletResponse response, @RequestParam(required = false) String fromDate,
                                             @RequestParam(required = false) String toDate)  throws Exception {
        if (this.isInternalUser(request)) {
            if (fromDate == "" || toDate == "") {
                fromDate = null;
                toDate = null;
            }
            String reportName = CHECK_RECON_RPT;
            return this.viewAppletJasperReport(request, response, fromDate, toDate, reportName);
        } else {
            ModelAndView modelAndView = this.getModelAndView(request, ACCESS_DENIED);
            return modelAndView;
        }
    }


    @RequestMapping(value="/viewcheckreconciliationreportpdf.admin")
    public String viewCheckReconciliationReportPdf(HttpServletRequest request,
                                   @RequestParam(required = false) String fromDate,
                                   @RequestParam(required = false) String toDate) {
        if (this.isInternalUser(request)) {
            String reportName = CHECK_RECON_RPT;
            String outputFileName = "CheckReconciliationReport_";
            return this.viewPDF(request, fromDate, toDate, reportName, outputFileName);
        } else {
            return null;
        }
    }


    @RequestMapping(value="/viewcheckreconciliationreportxls.admin")
    public String viewCheckReconciliationReportXLSX(HttpServletRequest request,
                                   @RequestParam(required = false) String fromDate,
                                     @RequestParam(required = false) String toDate) {
        if (this.isInternalUser(request)) {
            String reportName = CHECK_RECON_RPT;
            String outputFileName = "CheckReconciliationReport_";
            return this.viewXLSX(request, fromDate, toDate, reportName, outputFileName);
        } else {
            return null;
        }
    }


    @RequestMapping(value="/viewcheckreconciliationreportrtf.admin")
    public String viewCheckReconciliationReportCsv(HttpServletRequest request,
                                   @RequestParam(required = false) String fromDate,
                                   @RequestParam(required = false) String toDate) {
        if (this.isInternalUser(request)) {
            String reportName = CHECK_RECON_RPT;
            String outputFileName = "CheckReconciliationReport_";
            return this.viewRTF(request, fromDate, toDate, reportName, outputFileName);
        } else {
            return null;
        }

    }

    protected ModelAndView viewCheckReconciliationReport(HttpServletRequest request, String viewName) {
        ModelAndView modelAndView = this.getModelAndView(request, viewName, TOP_FUNDS_TRANSFER,
        		SUB_FUNDS_TRANSFER_RECONCILIATION_REPORT);
        modelAndView.addObject("displayFlag", false);
        return modelAndView;

    }

    protected ModelAndView searchTransactions(HttpServletRequest request,
                                              String viewName,
                                              String fromDate,
                                              String toDate,
                                              String sessionId,
                                              String topMenu,
                                              String subMenu) {
        ModelAndView modelAndView = this.getModelAndView(request, viewName);
        modelAndView.addObject("fromDate", fromDate);
        modelAndView.addObject("toDate", toDate);
        modelAndView.addObject("sessionId", sessionId);
        modelAndView.addObject("displayFlag", true);
        modelAndView.addObject("reportURL", requestURL);
        modelAndView.addObject("topMenu", topMenu);
		modelAndView.addObject("subMenu", subMenu);
        return modelAndView;
    }

    protected ModelAndView viewAppletJasperReport(HttpServletRequest request,
                                         HttpServletResponse response,
                                         String fromDate,
                                         String toDate,
                                         String reportName) throws Exception {
        Connection reportConn = null;
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
            if (fromDate != null && fromDate != "" && toDate != null && toDate != "") {
                reportParameters.put("FROM_DATE", fromDate);
                reportParameters.put("TO_DATE", toDate);
            }
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

    protected ModelAndView viewHTML(HttpServletRequest request,
                                    String fromDate,
                                      String toDate,
                                      int pageIndex,
                                      String reportName,
                                      String viewName,
                                      String topMenu,
                                      String subMenu) {
        ModelAndView modelAndView = this.getModelAndView(request, viewName);
        int lastPageIndex = 0;
        StringBuffer stringBuffer = new StringBuffer();
        Connection reportConn = null;
        try {
            modelAndView.addObject("fromDate", fromDate);
            modelAndView.addObject("toDate", toDate);
            modelAndView.addObject("pageIndex", pageIndex);
            modelAndView.addObject("displayFlag", true);
            modelAndView.addObject("topMenu", topMenu);
    		modelAndView.addObject("subMenu", subMenu);

            ServletContext servletContext = this.getServletContext();
            File reportFile = null;
            JasperReport jasperReport = null;
            JasperPrint jasperPrint = null;
            Map<String,Object> reportParameters = new HashMap<String,Object>();

            reportFile = new File(servletContext.getRealPath(reportName));
            jasperReport = (JasperReport)JRLoader.loadObjectFromFile(reportFile.getPath());

            if (!reportFile.exists()) {
                throw new JRRuntimeException("The report design must be compiled first.");
            }
            reportParameters.put("REQUEST_URL", this.requestURL);
            reportParameters.put("BaseDir", reportFile.getParentFile());
            if (fromDate != null && fromDate != "" && toDate != null && toDate != "") {
                reportParameters.put("FROM_DATE", fromDate);
                reportParameters.put("TO_DATE", toDate);
            }
            reportParameters.put("PAGE_INDEX", pageIndex);

            reportConn = this.datasource.getConnection();
            jasperPrint = JasperFillManager.fillReport(jasperReport, reportParameters, reportConn);

            JRHtmlExporter exporter = new JRHtmlExporter();

            if (jasperPrint.getPages().size() == 0) {
                stringBuffer =  new StringBuffer(this.getMessage("report.nodata"));
                modelAndView.addObject("stringBuffer", stringBuffer);
                return modelAndView;
            }

            if (jasperPrint.getPages() != null) {
                lastPageIndex = jasperPrint.getPages().size() - 1;
            }

            if (pageIndex < 0) {
                pageIndex = 0;
            }

            if (pageIndex > lastPageIndex) {
                pageIndex = lastPageIndex;
            }

            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STRING_BUFFER, stringBuffer);
            exporter.setParameter(JRExporterParameter.PAGE_INDEX, Integer.valueOf(pageIndex));
            exporter.setParameter(JRHtmlExporterParameter.HTML_HEADER, "");
            exporter.setParameter(JRHtmlExporterParameter.BETWEEN_PAGES_HTML, "");
            exporter.setParameter(JRHtmlExporterParameter.HTML_FOOTER, "");
            exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, false);
            exporter.setParameter(JRHtmlExporterParameter.ZOOM_RATIO, 1.3f);
            exporter.exportReport();
        } catch (Exception exception) {
            logger.error("Error Occured When Rendering the Report HTML Report",  exception);
        } finally {
            try {
                if (reportConn != null) {
                    reportConn.close();
                }
            } catch (SQLException sQLException) {
                logger.error("Error Occured in Closing the Connection in the HTML Report",  sQLException);
            }
        }
        modelAndView.addObject("stringBuffer", stringBuffer);
        modelAndView.addObject("lastPageIndex", lastPageIndex);
        return modelAndView;
    }

    protected String viewXLSX(HttpServletRequest request, String fromDate, String toDate, String reportName,
            String outputFileName) {
        String forwardRequestName = JASPER_FORWARD_XSLX;
        String extension = SystemUtil.format(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "MMM-dd-yyyy");
        forwardRequestName = forwardRequestName.concat("&fileName=").concat(outputFileName).concat(extension).concat(".xlsx");
        return this.viewDownLoadContent(request, fromDate, toDate, reportName, forwardRequestName);
    }

    protected String viewPDF(HttpServletRequest request, String fromDate, String toDate, String reportName,
            String outputFileName) {
        String forwardRequestName = JASPER_FORWARD_PDF;
        String extension = SystemUtil.format(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "MMM-dd-yyyy");
        forwardRequestName = forwardRequestName.concat("&fileName=").concat(outputFileName).concat(extension).concat(".pdf");
        return this.viewDownLoadContent(request, fromDate, toDate, reportName, forwardRequestName);
    }

    protected String viewRTF(HttpServletRequest request, String fromDate, String toDate, String reportName,
            String outputFileName) {
        String forwardRequestName = JASPER_FORWARD_RTF;
        String extension = SystemUtil.format(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "MMM-dd-yyyy");
        forwardRequestName = forwardRequestName.concat("&fileName=").concat(outputFileName).concat(extension).concat(".rtf");
        return this.viewDownLoadContent(request, fromDate, toDate, reportName, forwardRequestName);
    }

    protected String viewDownLoadContent(HttpServletRequest request, String fromDate, String toDate, String reportName,
            String forwardRequestName) {
        Connection reportConn = null;
        Map<String,Object> reportParameters = new HashMap<String, Object>();
        try {
            ServletContext servletContext = this.getServletContext();
            File reportFile = null;
            JasperReport jasperReport = null;
            JasperPrint jasperPrint = null;

            reportFile = new File(servletContext.getRealPath(reportName));
            jasperReport = (JasperReport)JRLoader.loadObjectFromFile(reportFile.getPath());

            if (!reportFile.exists()) {
                throw new JRRuntimeException("The report design must be compiled first.");
            }

            reportParameters.put("FROM_DATE", fromDate);
            reportParameters.put("TO_DATE", toDate);

            reportConn = this.datasource.getConnection();
            jasperPrint = JasperFillManager.fillReport(jasperReport, reportParameters, reportConn);
            request.getSession().setAttribute(BaseHttpServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);
        } catch (Exception exception) {
            logger.error("Error Occured When Rendering in viewDownLoadContent",  exception);
        } finally {
            try {
                if (reportConn != null) {
                    reportConn.close();
                }
            } catch (SQLException sQLException) {
                logger.error("Error Occured in Closing the Connection in viewDownLoadContent",  sQLException);
            }
        }
        return forwardRequestName;
    }

}
