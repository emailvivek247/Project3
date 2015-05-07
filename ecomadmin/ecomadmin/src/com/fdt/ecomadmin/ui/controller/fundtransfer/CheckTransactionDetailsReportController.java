package com.fdt.ecomadmin.ui.controller.fundtransfer;

import static com.fdt.ecomadmin.ui.controller.ReportConstants.CHECK_TX_DETAIL_RPT;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.CHECK_TRANSACTION_DETAILS_REPORT_HTML_VIEW;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_CHECK_TRANSACTION_DETAILS;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.*;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JRVirtualizationHelper;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsAbstractExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.FileBufferedOutputStream;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSwapFile;

import org.apache.commons.lang.StringUtils;
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
import com.fdt.ecom.entity.Site;

@Controller
public class CheckTransactionDetailsReportController extends AbstractBaseController  {

    @Value("${email.useractivation.requesturl}")
    protected String requestURL = null;

    @Autowired
    protected DataSource datasource = null;

    @Value("${jasper.tempfile.path}")
    private String reportTempFile = null;

    @Value("${jasper.tempfile.blocksize}")
    private String blockSize;

    @Value("${jasper.tempfile.mingrowcount}")
    private String minGrowCount;

    /* Initial checktransactiondetails Transaction Form Load for Viewing checktransactiondetails Transactions report */
    @Link(label="Check Transaction Details", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewchecktransactiondetails.admin")
    public ModelAndView viewcheckTransactionDetails(HttpServletRequest request) {
        return this.viewCheckTransactionDetails(request, CHECK_TRANSACTION_DETAILS_REPORT_HTML_VIEW);
    }

    /* Called When checktransactiondetails Transaction Form is Submitted */
    @Link(label="Check Transaction Details", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchchecktransactiondetailsapp.admin")
    public ModelAndView searchCheckTransactionDetails(HttpServletRequest request,
                                             @RequestParam(required = false) Long siteId,
                                             @RequestParam(required = false) String dateRange,
                                             @RequestParam(required = false) String checkNum,
                                             @CookieValue("JSESSIONID") String sessionId) {
        if (!this.isInternalUser(request)) {
            List<Site> sites = this.getAssignedSites(request);
            for (Site site : sites) {
                siteId = site.getId();
            }
        }

        String fromDate = null;

        String toDate = null;

        if(!StringUtils.isBlank(dateRange)) {
        	fromDate = dateRange.split("-")[0].trim();
        }

        if(!StringUtils.isBlank(dateRange)) {
        	toDate = dateRange.split("-")[1].trim();
        }

        return this.searchTransactions(request, VIEW_CHECK_TRANSACTION_DETAILS, siteId, fromDate, toDate,
        		checkNum, sessionId, TOP_FUNDS_TRANSFER, SUB_FUNDS_TRANSFER_DETAIL_REPORT);

    }

    @Link(label="Check Transaction Details", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchchecktransactiondetails.admin")
    public ModelAndView viewCheckTransactionDetailsHTML(HttpServletRequest request,
                                   @RequestParam(required = false) Long siteId,
                                   @RequestParam(required = false) String dateRange,
                                   @RequestParam(required = false) String checkNum,
                                   @RequestParam(required = false) int pageIndex) {

        if (!this.isInternalUser(request)) {
            List<Site> sites = this.getAssignedSites(request);
            for (Site site : sites) {
                siteId = site.getId();
            }
        }

        String reportName = CHECK_TX_DETAIL_RPT;

        String fromDate = null;

        String toDate = null;

        if(!StringUtils.isBlank(dateRange)) {
        	fromDate = dateRange.split("-")[0].trim();
        }

        if(!StringUtils.isBlank(dateRange)) {
        	toDate = dateRange.split("-")[1].trim();
        }
        return viewHTML(request, siteId, fromDate, toDate, checkNum, pageIndex, reportName,
        		CHECK_TRANSACTION_DETAILS_REPORT_HTML_VIEW, TOP_FUNDS_TRANSFER, SUB_FUNDS_TRANSFER_DETAIL_REPORT);
    }

    /* Called From Applet to Load report. If a Specific Site is chosen, then checktransactiondetailsTransactionReport_SiteUser.jasper
     * is rendered, otherwise checktransactiondetailsTransactionReport_PSOUser.jasper is rendered.  */
    @Link(label="Check Transaction Details", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewchecktransactiondetailsreport.admin", method = RequestMethod.POST)
    public ModelAndView viewCheckTransactionDetailsJasperReport(HttpServletRequest request,
                                            HttpServletResponse response,
                                             @RequestParam(required = false) Long siteId,
                                             @RequestParam(required = false) String fromDate,
                                             @RequestParam(required = false) String toDate,
                                             @RequestParam(required = false) String checkNum) throws Exception {
        if (!this.isInternalUser(request)) {
            List<Site> sites = this.getAssignedSites(request);
            for (Site site : sites) {
                siteId = site.getId();
            }
        }
        if (fromDate == "" || toDate == "") {
            fromDate = null;
            toDate = null;
        }
        String reportName = CHECK_TX_DETAIL_RPT;
        return this.viewAppletJasperReport(request, response, siteId, fromDate, toDate, checkNum, reportName);

    }

    @RequestMapping(value="/viewchecktransactiondetailspdf.admin")
    public String viewCheckTransactiondetailsPdf(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(required = false) Long siteId,
                                   @RequestParam(required = false) String fromDate,
                                   @RequestParam(required = false) String toDate,
                                   @RequestParam(required = false) String checkNum) {
        if (!this.isInternalUser(request)) {
            List<Site> sites = this.getAssignedSites(request);
            for (Site site : sites) {
                siteId = site.getId();
            }
        }
        String reportName = CHECK_TX_DETAIL_RPT;
        String outputFileName = "Check_Transaction_Detail_Report_";
        this.viewPDF(request, response, siteId, fromDate, toDate, checkNum, reportName, outputFileName);
        return null;
    }

    @RequestMapping(value="/viewchecktransactiondetailsxls.admin")
    public String viewCheckTransactiondetailsXLSX(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(required = false) Long siteId,
                                   @RequestParam(required = false) String fromDate,
                                     @RequestParam(required = false) String toDate,
                                     @RequestParam(required = false) String checkNum) {
        if (!this.isInternalUser(request)) {
            List<Site> sites = this.getAssignedSites(request);
            for (Site site : sites) {
                siteId = site.getId();
            }
        }
        String reportName = CHECK_TX_DETAIL_RPT;
        String outputFileName = "Check_Transaction_Detail_Report_";
        this.viewXLSX(request, response, siteId, fromDate, toDate, checkNum, reportName, outputFileName);
        return null;
    }

    @RequestMapping(value="/viewchecktransactiondetailsrtf.admin")
    public String viewCheckTransactiondetailsCsv(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(required = false) Long siteId,
                                   @RequestParam(required = false) String fromDate,
                                   @RequestParam(required = false) String toDate,
                                   @RequestParam(required = false) String checkNum) {
        if (!this.isInternalUser(request)) {
            List<Site> sites = this.getAssignedSites(request);
            for (Site site : sites) {
                siteId = site.getId();
            }
        }
        String reportName = CHECK_TX_DETAIL_RPT;
        String outputFileName = "Check_Transaction_Detail_Report_";
        this.viewRTF(request, response, siteId, fromDate, toDate, checkNum, reportName, outputFileName);
        return null;
    }

    protected ModelAndView viewCheckTransactionDetails(HttpServletRequest request, String viewName) {
        ModelAndView modelAndView = this.getModelAndView(request, viewName, TOP_FUNDS_TRANSFER,
        	SUB_FUNDS_TRANSFER_DETAIL_REPORT);
        List<Site> sites = this.getAssignedSites(request);
        modelAndView.addObject("sites", sites);
        modelAndView.addObject("displayFlag", false);
        return modelAndView;

    }

    protected ModelAndView searchTransactions(HttpServletRequest request,
                                              String viewName,
                                              Long siteId,
                                              String fromDate,
                                              String toDate,
                                              String checkNum,
                                              String sessionId,
                                              String topMenu,
                                              String subMenu) {
        ModelAndView modelAndView = this.getModelAndView(request, viewName);
        List<Site> sites = this.getAssignedSites(request);
        modelAndView.addObject("selectedSiteId", siteId);
        modelAndView.addObject("fromDate", fromDate);
        modelAndView.addObject("toDate", toDate);
        modelAndView.addObject("checkNum", checkNum);
        modelAndView.addObject("sites", sites);
        modelAndView.addObject("sessionId", sessionId);
        modelAndView.addObject("displayFlag", true);
        modelAndView.addObject("reportURL", this.requestURL);
        modelAndView.addObject("requestUrl", this.requestURL);
        modelAndView.addObject("topMenu", topMenu);
		modelAndView.addObject("subMenu", subMenu);
        return modelAndView;
    }

    protected ModelAndView viewAppletJasperReport(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Long siteId,
                                         String fromDate,
                                         String toDate,
                                         String checkNum,
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
            reportParameters.put("SITE_ID", siteId);
            reportParameters.put("CHECKNUM", checkNum);
            reportParameters.put("ROLE",  (this.isInternalUser(request) == true ? "PSO" : "CLIENT"));
            if (checkNum ==null || checkNum.equalsIgnoreCase("")) {
                if (fromDate != null && fromDate != "" && toDate != null && toDate != "") {
                    reportParameters.put("FROM_DATE", fromDate);
                    reportParameters.put("TO_DATE", toDate);
                }
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
                                    Long siteId,
                                      String fromDate,
                                      String toDate,
                                      String checkNum,
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
            List<Site> sites = this.getAssignedSites(request);

            modelAndView.addObject("sites" , sites);
            modelAndView.addObject("fromDate", fromDate);
            modelAndView.addObject("toDate", toDate);
            modelAndView.addObject("checkNum", checkNum);
            modelAndView.addObject("pageIndex", pageIndex);
            modelAndView.addObject("selectedSiteId", siteId);
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
            reportParameters.put("SITE_ID",  siteId);
            reportParameters.put("CHECKNUM", checkNum);
            reportParameters.put("ROLE",  (this.isInternalUser(request) == true ? "PSO" : "CLIENT"));
            if (checkNum ==null || checkNum.equalsIgnoreCase("")) {
                if (fromDate != null && fromDate != "" && toDate != null && toDate != "") {
                    reportParameters.put("FROM_DATE", fromDate);
                    reportParameters.put("TO_DATE", toDate);
                }
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
            exporter.setParameter(JRHtmlExporterParameter.ZOOM_RATIO, 1.1f);
            exporter.setParameter(JRHtmlExporterParameter.SIZE_UNIT, JRHtmlExporterParameter.SIZE_UNIT_POINT);
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

    protected void viewXLSX(HttpServletRequest request, HttpServletResponse response, Long siteId, String fromDate,
    		String toDate, String checkNum, String reportName, String outputFileName) {
    	Connection reportConn = null;
        Map<String,Object> reportParameters = new HashMap<String, Object>();
        String extension = SystemUtil.format(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "MMM-dd-yyyy");
        outputFileName = outputFileName.concat(extension).concat(".xlsx");
        FileBufferedOutputStream fbos = new FileBufferedOutputStream();
        ServletOutputStream ouputStream = null;
        JRSwapFile jRSwapFile =  new JRSwapFile(this.reportTempFile, this.getBlockSize(), this.getMinGrowCount());
        JRSwapFileVirtualizer jRVirtualizer = new JRSwapFileVirtualizer(400, jRSwapFile, true);
        JRVirtualizationHelper.setThreadVirtualizer(jRVirtualizer);
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
            reportParameters.put("SITE_ID", siteId);
            reportParameters.put("FROM_DATE", fromDate);
            reportParameters.put("TO_DATE", toDate);
            reportParameters.put("CHECKNUM", checkNum);
            reportParameters.put(JRParameter.REPORT_VIRTUALIZER, jRVirtualizer);

            reportConn = this.datasource.getConnection();
            jasperPrint = JasperFillManager.fillReport(jasperReport, reportParameters, reportConn);

            JRXlsxExporter exporter = new JRXlsxExporter();

            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, fbos);
            exporter.setParameter(JRXlsAbstractExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
            exporter.setParameter(JRXlsAbstractExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);

            exporter.exportReport();

            /** Setting the Response Content Type **/
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            /** Setting the Response Header **/
            response.setHeader("Content-Disposition", "attachment; filename=" + outputFileName);
            response.setContentLength(fbos.size());
            ouputStream = response.getOutputStream();
            fbos.writeData(ouputStream);
        } catch (Exception exception) {
            logger.error("Error Occured When Rendering in viewXLSX Of CheckTransactionDetailsReportController",  exception);
        } finally {
            /** Close the Streams **/
            try {
                fbos.close();
                fbos.dispose();
                ouputStream.flush();
            } catch (IOException iOException) {
                logger.error("Error While Closing the Servlet Stream",  iOException);
            }
            try {
                if (reportConn != null)reportConn.close();
            } catch (SQLException sQLException) {
                logger.error("Error Occured When Rendering in viewXLSX Of CheckTransactionDetailsReportController",
                		sQLException);
            }
            /** Cleaning the Virtualizer **/
            if (jRVirtualizer != null) {
                jRSwapFile.dispose();
                jRVirtualizer.cleanup();
            }
        }

    }

    protected void viewPDF(HttpServletRequest request, HttpServletResponse response, Long siteId, String fromDate,
    		String toDate, String checkNum, String reportName, String outputFileName) {
        Connection reportConn = null;
        Map<String,Object> reportParameters = new HashMap<String, Object>();
        String extension = SystemUtil.format(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "MMM-dd-yyyy");
        outputFileName = outputFileName.concat(extension).concat(".pdf");
        FileBufferedOutputStream fbos = new FileBufferedOutputStream();
        ServletOutputStream ouputStream = null;
        JRSwapFile jRSwapFile =  new JRSwapFile(this.reportTempFile, this.getBlockSize(), this.getMinGrowCount());
        JRSwapFileVirtualizer jRVirtualizer = new JRSwapFileVirtualizer(400, jRSwapFile, true);
        JRVirtualizationHelper.setThreadVirtualizer(jRVirtualizer);
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

            reportParameters.put("SITE_ID", siteId);
            reportParameters.put("FROM_DATE", fromDate);
            reportParameters.put("TO_DATE", toDate);
            reportParameters.put("CHECKNUM", checkNum);
            reportParameters.put(JRParameter.REPORT_VIRTUALIZER, jRVirtualizer);

            reportConn = this.datasource.getConnection();

            jasperPrint = JasperFillManager.fillReport(jasperReport, reportParameters, reportConn);

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, fbos);
            exporter.exportReport();

            /** Setting the Response Content Type **/
            response.setContentType("application/pdf");
            /** Setting the Response Header **/
            response.setHeader("Content-Disposition", "attachment; filename=" + outputFileName);
            response.setContentLength(fbos.size());
            ouputStream = response.getOutputStream();
            fbos.writeData(ouputStream);
        } catch (Exception exception) {
            logger.error("Error Occured When Rendering in viewPDF Of CheckTransactionDetailsReportController",  exception);
        } finally {
            /** Close the Streams **/
            try {
                fbos.close();
                fbos.dispose();
                ouputStream.flush();
            } catch (IOException iOException) {
                logger.error("Error While Closing the Servlet Stream",  iOException);
            }
            try {
                if (reportConn != null)reportConn.close();
            } catch (SQLException sQLException) {
                logger.error("Error Occured When Rendering in viewPDF Of CheckTransactionDetailsReportController",
                		sQLException);
            }
            /** Cleaning the Virtualizer **/
            if (jRVirtualizer != null) {
                jRSwapFile.dispose();
                jRVirtualizer.cleanup();
            }
        }
    }

    protected void viewRTF(HttpServletRequest request, HttpServletResponse response, Long siteId, String fromDate,
    		String toDate, String checkNum, String reportName, String outputFileName) {
    	Connection reportConn = null;
        Map<String,Object> reportParameters = new HashMap<String, Object>();
        String extension = SystemUtil.format(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "MMM-dd-yyyy");
        outputFileName = outputFileName.concat(extension).concat(".rtf");
        FileBufferedOutputStream fbos = new FileBufferedOutputStream();
        ServletOutputStream ouputStream = null;
        JRSwapFile jRSwapFile =  new JRSwapFile(this.reportTempFile, this.getBlockSize(), this.getMinGrowCount());
        JRSwapFileVirtualizer jRVirtualizer = new JRSwapFileVirtualizer(400, jRSwapFile, true);
        JRVirtualizationHelper.setThreadVirtualizer(jRVirtualizer);
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

            reportParameters.put("SITE_ID", siteId);
            reportParameters.put("FROM_DATE", fromDate);
            reportParameters.put("TO_DATE", toDate);
            reportParameters.put("CHECKNUM", checkNum);
            reportParameters.put(JRParameter.REPORT_VIRTUALIZER, jRVirtualizer);

            reportConn = this.datasource.getConnection();
            jasperPrint = JasperFillManager.fillReport(jasperReport, reportParameters, reportConn);

            JRRtfExporter exporter = new JRRtfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, fbos);

            exporter.exportReport();

            /** Setting the Response Content Type **/
            response.setContentType("application/rtf");
            /** Setting the Response Header **/
            response.setHeader("Content-Disposition", "attachment; filename=" + outputFileName);
            response.setContentLength(fbos.size());
            ouputStream = response.getOutputStream();
            fbos.writeData(ouputStream);
        } catch (Exception exception) {
            logger.error("Error Occured When Rendering in viewRTF Of CheckTransactionDetailsReportController",  exception);
        } finally {
            /** Close the Streams **/
            try {
                fbos.close();
                fbos.dispose();
                ouputStream.flush();
            } catch (IOException iOException) {
                logger.error("Error While Closing the Servlet Stream",  iOException);
            }
            try {
                if (reportConn != null)reportConn.close();
            } catch (SQLException sQLException) {
                logger.error("Error Occured When Rendering in viewRTF Of CheckTransactionDetailsReportController",  sQLException);
            }
            /** Cleaning the Virtualizer **/
            if (jRVirtualizer != null) {
                jRSwapFile.dispose();
                jRVirtualizer.cleanup();
            }
        }
    }

    public int getBlockSize() {
		return Integer.parseInt(blockSize);
	}

	public int getMinGrowCount() {
		return Integer.parseInt(minGrowCount);
	}
}
