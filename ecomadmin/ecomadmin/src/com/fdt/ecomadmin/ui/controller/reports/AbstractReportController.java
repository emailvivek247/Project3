package com.fdt.ecomadmin.ui.controller.reports;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
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
import net.sf.jasperreports.engine.export.JRCsvExporter;
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
import net.sf.jasperreports.j2ee.servlets.BaseHttpServlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.common.util.SystemUtil;
import com.fdt.ecom.entity.Code;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.enums.TransactionType;

public abstract class AbstractReportController extends AbstractBaseController {
	private static final String REGISTERED_APPLICATION = "REGISTERED_APPLICATION";

    @Value("${email.useractivation.requesturl}")
    private String requestURL = null;

    @Autowired
    private DataSource datasource = null;

    @Value("${jasper.tempfile.path}")
    private String reportTempFile = null;

    @Value("${jasper.tempfile.blocksize}")
    private String blockSize;

    @Value("${jasper.tempfile.mingrowcount}")
    private String minGrowCount;

    protected ModelAndView viewTransactions(HttpServletRequest request, String viewName, String topMenu, String subMenu) {
        ModelAndView modelAndView = this.getModelAndView(request, viewName, topMenu, subMenu);
        List<Site> sites = this.getAssignedSites(request);
        List<TransactionType> transactionTypeList = new LinkedList<TransactionType>();
        for (TransactionType transactionType : TransactionType.values()) {
            transactionTypeList.add(transactionType);
        }

        List<String> applicationList = new ArrayList<String>();
        for(Code code : this.getServiceStub().getCodes(REGISTERED_APPLICATION)){
        	applicationList.add(code.getCode());
        }
        modelAndView.addObject("applicationList",this.getServiceStub().getCodes(REGISTERED_APPLICATION));
        modelAndView.addObject("sites", sites);
        modelAndView.addObject("transactionTypeList", transactionTypeList);
        modelAndView.addObject("displayFlag", false);
        return modelAndView;
    }

    protected ModelAndView searchTransactions(HttpServletRequest request,
                                              String viewName,
                                              Long siteId,
                                              String fromDate,
                                              String toDate,
                                              String transactionType,
                                              String sessionId,
                                              String topMenu, String subMenu, Integer locationId, String isCertified, String application) {
        ModelAndView modelAndView = this.getModelAndView(request, viewName, topMenu, subMenu);
        List<Site> sites = this.getAssignedSites(request);
        List<TransactionType> transactionTypeList = new LinkedList<TransactionType>();
        for (TransactionType transactionTypeItem : TransactionType.values()) {
            transactionTypeList.add(transactionTypeItem);
        }
        List<String> applicationList = new ArrayList<String>();
        for(Code code : this.getServiceStub().getCodes(REGISTERED_APPLICATION)){
        	applicationList.add(code.getCode());
        }
        modelAndView.addObject("applicationList",this.getServiceStub().getCodes(REGISTERED_APPLICATION));
        modelAndView.addObject("selectedSiteId", siteId);
        modelAndView.addObject("selectedLocationId", locationId);
        modelAndView.addObject("selectedIsCertified", isCertified);
        modelAndView.addObject("selectedApplication", application);
        modelAndView.addObject("fromDate", fromDate);
        modelAndView.addObject("toDate", toDate);
        modelAndView.addObject("selectTranType", transactionType);
        modelAndView.addObject("sites", sites);
        modelAndView.addObject("transactionTypeList", transactionTypeList);
        modelAndView.addObject("sessionId", sessionId);
        modelAndView.addObject("displayFlag", true);
        modelAndView.addObject("reportURL", requestURL);
        return modelAndView;
    }

    protected ModelAndView viewAppletJasperReport(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Long siteId,
                                         String txType,
                                         String fromDate,
                                         String toDate,
                                         String reportName,
                                         Integer locationId,
                                         String isCertified) throws Exception {
        Connection reportConn = null;
        try {
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
            if (fromDate != null && fromDate != "" && toDate != null && toDate != "") {
                reportParameters.put("FROM_DATE", fromDate);
                reportParameters.put("TO_DATE", toDate);
            }
            reportParameters.put("LOCATION_ID",  locationId);
            reportParameters.put("IS_CERTIFIED",  isCertified);
            reportParameters.put("TRAN_TYPE", txType);
            reportParameters.put("ROLE",  (this.isInternalUser(request) == true ? "PSO" : "CLIENT"));
            reportParameters.put("EXPORTFORMAT",  "APPLET");

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
                                      String transactionType,
                                      int pageIndex,
                                      String reportName,
                                      String viewName,
                                      String topMenu,
                                      String subMenu,
                                      Integer locationId,
                                      String isCertified,
                                      String application) {
        ModelAndView modelAndView = this.getModelAndView(request, viewName, topMenu, subMenu);
        int lastPageIndex = 0;
        StringBuffer stringBuffer = new StringBuffer();
        Connection reportConn = null;
        JRSwapFile jRSwapFile =  new JRSwapFile(this.reportTempFile, this.getBlockSize(), this.getMinGrowCount());
        JRSwapFileVirtualizer jRVirtualizer = new JRSwapFileVirtualizer(400, jRSwapFile, true);
        JRVirtualizationHelper.setThreadVirtualizer(jRVirtualizer);
        try {
            List<Site> sites = this.getAssignedSites(request);
            List<TransactionType> transactionTypeList = new LinkedList<TransactionType>();
            for (TransactionType transactionTypeItem : TransactionType.values()) {
                transactionTypeList.add(transactionTypeItem);
            }
            List<String> applicationList = new ArrayList<String>();
            for(Code code : this.getServiceStub().getCodes(REGISTERED_APPLICATION)){
            	applicationList.add(code.getCode());
            }
            modelAndView.addObject("applicationList",this.getServiceStub().getCodes(REGISTERED_APPLICATION));
            modelAndView.addObject("sites" , sites);
            modelAndView.addObject("transactionTypeList", transactionTypeList);
            modelAndView.addObject("fromDate", fromDate);
            modelAndView.addObject("toDate", toDate);
            modelAndView.addObject("selectedLocationId", locationId);
            modelAndView.addObject("selectedIsCertified", isCertified);
            modelAndView.addObject("selectedApplication", application);
            modelAndView.addObject("selectTranType", transactionType);
            modelAndView.addObject("pageIndex", pageIndex);
            modelAndView.addObject("selectedSiteId", siteId);
            modelAndView.addObject("displayFlag", true);

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
            reportParameters.put("ROLE",  (this.isInternalUser(request) == true ? "PSO" : "CLIENT"));
            reportParameters.put("EXPORTFORMAT",  "HTML");
            if (fromDate != null && fromDate != "" && toDate != null && toDate != "") {
                reportParameters.put("FROM_DATE", fromDate);
                reportParameters.put("TO_DATE", toDate);
            }
            reportParameters.put("LOCATION_ID", locationId);
            reportParameters.put("APPLICATION", application);
            reportParameters.put("IS_CERTIFIED", isCertified);
            reportParameters.put("TRAN_TYPE", transactionType);
            reportParameters.put("PAGE_INDEX", pageIndex);
            reportParameters.put("EXPORTFORMAT", "HTML");
            reportParameters.put(JRParameter.REPORT_VIRTUALIZER, jRVirtualizer);
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
            exporter.setParameter(JRHtmlExporterParameter.ZOOM_RATIO, 1.13f);
            exporter.setParameter(JRHtmlExporterParameter.SIZE_UNIT, JRHtmlExporterParameter.SIZE_UNIT_POINT);
            exporter.exportReport();
        } catch (Exception exception) {
            logger.error("Error Occured When Rendering the Report HTML Report",  exception);
        } finally {
            try {
                if (reportConn != null) reportConn.close();
            } catch (SQLException sQLException) {
                logger.error("Error Occured in Closing the Connection in the HTML Report",  sQLException);
            }
            /** Cleaning the Virtualizer **/
            if (jRVirtualizer != null) {
                jRSwapFile.dispose();
                jRVirtualizer.cleanup();
            }
        }
        modelAndView.addObject("stringBuffer", stringBuffer);
        modelAndView.addObject("lastPageIndex", lastPageIndex);
        return modelAndView;
    }

    protected void viewXLSX(HttpServletRequest request, HttpServletResponse response, Long siteId, String fromDate,
            String toDate, String transactionType, String reportName, String outputFileName, Integer locationId, String isCertified, String application) {
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
            reportParameters.put("REQUEST_URL", this.requestURL);
            reportParameters.put("SITE_ID", siteId);
            reportParameters.put("FROM_DATE", fromDate);
            reportParameters.put("TO_DATE", toDate);
            reportParameters.put("LOCATION_ID", locationId);
            reportParameters.put("IS_CERTIFIED", isCertified);
            reportParameters.put("APPLICATION", application);
            reportParameters.put("TRAN_TYPE", transactionType);
            reportParameters.put("EXPORTFORMAT", "XLSX");
            reportParameters.put("ROLE",  (this.isInternalUser(request) == true ? "PSO" : "CLIENT"));
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
            logger.error("Error Occured When Rendering in viewDownLoadContent",  exception);
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
                logger.error("Error Occured in Closing the Connection in viewDownLoadContent",  sQLException);
            }
            /** Cleaning the Virtualizer **/
            if (jRVirtualizer != null) {
                jRSwapFile.dispose();
                jRVirtualizer.cleanup();
            }
        }
    }

    protected void viewCSV(HttpServletRequest request, HttpServletResponse response, Long siteId, String fromDate,
            String toDate, String transactionType, String reportName, String outputFileName, Integer locationId, String isCertified, String application) {
        Connection reportConn = null;
        Map<String,Object> reportParameters = new HashMap<String, Object>();
        String extension = SystemUtil.format(new Date().toString(), "EEE MMM dd HH:mm:ss zzz yyyy", "MMM-dd-yyyy");
        outputFileName = outputFileName.concat(extension).concat(".csv");
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
            reportParameters.put("REQUEST_URL", this.requestURL);
            reportParameters.put("SITE_ID", siteId);
            reportParameters.put("FROM_DATE", fromDate);
            reportParameters.put("TO_DATE", toDate);
            reportParameters.put("LOCATION_ID", locationId);
            reportParameters.put("IS_CERTIFIED", isCertified);
            reportParameters.put("APPLICATION", application);
            reportParameters.put("TRAN_TYPE", transactionType);
            reportParameters.put("EXPORTFORMAT", "CSV");
            reportParameters.put("ROLE",  (this.isInternalUser(request) == true ? "PSO" : "CLIENT"));
            reportParameters.put(JRParameter.REPORT_VIRTUALIZER, jRVirtualizer);

            reportConn = this.datasource.getConnection();
            jasperPrint = JasperFillManager.fillReport(jasperReport, reportParameters, reportConn);

            JRCsvExporter exporter = new JRCsvExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, fbos);

            exporter.exportReport();

            /** Setting the Response Content Type **/
            response.setContentType("application/csv");
            /** Setting the Response Header **/
            response.setHeader("Content-Disposition", "attachment; filename=" + outputFileName);
            response.setContentLength(fbos.size());
            ouputStream = response.getOutputStream();
            fbos.writeData(ouputStream);
        } catch (Exception exception) {
            logger.error("Error Occured When Rendering in viewDownLoadContent",  exception);
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
                logger.error("Error Occured in Closing the Connection in viewDownLoadContent",  sQLException);
            }
            /** Cleaning the Virtualizer **/
            if (jRVirtualizer != null) {
                jRSwapFile.dispose();
                jRVirtualizer.cleanup();
            }
        }
    }

    protected void viewPDF(HttpServletRequest request, HttpServletResponse response, Long siteId, String fromDate,
            String toDate, String transactionType, String reportName, String outputFileName, Integer locationId, String isCertified, String application) {
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
            reportParameters.put("REQUEST_URL", this.requestURL);
            reportParameters.put("SITE_ID", siteId);
            reportParameters.put("FROM_DATE", fromDate);
            reportParameters.put("TO_DATE", toDate);
            reportParameters.put("LOCATION_ID", locationId);
            reportParameters.put("IS_CERTIFIED", isCertified);
            reportParameters.put("APPLICATION", application);
            reportParameters.put("TRAN_TYPE", transactionType);
            reportParameters.put("ROLE",  (this.isInternalUser(request) == true ? "PSO" : "CLIENT"));
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
            logger.error("Error Occured When Rendering in viewDownLoadContent",  exception);
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
                logger.error("Error Occured in Closing the Connection in viewDownLoadContent",  sQLException);
            }
            /** Cleaning the Virtualizer **/
            if (jRVirtualizer != null) {
                jRSwapFile.dispose();
                jRVirtualizer.cleanup();
            }
        }
    }

    protected void viewRTF(HttpServletRequest request, HttpServletResponse response, Long siteId, String fromDate,
            String toDate, String transactionType, String reportName, String outputFileName, Integer locationId, String isCertified, String application) {
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
            reportParameters.put("REQUEST_URL", this.requestURL);
            reportParameters.put("SITE_ID", siteId);
            reportParameters.put("FROM_DATE", fromDate);
            reportParameters.put("TO_DATE", toDate);
            reportParameters.put("LOCATION_ID", locationId);
            reportParameters.put("IS_CERTIFIED", isCertified);
            reportParameters.put("APPLICATION", application);
            reportParameters.put("TRAN_TYPE", transactionType);
            reportParameters.put("ROLE",  (this.isInternalUser(request) == true ? "PSO" : "CLIENT"));
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
            logger.error("Error Occured When Rendering in viewDownLoadContent",  exception);
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
                logger.error("Error Occured in Closing the Connection in viewDownLoadContent",  sQLException);
            }
            /** Cleaning the Virtualizer **/
            if (jRVirtualizer != null) {
                jRSwapFile.dispose();
                jRVirtualizer.cleanup();
            }
        }
    }

    protected String viewDownLoadContent(HttpServletRequest request, Long siteId, String fromDate, String toDate,
            String transactionType,  String exportFormat, String reportName, String forwardRequestName) {
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
            reportParameters.put("REQUEST_URL", this.requestURL);
            reportParameters.put("SITE_ID", siteId);
            reportParameters.put("FROM_DATE", fromDate);
            reportParameters.put("TO_DATE", toDate);
            reportParameters.put("TRAN_TYPE", transactionType);
            reportParameters.put("ROLE",  (this.isInternalUser(request) == true ? "PSO" : "CLIENT"));
            reportParameters.put("EXPORTFORMAT",  exportFormat);
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

	public int getBlockSize() {
		return Integer.parseInt(blockSize);
	}

	public int getMinGrowCount() {
		return Integer.parseInt(minGrowCount);
	}
}