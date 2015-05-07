package com.fdt.ecomadmin.ui.controller.reports;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_REPORTS_GTR;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_REPORTS;
import static com.fdt.ecomadmin.ui.controller.ReportConstants.GT_RPT;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.GRAND_TOTAL_REPORT_HTML_VIEW;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_ACCESS_DENIED;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_GRAND_TOTAL_REPORT_DETAILS;

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
public class GTReportController extends AbstractBaseController  {

    @Value("${email.useractivation.requesturl}")
    protected String requestURL = null;

    @Value("${jasper.tempfile.path}")
    private String reportTempFile = null;

    @Value("${jasper.tempfile.blocksize}")
    private String blockSize;

    @Value("${jasper.tempfile.mingrowcount}")
    private String minGrowCount;

    @Autowired
    protected DataSource datasource = null;

    /* Initial Grand Total Report Form Load for Viewing grandtotalreport report */
    @Link(label="Grand Total Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewgrandtotalreport.admin")
    public ModelAndView viewgrandTotalDetails(HttpServletRequest request) {
        return this.viewGrandTotalDetails(request, GRAND_TOTAL_REPORT_HTML_VIEW, TOP_REPORTS, SUB_REPORTS_GTR);
    }

    /* Called When grandtotalreport Form is Submitted */
    @Link(label="Grand Total Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchgrandtotalreportapp.admin")
    public ModelAndView searchGrandTotalDetails(HttpServletRequest request,
                                             @RequestParam(required = false) Long siteId,
                                             @RequestParam(required = false) String dateRange,
                                             @CookieValue("JSESSIONID") String sessionId) {
        if (!checkLoggedinUserSiteValidity(request, siteId) || !this.isFeatureEnabledForUser(request, "GTReport")) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            return modelAndView;
        }

        return this.searchTransactions(request, VIEW_GRAND_TOTAL_REPORT_DETAILS, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), sessionId,
        		TOP_REPORTS, SUB_REPORTS_GTR);

    }

    @Link(label="Grand Total Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchgrandtotalreport.admin")
    public ModelAndView viewGrandTotalDetailsHTML(HttpServletRequest request,
                                   @RequestParam(required = false) Long siteId,
                                   @RequestParam(required = false) String dateRange,
                                   @RequestParam(required = false) int pageIndex) {
        if (!checkLoggedinUserSiteValidity(request, siteId) || !this.isFeatureEnabledForUser(request, "GTReport")) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            modelAndView.addObject("siteId", siteId);
            modelAndView.addObject("fromDate", dateRange.split("-")[0].trim());
            modelAndView.addObject("toDate", dateRange.split("-")[1].trim());
            return modelAndView;
        }
        String reportName =  GT_RPT;
        return viewHTML(request, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), pageIndex, reportName, GRAND_TOTAL_REPORT_HTML_VIEW, TOP_REPORTS, SUB_REPORTS_GTR);
    }

    /* Called From Applet to Load report. If a Specific Site is chosen, then grandtotalreportTransactionReport_SiteUser.jasper
     * is rendered, otherwise grandtotalreportTransactionReport_PSOUser.jasper is rendered.  */
    @Link(label="Grand Total Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewgrandtotalreportreport.admin", method = RequestMethod.POST)
    public ModelAndView viewGrandTotalDetailsJasperReport(HttpServletRequest request,
                                            HttpServletResponse response,
                                             @RequestParam(required = false) Long siteId,
                                             @RequestParam(required = false) String fromDate,
                                             @RequestParam(required = false) String toDate) throws Exception {
        if (!checkLoggedinUserSiteValidity(request, siteId) || !this.isFeatureEnabledForUser(request, "GTReport")) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            return modelAndView;
        }
        if (fromDate == "" || toDate == "") {
            fromDate = null;
            toDate = null;
        }
        String reportName =  GT_RPT;
        return this.viewAppletJasperReport(request, response, siteId, fromDate, toDate, reportName);

    }

    @RequestMapping(value="/viewgrandtotalreportpdf.admin")
    public String viewGrandTotaldetailsPdf(HttpServletRequest request,
								   HttpServletResponse response,
                                   @RequestParam(required = false) Long siteId,
                                   @RequestParam(required = false) String dateRange) {
        if (!checkLoggedinUserSiteValidity(request, siteId) || !this.isFeatureEnabledForUser(request, "GTReport")) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName =  GT_RPT;
        String outputFileName = "GTReport_";
        this.viewPDF(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), reportName, outputFileName);
        return null;
    }

    @RequestMapping(value="/viewgrandtotalreportxls.admin")
    public String viewGrandTotaldetailsXLSX(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(required = false) Long siteId,
                                   @RequestParam(required = false) String dateRange) {
        if (!checkLoggedinUserSiteValidity(request, siteId) || !this.isFeatureEnabledForUser(request, "GTReport")) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName =  GT_RPT;
        String outputFileName = "GTReport_";
        this.viewXLSX(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), reportName, outputFileName);
        return null;
    }

    @RequestMapping(value="/viewgrandtotalreportrtf.admin")
    public String viewGrandTotaldetailsRtf(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(required = false) Long siteId,
                                   @RequestParam(required = false) String dateRange) {
        if (!checkLoggedinUserSiteValidity(request, siteId) || !this.isFeatureEnabledForUser(request, "GTReport")) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName =  GT_RPT;
        String outputFileName = "GTReport_";
        this.viewRTF(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), reportName, outputFileName);
        return null;
    }

    private ModelAndView viewGrandTotalDetails(HttpServletRequest request, String viewName, String topMenu, String subMenu) {
        ModelAndView modelAndView = this.getModelAndView(request, viewName, topMenu, subMenu);
        List<Site> sites = this.getAssignedSites(request);
        modelAndView.addObject("sites", sites);
        modelAndView.addObject("displayFlag", false);
        return modelAndView;
    }

    private ModelAndView searchTransactions(HttpServletRequest request,
                                              String viewName,
                                              Long siteId,
                                              String fromDate,
                                              String toDate,
                                              String sessionId,
                                              String topMenu, String subMenu) {
        ModelAndView modelAndView = this.getModelAndView(request, viewName, topMenu, subMenu);
        List<Site> sites = this.getAssignedSites(request);
        modelAndView.addObject("selectedSiteId", siteId);
        modelAndView.addObject("fromDate", fromDate);
        modelAndView.addObject("toDate", toDate);
        modelAndView.addObject("sites", sites);
        modelAndView.addObject("sessionId", sessionId);
        modelAndView.addObject("displayFlag", true);
        modelAndView.addObject("reportURL", requestURL);
        return modelAndView;
    }

    private ModelAndView viewAppletJasperReport(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Long siteId,
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
            reportParameters.put("SITE_ID", siteId);
            reportParameters.put("ROLE",  (this.isInternalUser(request) == true ? "PSO" : "CLIENT"));
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

    /**
     * This method is different from the one in Abstract Report Controller as it does not send the Transaction Parameter.
     *
     * @param request
     * @param siteId
     * @param fromDate
     * @param toDate
     * @param transactionType
     * @param pageIndex
     * @param reportName
     * @param viewName
     * @return
     */
    private ModelAndView viewHTML(HttpServletRequest request,
                                    Long siteId,
                                      String fromDate,
                                      String toDate,
                                      int pageIndex,
                                      String reportName,
                                      String viewName,
                                      String topMenu, String subMenu) {
        ModelAndView modelAndView = this.getModelAndView(request, viewName);
        int lastPageIndex = 0;
        StringBuffer stringBuffer = new StringBuffer();
        Connection reportConn = null;
        try {
            List<Site> sites = this.getAssignedSites(request);
            modelAndView.addObject("topMenu", topMenu);
    		modelAndView.addObject("subMenu", subMenu);
            modelAndView.addObject("sites" , sites);
            modelAndView.addObject("fromDate", fromDate);
            modelAndView.addObject("toDate", toDate);
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
            exporter.setParameter(JRHtmlExporterParameter.ZOOM_RATIO, 1.4f);
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

    private void viewXLSX(HttpServletRequest request, HttpServletResponse response, Long siteId, String fromDate,
    		String toDate,	String reportName, String outputFileName) {
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

            if (jRVirtualizer != null) {
                jRSwapFile.dispose();
                jRVirtualizer.cleanup();
            }
        }
    }

    private void viewPDF(HttpServletRequest request, HttpServletResponse response, Long siteId, String fromDate,
    		String toDate, String reportName, String outputFileName) {
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

    private void viewRTF(HttpServletRequest request, HttpServletResponse response, Long siteId, String fromDate,
    		String toDate, String reportName, String outputFileName) {
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

    public int getBlockSize() {
		return Integer.parseInt(blockSize);
	}

	public int getMinGrowCount() {
		return Integer.parseInt(minGrowCount);
	}

}
