package com.fdt.ecomadmin.ui.controller.reports;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_REPORTS_RECURRING_TRANS;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_REPORTS;
import static com.fdt.ecomadmin.ui.controller.ReportConstants.RECUR_TX_RPT;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.RECURRING_REPORT_HTML_VIEW;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_ACCESS_DENIED;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_RECURRING_TRANSACTIONS;

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
public class RecurringTxReportController extends AbstractReportController {

    /* Initial Recurring Transaction Form Load for Viewing Recurring Transactions report */
	@Link(label="Recurring Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewrecurringtransactions.admin")
    public ModelAndView viewRecurringTransactions(HttpServletRequest request) {
        return this.viewTransactions(request, RECURRING_REPORT_HTML_VIEW, TOP_REPORTS, SUB_REPORTS_RECURRING_TRANS);
    }

	@Link(label="Recurring Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    /* Called When Recurring Transaction Form is Submitted */
    @RequestMapping(value="/searchrecurringtransactionsapp.admin")
    public ModelAndView searchRecurringTransactions(HttpServletRequest request,
                                             @RequestParam(required = false) Long siteId,
                                             @RequestParam(required = false) String dateRange,                                             
                                             @RequestParam(required = false) String transactionType,
                                             @CookieValue("JSESSIONID") String sessionId) {
        if (!checkLoggedinUserSiteValidity(request, siteId) || !this.isFeatureEnabledForUser(request, "TransactionLookup")) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            return modelAndView;
        }
        return this.searchTransactions(request, VIEW_RECURRING_TRANSACTIONS, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), 
        		transactionType, sessionId, TOP_REPORTS, SUB_REPORTS_RECURRING_TRANS, null, null, null);
    }

	@Link(label="Recurring Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchrecurringtransactions.admin")
    public ModelAndView viewRecurringHtml(HttpServletRequest request,
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
        String reportName =  RECUR_TX_RPT;
        return viewHTML(request, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, pageIndex, reportName, RECURRING_REPORT_HTML_VIEW,
        		TOP_REPORTS, SUB_REPORTS_RECURRING_TRANS, null, null, null);
    }

	@Link(label="Recurring Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    /* Called From Applet to Load report. If a Specific Site is chosen, then RecurringTransactionReport_SiteUser.jasper
     * is rendered, otherwise RecurringTransactionReport_PSOUser.jasper is rendered.  */
    @RequestMapping(value="/viewrecurringtransactionsreport.admin", method = RequestMethod.POST)
    public ModelAndView viewRecurringJasperReport(HttpServletRequest request,
                                            HttpServletResponse response,
                                            @RequestParam(required = false) Long siteId,
                                            @RequestParam(required = false) String txType,
                                            @RequestParam(required = false) String fromDate,
                                            @RequestParam(required = false) String toDate) throws Exception {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            return modelAndView;
        }
        String reportName =  RECUR_TX_RPT;
        return this.viewAppletJasperReport(request, response, siteId, txType, fromDate, toDate, reportName, null, null);
    }

    @RequestMapping(value="/viewrecurringpdf.admin")
    public String viewRecurringPDF(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam(required = false) Long siteId,
                                  @RequestParam(required = false) String dateRange,  
                                  @RequestParam(required = false) String transactionType) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName =  RECUR_TX_RPT;
        String outputFileName = "RecurringTransactionReport_";
        this.viewPDF(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, reportName, outputFileName, null, null, null);
        return null;
    }

    @RequestMapping(value="/viewrecurringxls.admin")
    public String viewRecurringXLSX(HttpServletRequest request, HttpServletResponse response,
    								 @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,  
                                     @RequestParam(required = false) String transactionType) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName =  RECUR_TX_RPT;
        String outputFileName = "RecurringTransactionReport_";
        this.viewXLSX(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, reportName, outputFileName, null, null, null);
        return null;
    }

    @RequestMapping(value="/viewrecurringcsv.admin")
    public String viewRecurringCSV(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(required = false) Long siteId,
                                   @RequestParam(required = false) String dateRange,  
                                     @RequestParam(required = false) String transactionType) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName =  RECUR_TX_RPT;
        String outputFileName = "RecurringTransactionReport_";
        this.viewCSV(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, reportName, outputFileName, null, null, null);
        return null;
    }

    @RequestMapping(value="/viewrecurringrtf.admin")
    public String viewRecurringRTF(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,  
                                     @RequestParam(required = false) String transactionType) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName =  RECUR_TX_RPT;
        String outputFileName = "RecurringTransactionReport_";
        this.viewRTF(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, reportName, outputFileName, null, null, null);
        return null;
    }

}
