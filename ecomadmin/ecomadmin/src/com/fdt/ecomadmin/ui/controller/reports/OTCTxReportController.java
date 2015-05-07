package com.fdt.ecomadmin.ui.controller.reports;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_REPORTS_OTC_TRANS;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_REPORTS;
import static com.fdt.ecomadmin.ui.controller.ReportConstants.OTC_TX_RPT;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.OTC_REPORT_HTML_VIEW;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_ACCESS_DENIED;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_OTC_TRANSACTIONS;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.breadcrumbs.Link;

@Controller
public class OTCTxReportController extends AbstractReportController {

    /* Initial OTC Transaction Form Load for Viewing OTC Transactions report */
	@Link(label="Over The Counter Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewotctransactions.admin")
    public ModelAndView viewOTCTransactions(HttpServletRequest request) {
        return this.viewTransactions(request, OTC_REPORT_HTML_VIEW, TOP_REPORTS, SUB_REPORTS_OTC_TRANS);
    }

    /* Called When OTC Transaction Form is Submitted */
	@Link(label="Over The Counter Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchotctransactionsapp.admin")
    public ModelAndView searchOTCTransactions(HttpServletRequest request,
                                             @RequestParam(required = false) Long siteId,
                                             @RequestParam(required = false) String dateRange,  
                                             @RequestParam(required = false) String transactionType,
                                             @CookieValue("JSESSIONID") String sessionId) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            return modelAndView;
        }        
        return this.searchTransactions(request, VIEW_OTC_TRANSACTIONS, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, sessionId,
        		TOP_REPORTS, SUB_REPORTS_OTC_TRANS, null, null, null);
    }

	@Link(label="Over The Counter Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchotctransactions.admin")
    public ModelAndView viewOTCHTML(HttpServletRequest request,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,  
                                     @RequestParam(required = false) String transactionType,
                                     @RequestParam(required = false) int pageIndex) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            modelAndView.addObject("siteId", siteId);
            modelAndView.addObject("fromDate", dateRange.split("-")[0].trim());
            modelAndView.addObject("toDate", dateRange.split("-")[1].trim());
            modelAndView.addObject("transactionType", transactionType);
            return modelAndView;
        }
        String reportName =  OTC_TX_RPT;        
        return viewHTML(request, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), 
        		transactionType, pageIndex, reportName, OTC_REPORT_HTML_VIEW, TOP_REPORTS, SUB_REPORTS_OTC_TRANS, null, null, null);
    }

    /* Called From Applet to Load report. If a Specific Site is chosen, then OTCTransactionReport_SiteUser.jasper
     * is rendered, otherwise OTCTransactionReport_PSOUser.jasper is rendered.  */
	@Link(label="Over The Counter Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewotctransactionsreport.admin", method = RequestMethod.POST)
    public ModelAndView viewOTCJasperReport(HttpServletRequest request,
                                            HttpServletResponse response,
                                            @RequestParam(required = false) Long siteId,
                                            @RequestParam(required = false) String txType,
                                            @RequestParam(required = false) String fromDate,
                                            @RequestParam(required = false) String toDate) throws Exception {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            return modelAndView;
        }
        if (fromDate == "" || toDate == "") {
            fromDate = null;
            toDate = null;
        }
        String reportName =  OTC_TX_RPT;
        return this.viewAppletJasperReport(request, response, siteId, txType, fromDate, toDate, reportName,  null, null);

    }

    @RequestMapping(value="/viewotcpdf.admin")
    public String viewOTCPdf(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam(required = false) Long siteId,
                                  @RequestParam(required = false) String dateRange, 
                                  @RequestParam(required = false) String transactionType) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName =  OTC_TX_RPT;
        String outputFileName = "OTCTransactionReport_";
        this.viewPDF(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, 
        		reportName, outputFileName, null, null, null);
        return null;
    }

    @RequestMapping(value="/viewotcxls.admin")
    public String viewOTCXLSX(HttpServletRequest request, HttpServletResponse response,
                                   	 @RequestParam(required = false) Long siteId,
                                   	 @RequestParam(required = false) String dateRange, 
                                     @RequestParam(required = false) String transactionType) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName =  OTC_TX_RPT;
        String outputFileName = "OTCTransactionReport_";
        this.viewXLSX(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, 
        		reportName, outputFileName, null, null, null);
        return null;
    }

    @RequestMapping(value="/viewotccsv.admin")
    public String viewOTCCSV(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange, 
                                     @RequestParam(required = false) String transactionType) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName =  OTC_TX_RPT;
        String outputFileName = "OTCTransactionReport_";
        this.viewCSV(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, 
        		reportName, outputFileName, null, null, null);
        return null;
    }

    @RequestMapping(value="/viewotcrtf.admin")
    public String viewOTCrtf(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange, 
                                     @RequestParam(required = false) String transactionType) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName =  OTC_TX_RPT;
        String outputFileName = "OTCTransactionReport_";
        this.viewRTF(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, 
        		reportName, outputFileName, null, null, null);
        return null;
    }
}
