package com.fdt.ecomadmin.ui.controller.reports;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_REPORTS_DETAIL_DOCUMENT_TRANS;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_REPORTS_PAYASUGO_TRANS;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_REPORTS;
import static com.fdt.ecomadmin.ui.controller.ReportConstants.DETAIL_DOCUMENT_TX_RPT;
import static com.fdt.ecomadmin.ui.controller.ReportConstants.PAYASUGO_TX_RPT;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.DETAIL_DOCUMENT_TRANSACTIONS_HTML_VIEW;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.PAYASUGO_REPORT_HTML_VIEW;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_ACCESS_DENIED;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_DETAIL_DOCUMENT_TRANSACTIONS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_PAYASUGO_TRANSACTIONS;

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
public class PayAsUGoTxReportController extends AbstractReportController {

    /* Initial PayAsUGo Transaction Form Load for Viewing PayAsUGo Transactions report */
	@Link(label="Pay As You Go Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewpayasugotransactions.admin")
    public ModelAndView viewPayAsUGoTransactions(HttpServletRequest request) {
        return this.viewTransactions(request, PAYASUGO_REPORT_HTML_VIEW, TOP_REPORTS, SUB_REPORTS_PAYASUGO_TRANS);
    }

	@Link(label="Detail Document Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewDetailDocumentTransactions.admin")
    public ModelAndView viewDetailDocumentTransactions(HttpServletRequest request) {
        return this.viewTransactions(request, DETAIL_DOCUMENT_TRANSACTIONS_HTML_VIEW, TOP_REPORTS, SUB_REPORTS_DETAIL_DOCUMENT_TRANS);
    }

	/* Called When PayAsUGo Transaction Form is Submitted */
	@Link(label="Pay As You Go Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchpayasugotransactionsapp.admin")
    public ModelAndView searchPayAsUGoTransactions(HttpServletRequest request,
                                             @RequestParam(required = false) Long siteId,
                                             @RequestParam(required = false) String dateRange,
                                             @RequestParam(required = false) String transactionType,
                                             @RequestParam(required = false) String isCertified,
                                             @CookieValue("JSESSIONID") String sessionId) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            return modelAndView;
        }
        return this.searchTransactions(request, VIEW_PAYASUGO_TRANSACTIONS, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, sessionId
        		,TOP_REPORTS, SUB_REPORTS_PAYASUGO_TRANS, null, isCertified, null);
    }

	/* Called When PayAsUGo Transaction Form is Submitted */
	@Link(label="Detail Document Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchDetailDocumentTransactionsApp.admin")
    public ModelAndView searchDetailDocumentTransactionsApp(HttpServletRequest request,
                                             @RequestParam(required = false) Long siteId,
                                             @RequestParam(required = false) String dateRange,
                                             @RequestParam(required = false) String transactionType,
                                             @RequestParam(required = false) Integer locationId,
                                             @RequestParam(required = false) String isCertified,
                                             @CookieValue("JSESSIONID") String sessionId) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            return modelAndView;
        }
        return this.searchTransactions(request, VIEW_DETAIL_DOCUMENT_TRANSACTIONS, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, sessionId
        		,TOP_REPORTS, SUB_REPORTS_DETAIL_DOCUMENT_TRANS, locationId, isCertified, null);
    }

	@Link(label="Pay As You Go Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchpayasugotransactions.admin")
    public ModelAndView viewPayAsUGoHtml(HttpServletRequest request,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,
                                     @RequestParam(required = false) String transactionType,
                                     @RequestParam(required = false) String isCertified,
                                     @RequestParam(required = false) int pageIndex) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            modelAndView.addObject("siteId", siteId);
            modelAndView.addObject("fromDate", dateRange.split("-")[0].trim());
            modelAndView.addObject("toDate", dateRange.split("-")[1].trim());
            modelAndView.addObject("transactionType", transactionType);
            return modelAndView;
        }
        String reportName = PAYASUGO_TX_RPT;
        return viewHTML(request, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(),
        		transactionType, pageIndex, reportName, PAYASUGO_REPORT_HTML_VIEW,
        		TOP_REPORTS, SUB_REPORTS_PAYASUGO_TRANS, null, isCertified, null);
    }


	@Link(label="Detail Document Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/searchDetailDocumentTransactions.admin")
    public ModelAndView viewDetailDocumentHtml(HttpServletRequest request,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,
                                     @RequestParam(required = false) String transactionType,
                                     @RequestParam(required = false) int pageIndex,
                                     @RequestParam(required = false) Integer locationId,
                                     @RequestParam(required = false) String isCertified) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            modelAndView.addObject("siteId", siteId);
            modelAndView.addObject("fromDate", dateRange.split("-")[0].trim());
            modelAndView.addObject("toDate", dateRange.split("-")[1].trim());
            modelAndView.addObject("transactionType", transactionType);
            return modelAndView;
        }
        String reportName = DETAIL_DOCUMENT_TX_RPT;
        return viewHTML(request, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, pageIndex, reportName, DETAIL_DOCUMENT_TRANSACTIONS_HTML_VIEW,
        		TOP_REPORTS, SUB_REPORTS_DETAIL_DOCUMENT_TRANS, locationId, isCertified, null);
    }

	/* Called From Applet to Load report. If a Specific Site is chosen, then PayAsUGoPaymentsTransactionReport_SiteUser.jasper
     * is rendered, otherwise PayAsUGoPaymentsTransactionReport_PSOUser.jasper is rendered.  */
	@Link(label="Pay As You Go Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewpayasugotransactionsreport.admin", method = RequestMethod.POST)
    public ModelAndView viewPayAsUGoJasperReport(HttpServletRequest request,
                                            HttpServletResponse response,
                                            @RequestParam(required = false) Long siteId,
                                            @RequestParam(required = false) String txType,
                                            @RequestParam(required = false) String fromDate,
                                            @RequestParam(required = false) String toDate,
                                            @RequestParam(required = false) String isCertified) throws Exception {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            return modelAndView;
        }
        String reportName = PAYASUGO_TX_RPT;
        return this.viewAppletJasperReport(request, response, siteId, txType, fromDate, toDate,  reportName, null, isCertified);
    }

	/* Called From Applet to Load report. If a Specific Site is chosen, then PayAsUGoPaymentsTransactionReport_SiteUser.jasper
     * is rendered, otherwise PayAsUGoPaymentsTransactionReport_PSOUser.jasper is rendered.  */
	@Link(label="Detail Document Transaction Report", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/viewDetailDocumentTransactionsReport.admin", method = RequestMethod.POST)
    public ModelAndView viewDetailDocumentJasperReport(HttpServletRequest request,
                                            HttpServletResponse response,
                                            @RequestParam(required = false) Long siteId,
                                            @RequestParam(required = false) String txType,
                                            @RequestParam(required = false) String fromDate,
                                            @RequestParam(required = false) String toDate,
                                            @RequestParam(required = false) Integer locationId,
                                            @RequestParam(required = false) String isCertified) throws Exception {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            ModelAndView modelAndView = new ModelAndView(REDIRECT_ACCESS_DENIED);
            return modelAndView;
        }
        String reportName = DETAIL_DOCUMENT_TX_RPT;
        return this.viewAppletJasperReport(request, response, siteId, txType, fromDate, toDate, reportName, locationId, isCertified);
    }

	@RequestMapping(value="/viewpayasugopdf.admin")
    public String viewPayAsUGoPDF(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam(required = false) Long siteId,
                                  @RequestParam(required = false) String dateRange,
                                  @RequestParam(required = false) String transactionType,
                                  @RequestParam(required = false) String isCertified) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName = PAYASUGO_TX_RPT;
        String outputFileName = "PayAsUGoPaymentsTransactionReport_";
        this.viewPDF(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), transactionType, reportName, outputFileName, null, isCertified, null);
        return null;
    }

	@RequestMapping(value="/viewDetailDocumentPdf.admin")
    public String viewDetailDocumentPDF(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam(required = false) Long siteId,
                                  @RequestParam(required = false) String dateRange,
                                  @RequestParam(required = false) String transactionType,
                                  @RequestParam(required = false) Integer locationId,
                                  @RequestParam(required = false) String isCertified) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName = DETAIL_DOCUMENT_TX_RPT;
        String outputFileName = "DetailDocumentsTransactionReport_";
        this.viewPDF(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(),  transactionType, reportName, outputFileName, locationId, isCertified, null);
        return null;
    }

	@RequestMapping(value="/viewpayasugoxls.admin")
    public String viewPayAsUGoXLS(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,
                                     @RequestParam(required = false) String transactionType,
                                     @RequestParam(required = false) String isCertified) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName = PAYASUGO_TX_RPT;
        String outputFileName = "PayAsUGoPaymentsTransactionReport_";
        this.viewXLSX(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(),  transactionType, reportName, outputFileName, null, isCertified, null);
        return null;
    }

	@RequestMapping(value="/viewDetailDocumentXls.admin")
    public String viewDetailDocumentXLS(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,
                                     @RequestParam(required = false) String transactionType,
                                     @RequestParam(required = false) Integer locationId,
                                     @RequestParam(required = false) String isCertified) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName = DETAIL_DOCUMENT_TX_RPT;
        String outputFileName = "DetailDocumentsTransactionReport_";
        this.viewXLSX(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(),  transactionType, reportName, outputFileName, locationId, isCertified, null);
        return null;
    }

	@RequestMapping(value="/viewpayasugocsv.admin")
    public String viewPayAsUGoCSV(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,
                                     @RequestParam(required = false) String transactionType,
                                     @RequestParam(required = false) String isCertified) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName = PAYASUGO_TX_RPT;
        String outputFileName = "PayAsUGoPaymentsTransactionReport_";
        this.viewCSV(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(),  transactionType, reportName, outputFileName, null, isCertified, null);
        return null;
    }

	@RequestMapping(value="/viewDetailDocumentCsv.admin")
    public String viewDetailDocumentCSV(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,
                                     @RequestParam(required = false) String transactionType,
                                     @RequestParam(required = false) Integer locationId,
                                     @RequestParam(required = false) String isCertified) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName = DETAIL_DOCUMENT_TX_RPT;
        String outputFileName = "DetailDocumentsTransactionReport_";
        this.viewCSV(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(),  transactionType, reportName, outputFileName, locationId, isCertified, null);
        return null;
    }

	@RequestMapping(value="/viewpayasugortf.admin")
    public String viewPayAsUGoRTF(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,
                                     @RequestParam(required = false) String transactionType,
                                     @RequestParam(required = false) String isCertified) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName = PAYASUGO_TX_RPT;
        String outputFileName = "PayAsUGoPaymentsTransactionReport_";
        this.viewRTF(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(),  transactionType, reportName, outputFileName, null, isCertified, null);
        return null;
    }

	@RequestMapping(value="/viewDetailDocumentRtf.admin")
    public String viewDetailDocumentRTF(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) Long siteId,
                                     @RequestParam(required = false) String dateRange,
                                     @RequestParam(required = false) String transactionType,
                                     @RequestParam(required = false) Integer locationId,
                                     @RequestParam(required = false) String isCertified) {
        if (!checkLoggedinUserSiteValidity(request, siteId)) {
            return REDIRECT_ACCESS_DENIED;
        }
        String reportName = DETAIL_DOCUMENT_TX_RPT;
        String outputFileName = "DetailDocumentsTransactionReport_";
        this.viewRTF(request, response, siteId, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(),  transactionType, reportName, outputFileName, locationId, isCertified, null);
        return null;
    }

}
