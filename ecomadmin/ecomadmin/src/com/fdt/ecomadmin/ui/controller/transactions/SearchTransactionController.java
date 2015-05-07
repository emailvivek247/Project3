package com.fdt.ecomadmin.ui.controller.transactions;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_REPORTS_TRANS_LOOKUP;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_REPORTS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.CONFIRM_PARTIAL_REFUND;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.CONFIRM_REFUND;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.EXCEL;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.GENERAL_ERROR;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.PARTIAL_REFUND_STATUS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.PAY_AS_U_GO;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.PDF;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_ACCESS_DENIED;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_PARTIAL_REFUND_STATUS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_REFUND_STATUS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REFUND_STATUS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.SEARCH_TRANSACTION;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_TRANSACTION_DETAILS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.WEB;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.entity.Tx;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.export.ExcelExport;
import com.fdt.common.export.PDFCell;
import com.fdt.common.export.PDFExport;
import com.fdt.common.ui.breadcrumbs.Link;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.common.util.SystemUtil;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.enums.PaymentType;
import com.fdt.ecom.entity.enums.TransactionType;
import com.fdt.ecomadmin.ui.controller.util.ExportConstants;
import com.fdt.otctx.entity.OTCTx;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.recurtx.entity.RecurTx;
import com.fdt.webtx.entity.WebTx;

@Controller
public class SearchTransactionController extends AbstractBaseController {
	
	private static int[] TX_PDF_COLUMN_WIDTHS = new int[] {14, 10, 10, 8, 14, 8, 8};
	
	@Link(label="Transaction Details", family="ACCEPTADMIN", parent = "Transaction Lookup" )
	@RequestMapping(value="/viewtransactiondetails.admin")
	public ModelAndView viewTransactionDetails(HttpServletRequest request,
			@RequestParam(required = false) String transactionRefNum,
			@RequestParam(required = false) String paymentChannel) {
		ModelAndView modelAndView = this.getModelAndView(request, VIEW_TRANSACTION_DETAILS, TOP_REPORTS, SUB_REPORTS_TRANS_LOOKUP);
		String siteName = null;
		if (!this.isInternalUser(request)) {
			List<Site> sites = this.getAssignedSites(request);
			for (Site site : sites) {
				siteName = site.getName();
			}
		}
		if ((transactionRefNum != null && transactionRefNum != "") &&  (paymentChannel != null && paymentChannel != "")){
			if (paymentChannel.equals(PaymentType.OTC.toString())) {
				OTCTx transaction = this.getServiceStub().getOTCTransactionByTxRefNum(transactionRefNum, siteName);
				List<OTCTx> transactionList =  new LinkedList<OTCTx>();
				if (transaction != null) {
					transactionList.add(transaction);
				}
				modelAndView.addObject("transactionList", transactionList);
			} else if (paymentChannel.equals(PaymentType.WEB.toString())) {
				WebTx transaction = this.getServiceStub().getWebTxByTxRefNum(transactionRefNum, siteName);
				List<WebTx> transactionList =  new LinkedList<WebTx>();
				if (transaction != null) {
					transactionList.add(transaction);
				}
				modelAndView.addObject("transactionList", transactionList);
			} else if (paymentChannel.equals(PaymentType.PAYASUGO.toString())) {
				PayAsUGoTx transaction = this.getServiceStub().getPayAsUGoTransactionByTxRefNum(transactionRefNum, siteName);
				List<PayAsUGoTx> transactionList =  new LinkedList<PayAsUGoTx>();
				if (transaction != null) {
					transactionList.add(transaction);
				}
				modelAndView.addObject("transactionList", transactionList);
			}
			else if (paymentChannel.equals(PaymentType.RECURRING.toString())) {
				List<RecurTx> transactionList = this.getServiceStub().
						getRecurringTransactionByTxRefNum(transactionRefNum, siteName);
				modelAndView.addObject("transactionList", transactionList);
			}
			modelAndView.addObject("transactionRefNum", transactionRefNum);
			modelAndView.addObject("paymentChannel", paymentChannel);
		}
		return modelAndView;
	}

	@Link(label="Transaction Lookup", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/searchtransaction.admin")
	public ModelAndView searchTransaction(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, SEARCH_TRANSACTION, TOP_REPORTS, SUB_REPORTS_TRANS_LOOKUP);
		return modelAndView;
	}
	
    @RequestMapping(value="/lookupTransactions.admin", method=RequestMethod.GET,  produces="application/json")
	@ResponseBody
    public PageRecordsDTO getTransactions(HttpServletRequest request, String transactionRefNum,
			String accountName,
			String accountNumber,
			String dateRange,
			String productId,
			String productName,
			String productType,
			String invoiceId,
			String paymentChannel,
			Integer skip, 
			Integer take){
    	String fromDate = null;
    	String toDate = null;
    	if(!StringUtils.isBlank(dateRange)){
    		fromDate = dateRange.split("-")[0].trim();
    		toDate = dateRange.split("-")[1].trim();
    	}
    	return this.getServiceStub().lookupTx(productId, productName, productType, invoiceId, transactionRefNum, accountName,
				accountNumber, fromDate, toDate, paymentChannel, null, skip, take);
    }	
	
    @RequestMapping(value="/exportLookupTransactions.admin")
    public void exportLookupTransactions(HttpServletRequest request, HttpServletResponse response, String transactionRefNum,
			String accountName,
			String accountNumber,
			String dateRange,
			String productId,
			String productName,
			String productType,
			String invoiceId,
			String paymentChannel,
			String exportType
			){
    	
    	PageRecordsDTO page =  this.getServiceStub().lookupTx(productId, productName, productType, invoiceId, transactionRefNum, accountName,
				accountNumber, dateRange.split("-")[0].trim(), dateRange.split("-")[1].trim(), paymentChannel, null, 0, Integer.MAX_VALUE);
    	Collection<Tx> txList = (Collection<Tx>)page.getRecords();
        byte[] outputBytes = new byte[1];
        String fileExtension = null;
        try{
	        if(exportType.equals(EXCEL)){
	        	// Create Excel File contents
	        	List<List<String>> transactions = new ArrayList<List<String>>();
	        	for(Tx tx : txList){
	        		transactions.add(this.getTxRowForExcel(tx));
	        	}
	        	ExcelExport export = new ExcelExport();
	        	outputBytes = export.exportToExcel(ExportConstants.getTxHeaders(), transactions);
	        	fileExtension = "xls";
	        	
	        } else if (exportType.equals(PDF)){
	        	// Create PDF file contents
	        	List<List<PDFCell>> transactions = new ArrayList<List<PDFCell>>();
	        	for(Tx tx : txList){
	        		transactions.add(this.getTxRowForPDF(tx));
	        	}
	        	PDFExport export = new PDFExport(TX_PDF_COLUMN_WIDTHS.length, TX_PDF_COLUMN_WIDTHS);
	        	outputBytes = export.exportToPDF(ExportConstants.getTxHeaders(), transactions);
	        	fileExtension = "pdf";	        	
	        } else {
	        	logger.error("Invalid export Type passed to exportLookupTransactions");
	        }
	        response.reset();
			response.resetBuffer();
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition",  "attachment; filename=Transactions." + fileExtension);
			response.setHeader("Expires", " 0");
			response.setHeader("Cache-Control", " must-revalidate, post-check=0, pre-check=0" );
			response.setHeader("Pragma" , "public");
			ByteArrayOutputStream outputStream =  new ByteArrayOutputStream();
			outputStream.write(outputBytes);
			outputStream.writeTo(response.getOutputStream());
			response.getOutputStream().flush();
        } catch(Exception e){
        	logger.error("Error while writing exporting Recurring Transactions " , e);
        }
    	
    }	
	



	@RequestMapping(value="/confirmrefundtransaction.admin")
	public ModelAndView getReferencedTransaction(HttpServletRequest request,
			@RequestParam(required = false) String transactionRefNum,
			@RequestParam(required = false) String paymentChannel) {
		ModelAndView modelAndView = this.getModelAndView(request, CONFIRM_REFUND, TOP_REPORTS, SUB_REPORTS_TRANS_LOOKUP);

		if (!this.isFeatureEnabledForUser(request, "RefundTransaction")) {
			modelAndView.setViewName(REDIRECT_ACCESS_DENIED);
			return modelAndView;
		}

		String siteName = null;
		if (!this.isInternalUser(request)) {
			List<Site> sites = this.getAssignedSites(request);
			for (Site site : sites) {
				siteName = site.getName();
			}
		}
		if ((transactionRefNum != null && transactionRefNum != "") &&  (paymentChannel != null && paymentChannel != "")) {
			if (paymentChannel.equals(PaymentType.OTC.toString())) {
				OTCTx referencedTransaction
					= this.getServiceStub().getReferencedOTCTransaction(transactionRefNum, siteName);
				modelAndView.addObject("referencedTransaction", referencedTransaction);
				if (referencedTransaction == null) {
					OTCTx transaction
						= this.getServiceStub().getOTCTransactionByTxRefNum(transactionRefNum, siteName);
					modelAndView.addObject("transaction", transaction);
				}
			} else if (paymentChannel.equals(PaymentType.WEB.toString())) {
				List<WebTx> referencedTransactionList
					= this.getServiceStub().getReferencedWebTx(transactionRefNum, siteName);
				modelAndView.addObject("referencedTransaction", referencedTransactionList);
				if (referencedTransactionList == null || referencedTransactionList.size() == 0) {
					WebTx transaction = this.getServiceStub().getWebTxByTxRefNum(transactionRefNum, siteName);
					modelAndView.addObject("transaction", transaction);
				}
			} else if (paymentChannel.equals(PaymentType.PAYASUGO.toString())) {
				List<PayAsUGoTx> referencedTransactionList
				= this.getServiceStub().getReferencedPayAsUGoTransaction(transactionRefNum, siteName);
				modelAndView.addObject("referencedTransaction", referencedTransactionList);
				if (referencedTransactionList == null || referencedTransactionList.size() == 0) {
					PayAsUGoTx transaction = this.getServiceStub().getPayAsUGoTransactionByTxRefNum(transactionRefNum, siteName);
					modelAndView.addObject("transaction", transaction);
				}
			} else if (paymentChannel.equals(PaymentType.RECURRING.toString())) {
				RecurTx referencedTransaction
					= this.getServiceStub().getReferencedRecurringTransactionByTxRefNum(transactionRefNum, siteName);
				modelAndView.addObject("referencedTransaction", referencedTransaction);
				if (referencedTransaction == null) {
					List<RecurTx> recurTransactionList = this.getServiceStub().
							getRecurringTransactionByTxRefNum(transactionRefNum, siteName);
					for (RecurTx recurTransaction : recurTransactionList) {
						if (recurTransaction.getTransactionType().toString().equalsIgnoreCase("CHARGE")) {
							modelAndView.addObject("transaction", recurTransaction);
						}
					}
				}
			}
			modelAndView.addObject("transactionRefNum", transactionRefNum);
			modelAndView.addObject("paymentChannel", paymentChannel);
		}
		return modelAndView;
	}

	@RequestMapping(value="/processrefundtransaction.admin")
	public ModelAndView processRefund(HttpServletRequest request,
								@RequestParam(required = false) String transactionRefNum,
								@RequestParam(required = false) String paymentChannel,
								@RequestParam(required = false) String comments) {

		request.getSession().removeAttribute("paymentChannel");
		request.getSession().removeAttribute("txRefundResponse");
		request.getSession().removeAttribute("referenceTransaction");
		request.getSession().removeAttribute("transactionRefNum");

		request.getSession().setAttribute("paymentChannel", paymentChannel);
		request.getSession().setAttribute("transactionRefNum", transactionRefNum);

		String siteName = null;
		ModelAndView modelAndView = this.getModelAndView(request, REDIRECT_REFUND_STATUS, TOP_REPORTS, SUB_REPORTS_TRANS_LOOKUP);

		if (transactionRefNum == null || transactionRefNum == "") {
			modelAndView.addObject("generalException", "Invalid Transaction Reference Number");
			modelAndView.setViewName(GENERAL_ERROR);
			return modelAndView;
		}

		if (!this.isFeatureEnabledForUser(request, "RefundTransaction")) {
			modelAndView.setViewName(REDIRECT_ACCESS_DENIED);
			return modelAndView;
		}

		if (!this.isInternalUser(request)) {
			List<Site> sites = this.getAssignedSites(request);
			for (Site site : sites) {
				siteName = site.getName();
			}
		}

		if ((transactionRefNum != null && transactionRefNum != "") &&  (paymentChannel != null && paymentChannel != "")) {
			if (paymentChannel.equals(PaymentType.OTC.toString())) {
				OTCTx transaction = this.getServiceStub().getOTCTransactionByTxRefNum(transactionRefNum, siteName);
				if (transaction != null && transaction.getTxRefNum() !=null && !transaction.getTxRefNum().equals("")) {
					if (transaction.getTransactionType() == TransactionType.CHARGE) {
						OTCTx referencedTransaction
							= this.getServiceStub().getReferencedOTCTransaction(transaction.getTxRefNum(), siteName);
						if (referencedTransaction == null) {
							PayPalDTO responseDto = new PayPalDTO();
							responseDto = this.getServiceStub().doReferenceCredit(transactionRefNum, comments,
									request.getUserPrincipal().getName(), request.getRemoteHost(), siteName, PaymentType.OTC);
							responseDto.setReferencedtxRefNum(transactionRefNum);
							request.getSession().setAttribute("txRefundResponse", responseDto);
						} else {
							request.getSession().setAttribute("referenceTransaction", referencedTransaction);
						}
					} else if (transaction.getTransactionType() == TransactionType.REFUND) {
						request.getSession().setAttribute("referenceTransaction", transaction);
					}
				} else {
					modelAndView.addObject("generalException", "Invalid Transaction Reference Number");
					modelAndView.setViewName(GENERAL_ERROR);
				}
			} else if (paymentChannel.equals(PaymentType.WEB.toString())) {
				WebTx transaction = this.getServiceStub().getWebTxByTxRefNum(transactionRefNum, siteName);
				if (transaction != null && transaction.getTxRefNum() !=null && !transaction.getTxRefNum().equals("")) {
					if (transaction.getTransactionType() == TransactionType.CHARGE) {
						List<WebTx> refWebTxList
							= this.getServiceStub().getReferencedWebTx(transactionRefNum, siteName);
						if (refWebTxList == null || refWebTxList.size() == 0) {
							PayPalDTO responseDto = new PayPalDTO();
							responseDto = this.getServiceStub().doReferenceCredit(transactionRefNum, comments,
									request.getUserPrincipal().getName(), request.getRemoteHost(), siteName, PaymentType.WEB);
							responseDto.setReferencedtxRefNum(transactionRefNum);
							request.getSession().setAttribute("txRefundResponse", responseDto);
						} else {
							request.getSession().setAttribute("referenceTransaction", refWebTxList.get(0));
						}
					} else if (transaction.getTransactionType() == TransactionType.REFUND) {
						request.getSession().setAttribute("referenceTransaction", transaction);
					}
				} else {
					modelAndView.addObject("generalException", "Invalid Transaction Reference Number");
					modelAndView.setViewName(GENERAL_ERROR);
				}
			} else if (paymentChannel.equals(PaymentType.PAYASUGO.toString())) {
				PayAsUGoTx transaction = this.getServiceStub().getPayAsUGoTransactionByTxRefNum(transactionRefNum, siteName);
				if (transaction != null && transaction.getTxRefNum() !=null && !transaction.getTxRefNum().equals("")) {
					if (transaction.getTransactionType() == TransactionType.CHARGE) {
						List<PayAsUGoTx> refPayAsUGoTransactionList
							= this.getServiceStub().getReferencedPayAsUGoTransaction(transactionRefNum, siteName);
						if (refPayAsUGoTransactionList == null || refPayAsUGoTransactionList.size() == 0) {
							PayPalDTO responseDto = new PayPalDTO();
							responseDto = this.getServiceStub().doReferenceCredit(transactionRefNum, comments,
									request.getUserPrincipal().getName(), request.getRemoteHost(), siteName, PaymentType.PAYASUGO);
							responseDto.setReferencedtxRefNum(transactionRefNum);
							request.getSession().setAttribute("txRefundResponse", responseDto);
						} else {
							request.getSession().setAttribute("referenceTransaction", refPayAsUGoTransactionList.get(0));
						}
					} else if (transaction.getTransactionType() == TransactionType.REFUND) {
						request.getSession().setAttribute("referenceTransaction", transaction);
					}
				} else {
					modelAndView.addObject("generalException", "Invalid Transaction Reference Number");
					modelAndView.setViewName(GENERAL_ERROR);
				}
			}
			else if (paymentChannel.equals(PaymentType.RECURRING.toString())) {
				List<RecurTx> recurTransactionList = this.getServiceStub().
						getRecurringTransactionByTxRefNum(transactionRefNum,
						siteName);
				RecurTx transaction = null;

				for (RecurTx recurTransaction : recurTransactionList) {
					if (recurTransaction.getTransactionType().toString().equalsIgnoreCase("CHARGE") &&
							!recurTransaction.isPreviousAccess()) {
						transaction = recurTransaction;
						request.getSession().setAttribute("transaction", recurTransaction);
					}
				}

				if (transaction != null && transaction.getTxRefNum() !=null && !transaction.getTxRefNum().equals("")) {
					if (transaction.getTransactionType() == TransactionType.CHARGE) {
						RecurTx referencedTransaction
							= this.getServiceStub().getReferencedRecurringTransactionByTxRefNum(transaction.getTxRefNum(),
									siteName);
						if (referencedTransaction == null) {
							PayPalDTO responseDto = new PayPalDTO();
							responseDto = this.getServiceStub().doReferenceCredit(transactionRefNum, comments,
										request.getUserPrincipal().getName(), request.getRemoteHost(), siteName,
										PaymentType.RECURRING);
							responseDto.setReferencedtxRefNum(transactionRefNum);
							request.getSession().setAttribute("txRefundResponse", responseDto);
						} else {
							request.getSession().setAttribute("referenceTransaction", referencedTransaction);
						}
					} else if (transaction.getTransactionType() == TransactionType.REFUND) {
						request.getSession().setAttribute("referenceTransaction", transaction);
					}
				} else {
					modelAndView.addObject("generalException", "Invalid Transaction Reference Number");
					modelAndView.setViewName(GENERAL_ERROR);
				}

			}
		}
		return modelAndView;
	}

	@RequestMapping(value="/refundStatusconfirmation.admin")
	public ModelAndView refundStatusconfirmation(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, REFUND_STATUS, TOP_REPORTS, SUB_REPORTS_TRANS_LOOKUP);

		RecurTx recurTransaction =  null;
		List<PayAsUGoTx> payAsUGoTransactionList = null;
		List<WebTx> webTransactionList = null;

		OTCTx otcTransaction = null;

		String transactionRefNum = request.getSession().getAttribute("transactionRefNum").toString();
		String paymentChannel = (String) request.getSession().getAttribute("paymentChannel");
		PayPalDTO txRefundResponse = (PayPalDTO) request.getSession().getAttribute("txRefundResponse");

		modelAndView.addObject("paymentChannel", paymentChannel);
		modelAndView.addObject("transactionRefNum", transactionRefNum);

		if (paymentChannel.equals(PaymentType.PAYASUGO.toString())) {
			payAsUGoTransactionList = (List<PayAsUGoTx>) request.getSession().getAttribute("referenceTransaction");
			if (payAsUGoTransactionList != null) {
				modelAndView.addObject("referenceTransaction", payAsUGoTransactionList);
			} else {
				modelAndView.addObject("txRefundResponse", txRefundResponse);
			}
		} else if (paymentChannel.equals(PaymentType.WEB.toString())) {
			webTransactionList = (List<WebTx>) request.getSession().getAttribute("referenceTransaction");
			if (payAsUGoTransactionList != null) {
				modelAndView.addObject("referenceTransaction", webTransactionList);
			} else {
				modelAndView.addObject("txRefundResponse", txRefundResponse);
			}
		} else if (paymentChannel.equals(PaymentType.OTC.toString())){
			otcTransaction = (OTCTx) request.getSession().getAttribute("referenceTransaction");
			if (otcTransaction != null) {
				modelAndView.addObject("referenceTransaction", otcTransaction);
			} else {
				modelAndView.addObject("txRefundResponse", txRefundResponse);
			}
		} else if (paymentChannel.equals(PaymentType.RECURRING.toString())){
			recurTransaction = (RecurTx) request.getSession().getAttribute("referenceTransaction");
			if (recurTransaction != null) {
				modelAndView.addObject("referenceTransaction", recurTransaction);
			} else {
				modelAndView.addObject("txRefundResponse", txRefundResponse);
			}
		}
		return modelAndView;
	}


	@RequestMapping(value="/confirmpartialrefund.admin")
	public ModelAndView getReferencedItem(HttpServletRequest request,
				@RequestParam(required = false) Long itemId,
				@RequestParam(required = false) String paymentChannel) {
		ModelAndView modelAndView = this.getModelAndView(request, CONFIRM_PARTIAL_REFUND, TOP_REPORTS, SUB_REPORTS_TRANS_LOOKUP);

		if (!this.isFeatureEnabledForUser(request, "RefundTransaction")) {
			modelAndView.setViewName(REDIRECT_ACCESS_DENIED);
			return modelAndView;
		}

		String siteName = null;
		if (!this.isInternalUser(request)) {
			List<Site> sites = this.getAssignedSites(request);
			for (Site site : sites) {
				siteName = site.getName();
			}
		}
		if (paymentChannel.equals(PaymentType.PAYASUGO.toString())) {
			PayAsUGoTx referencedTransaction
				= this.getServiceStub().getReferencedPayAsUGoTransactionItemByItemId(itemId, siteName);
			modelAndView.addObject("referencedTransaction", referencedTransaction);
			if (referencedTransaction == null) {
				PayAsUGoTx transaction = this.getServiceStub().getPayAsUGoTransactionItemByItemId(itemId, siteName);
				modelAndView.addObject("transaction", transaction);
			}
			modelAndView.addObject("transactionType", PAY_AS_U_GO);
		} else if (paymentChannel.equals(PaymentType.WEB.toString())) {
			WebTx referencedTransaction
				= this.getServiceStub().getReferencedWebTxItemByItemId(itemId, siteName);
			modelAndView.addObject("referencedTransaction", referencedTransaction);
			if (referencedTransaction == null) {
				WebTx transaction = this.getServiceStub().getWebTxItemByItemId(itemId, siteName);
				modelAndView.addObject("transaction", transaction);
			}
			modelAndView.addObject("transactionType", WEB);
		}

		modelAndView.addObject("itemId", itemId);
		modelAndView.addObject("paymentChannel", paymentChannel);
		return modelAndView;
	}


	@RequestMapping(value="/processpartialrefund.admin")
	public ModelAndView processPartialRefund(HttpServletRequest request,
			@RequestParam(required = false) Long itemId,
			@RequestParam(required = false) String comments,
			@RequestParam(required = false) String paymentChannel) {

		request.getSession().removeAttribute("partialRefundTransaction");
		request.getSession().removeAttribute("responseDto");
		request.getSession().removeAttribute("ERROR_MSG");

		String siteName = null;
		if (!this.isInternalUser(request)) {
			List<Site> sites = this.getAssignedSites(request);
			for (Site site : sites) {
				siteName = site.getName();
			}
		}
		ModelAndView modelAndView = this.getModelAndView(request, REDIRECT_PARTIAL_REFUND_STATUS, TOP_REPORTS, SUB_REPORTS_TRANS_LOOKUP );
		if (!this.isFeatureEnabledForUser(request, "RefundTransaction")) {
			modelAndView.setViewName(REDIRECT_ACCESS_DENIED);
			return modelAndView;
		}
		if (paymentChannel.equals(PaymentType.PAYASUGO.toString())) {
			PayAsUGoTx referencedTransaction = this.getServiceStub().getReferencedPayAsUGoTransactionItemByItemId(itemId, siteName);
			if (referencedTransaction == null) {
				PayPalDTO responseDto = new PayPalDTO();
				try {
					responseDto = this.getServiceStub().doPartialReferenceCredit(itemId, siteName, comments,
							request.getUserPrincipal().getName(), request.getRemoteHost(), PaymentType.PAYASUGO);
					if (responseDto.getTxRefNum() == null || responseDto.getTxRefNum() == "") {
						request.getSession().setAttribute("ERROR_MSG", this.getMessage("paypal.errorcode.generalsystemerror"));
					} else {
						request.getSession().setAttribute("responseDto", responseDto);
					}
				} catch (PaymentGatewayUserException payPalUserException) {
					request.getSession().setAttribute("ERROR_MSG", this.getMessage("paypal.errorcode." + payPalUserException.getErrorCode()));
				} catch (PaymentGatewaySystemException payPalSystemException) {
					request.getSession().setAttribute("ERROR_MSG", this.getMessage("paypal.errorcode.generalsystemerror"));
				} catch (SDLBusinessException sdlBusinessException) {
					request.getSession().setAttribute("ERROR_MSG", this.getMessage("sdl.generic.sdlBusinessException"));
				}
			}
		} else if (paymentChannel.equals(PaymentType.WEB.toString())) {
			WebTx referencedTransaction = this.getServiceStub().getReferencedWebTxItemByItemId(itemId, siteName);
			if (referencedTransaction == null) {
				PayPalDTO responseDto = new PayPalDTO();
				try {
					responseDto = this.getServiceStub().doPartialReferenceCredit(itemId, siteName, comments,
							request.getUserPrincipal().getName(), request.getRemoteHost(), PaymentType.WEB);
					if (responseDto.getTxRefNum() == null || responseDto.getTxRefNum() == "") {
						request.getSession().setAttribute("ERROR_MSG", this.getMessage("paypal.errorcode.generalsystemerror"));
					} else {
						request.getSession().setAttribute("responseDto", responseDto);
					}
				} catch (PaymentGatewayUserException payPalUserException) {
					request.getSession().setAttribute("ERROR_MSG", this.getMessage("paypal.errorcode." + payPalUserException.getErrorCode()));
				} catch (PaymentGatewaySystemException payPalSystemException) {
					request.getSession().setAttribute("ERROR_MSG", this.getMessage("paypal.errorcode.generalsystemerror"));
				} catch (SDLBusinessException sdlBusinessException) {
					request.getSession().setAttribute("ERROR_MSG", this.getMessage("sdl.generic.sdlBusinessException"));
				}
			}
		}
		modelAndView.addObject("paymentChannel", paymentChannel);
		return modelAndView;
	}

	@RequestMapping(value="/processpartialrefundconfirmation.admin")
	public ModelAndView processPartialRefund(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, PARTIAL_REFUND_STATUS, TOP_REPORTS, SUB_REPORTS_TRANS_LOOKUP);

		String errorMessage  = (String) request.getSession().getAttribute("ERROR_MSG");
		PayPalDTO PayPalDTO = (PayPalDTO) request.getSession().getAttribute("responseDto");

		if(errorMessage != null) {
			modelAndView.addObject("ERROR_MSG", errorMessage);
		}
		if(PayPalDTO != null) {
			modelAndView.addObject("responseDto", PayPalDTO);
		}
		return modelAndView;
	}
	
	private List<String> getTxRowForExcel(Tx tx){
		List<String> row = new ArrayList<String>();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");  
		
		String txDate = tx.getTransactionDate() != null ? df.format(tx.getTransactionDate()) : "";
		row.add(txDate);
		row.add(tx.getTxRefNum());
		row.add(tx.getOrigTxRefNum());
		row.add(tx.getCardNumber());
		if(!StringUtils.isBlank(tx.getAccountName()) && !tx.getAccountName().equals("N/A")){
			row.add(SystemUtil.convertCamelCase(tx.getAccountName()));	
		} else {
			row.add(tx.getAccountName());
		}
		row.add(String.format( "%.2f", tx.getTotalTxAmount()));
		row.add(SystemUtil.convertCamelCase(tx.getTransactionType().toString()));	
		return row;
	}	
	
	private List<PDFCell> getTxRowForPDF(Tx tx){
		List<PDFCell> cells = new ArrayList<PDFCell>();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");  
		
		String txDate = tx.getTransactionDate() != null ? df.format(tx.getTransactionDate()) : "";

		cells.add(new PDFCell(txDate, false));
		cells.add(new PDFCell(tx.getTxRefNum(), false));
		cells.add(new PDFCell(tx.getOrigTxRefNum(), false));
		cells.add(new PDFCell(tx.getCardNumber(), false));
		if(!StringUtils.isBlank(tx.getAccountName()) && !tx.getAccountName().equals("N/A")){
			cells.add(new PDFCell(SystemUtil.convertCamelCase(tx.getAccountName()), true));	
		} else {
			cells.add(new PDFCell(tx.getAccountName(), true));
		}
		cells.add(new PDFCell("$" + String.format( "%.2f", tx.getTotalTxAmount() ), false));
		cells.add(new PDFCell(SystemUtil.convertCamelCase(tx.getTransactionType().toString()), false));
		return cells;
	}


}
