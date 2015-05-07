package com.fdt.ecomadmin.ui.controller.reports;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_REPORTS_WEB_TRANS;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_REPORTS;
import static com.fdt.ecomadmin.ui.controller.ReportConstants.WEB_TX_RPT;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_ACCESS_DENIED;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_WEB_TRANSACTIONS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.WEB_REPORT_HTML_VIEW;

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
public class WebTxReportController extends AbstractReportController {

    /* Initial Web Transaction Form Load for Viewing Web Transactions report */
	@Link(label="Web Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewwebtransactions.admin")
    public ModelAndView viewWebTransactions(HttpServletRequest request) {
        return this.viewTransactions(request, WEB_REPORT_HTML_VIEW, TOP_REPORTS, SUB_REPORTS_WEB_TRANS);
    }

    /* Called When Web Transaction Form is Submitted */
	@Link(label="Web Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchwebtransactionsapp.admin")
    public ModelAndView searchWebTransactions(HttpServletRequest request,
                                             @RequestParam(required = false) Long siteId,
                                             @RequestParam(required = false) String dateRange,     
                                             @RequestParam(required = false) String transactionType,
                                             @RequestParam(required = false) String application,
                                             @CookieValue("JSESSIONID") String sessionId) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            return modelAndView;
        }
        return this.searchTransactions(request, VIEW_WEB_TRANSACTIONS, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, sessionId,
        		TOP_REPORTS, SUB_REPORTS_WEB_TRANS, null, null, application);
    }

	@Link(label="Web Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchwebtransactions.admin")
    public ModelAndView viewWebHtml(HttpServletRequest request,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,  
                                     @RequestParam(required = false) String transactionType,
                                     @RequestParam(required = false) String application,
                                     @RequestParam(required = false) int pageIndex) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            modelAndView.addObject("siteId", siteId);
            modelAndView.addObject("fromDate", dateRange.split("-")[0].trim());
            modelAndView.addObject("toDate", dateRange.split("-")[1].trim());
            modelAndView.addObject("transactionType", transactionType);
            modelAndView.addObject("application", application);
            return modelAndView;
        }
        String reportName = WEB_TX_RPT;
        return viewHTML(request, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, pageIndex, reportName, WEB_REPORT_HTML_VIEW,
        		TOP_REPORTS, SUB_REPORTS_WEB_TRANS, null, null, application);
    }

    /* Called From Applet to Load report. If a Specific Site is chosen, then WebPaymentsTransactionReport_SiteUser.jasper
     * is rendered, otherwise WebPaymentsTransactionReport_PSOUser.jasper is rendered.  */
	@Link(label="Web Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewwebtransactionsreport.admin", method = RequestMethod.POST)
    public ModelAndView viewWebJasperReport(HttpServletRequest request,
                                            HttpServletResponse response,
                                            @RequestParam(required = false) Long siteId,
                                            @RequestParam(required = false) String txType,
                                            @RequestParam(required = false) String fromDate,
                                            @RequestParam(required = false) String toDate) throws Exception {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            return modelAndView;
        }
        String reportName = WEB_TX_RPT;
        return this.viewAppletJasperReport(request, response, siteId, txType, fromDate, toDate, reportName, null, null);
    }

    @RequestMapping(value="/viewwebpdf.admin")
    public String viewWebPDF(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam(required = false) Long siteId,
                                  @RequestParam(required = false) String dateRange,  
                                  @RequestParam(required = false) String transactionType,
                                  @RequestParam(required = false) String application) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName = WEB_TX_RPT;
        String outputFileName = "WebPaymentsTransactionReport_";
        this.viewPDF(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, reportName, outputFileName, null, null, application);
        return null;
    }

    @RequestMapping(value="/viewwebxls.admin")
    public String viewWebXLSX(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,  
                                     @RequestParam(required = false) String transactionType,
                                     @RequestParam(required = false) String application) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName = WEB_TX_RPT;
        String outputFileName = "WebPaymentsTransactionReport_";
        this.viewXLSX(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, reportName, outputFileName, null, null, application);
        return null;
    }

    @RequestMapping(value="/viewwebcsv.admin")
    public String viewWebCSV(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,  
                                     @RequestParam(required = false) String transactionType,
                                     @RequestParam(required = false) String application) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName = WEB_TX_RPT;
        String outputFileName = "WebPaymentsTransactionReport_";
        this.viewCSV(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, reportName, outputFileName, null, null, application);
        return null;
    }

    @RequestMapping(value="/viewwebrtf.admin")
    public String viewWebRTF(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,  
                                     @RequestParam(required = false) String transactionType,
                                     @RequestParam(required = false) String application) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName = WEB_TX_RPT;
        String outputFileName = "WebPaymentsTransactionReport_";
        this.viewRTF(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, reportName, outputFileName, null, null, application);
        return null;
    }

}
