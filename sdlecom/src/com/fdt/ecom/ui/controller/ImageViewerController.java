package com.fdt.ecom.ui.controller;

import static com.fdt.ecom.ui.EcomViewConstants.ECOM_IMAGE_UNAVAILABLE;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_REDIRECT_REVIEW_SHOPPING_CART;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_REVIEW_SHOPPING_CART;
import static com.fdt.security.ui.FirmLevelUserConstants.ERROR;
import static com.fdt.security.ui.FirmLevelUserConstants.SUCCESS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Validator;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.entity.ErrorCode;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.common.util.CacheableServices;
import com.fdt.common.util.JsonResponse;
import com.fdt.common.util.TIFFToPDFConverter;
import com.fdt.common.util.adapter.DocumentManagementSystemAdapter;
import com.fdt.common.util.adapter.SDLDMSDocument;
import com.fdt.ecert.Certification;
import com.fdt.ecom.entity.Code;
import com.fdt.ecom.entity.Location;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.ecom.ui.validator.CreditCardFormValidator;
import com.fdt.payasugotx.dto.PayAsUSubDTO;
import com.fdt.payasugotx.entity.PayAsUGoTxItem;
import com.fdt.sdl.styledesigner.util.PageStyleUtil;
import com.fdt.security.entity.User;
import com.itextpdf.text.DocumentException;

@Controller
public class ImageViewerController extends AbstractBaseController {

	@Autowired(required = true)
	private Validator validator;

	@Autowired
	@Qualifier(value="creditCardFormValidator")
	private CreditCardFormValidator creditCardValidator;

	@Autowired(required = true)
	CacheableServices cachedService;


	public static String SHOPPING_CART = "SHOPPING_CART";

	public static String PRICE_CALC_SHOPPING_CART = "PRICE_CALC_SHOPPING_CART";

	@RequestMapping(value="/clearshoppingcart.admin")
	public ModelAndView clearShoppingCart(HttpServletRequest request) {
		ModelAndView modelAndView = getModelAndView(request, ECOM_REDIRECT_REVIEW_SHOPPING_CART);
		request.getSession().removeAttribute(SHOPPING_CART + request.getRemoteUser());
		modelAndView.addObject(SHOPPING_CART, null);
		return modelAndView;
	}

	@RequestMapping(value="/downloadDocument.admin")
	public String downLoadDocument(HttpServletRequest request,
							   	   HttpServletResponse response,
							   	   @RequestParam String imagelocation,
							   	   @RequestParam String waterMarkStr,
							   	   @RequestParam int waterMarkRedColorCode,
							   	   @RequestParam int waterMarkGreenColorCode,
							   	   @RequestParam int waterMarkBlueColorCode,
							   	   @RequestParam int fontSize,
							   	   @RequestParam int pageCount,
							   	   @RequestParam boolean isPrintingAllowed,
							   	   @RequestParam boolean isFillWatermark,
							   	   @RequestParam float opacity,
							   	   @RequestParam String productkey,
							   	   @RequestParam String uniqueidentifier,
							   	   @RequestParam boolean keepOriginalPageSize,
							   	   @RequestParam(required=false) String locationName,
							   	   @RequestParam (required=false) String caseNumber,
							   	   @RequestParam (required=false) String caseTitle,
							   	   @RequestParam (required=false) String caseEvent,
							   	   @RequestParam(required=false) String docRetrievalMode,
							   	   @RequestParam (required=false) String productName
							   	   ) { 
		try {
			SDLDMSDocument sDLDMSDocument = null;
			PayAsUGoTxItem item = this.getService().getPayAsUGoTxIdForPurchasedDoc(request.getRemoteUser(), productkey, uniqueidentifier);
			if (item != null) {
				ByteArrayOutputStream byteArrayOutputStream =  new ByteArrayOutputStream();
				boolean isFileConverted = false;
				
				if(!StringUtils.isBlank(docRetrievalMode) && docRetrievalMode.equals("DMS")) {
					DocumentManagementSystemAdapter documentManagementSystemAdapter = new DocumentManagementSystemAdapter();
					sDLDMSDocument = documentManagementSystemAdapter.getDocumentByDMSID(productName);
				}
				
				if(sDLDMSDocument==null) {
					if(item.isCertified()){
						this.createCertifiedCopyFromTiffFile(imagelocation, locationName, uniqueidentifier, item.getCertifiedDocumentNumber(), caseNumber, caseTitle, caseEvent, byteArrayOutputStream);
						isFileConverted = true;
					} else {
						isFileConverted = TIFFToPDFConverter.convert(imagelocation + ".burned", waterMarkStr,
							waterMarkRedColorCode, waterMarkGreenColorCode, waterMarkBlueColorCode, fontSize, pageCount,
							isPrintingAllowed, byteArrayOutputStream, isFillWatermark, opacity, keepOriginalPageSize);
					}
					if (!isFileConverted) {
						isFileConverted = TIFFToPDFConverter.convert(imagelocation, waterMarkStr,
							waterMarkRedColorCode, waterMarkGreenColorCode, waterMarkBlueColorCode, fontSize, pageCount,
							isPrintingAllowed, byteArrayOutputStream, isFillWatermark, opacity, keepOriginalPageSize);
					}
					if (isFileConverted) {
						response.reset();
						response.resetBuffer();
						response.setContentType("application/x-download");
						response.setHeader("Content-Disposition",  "attachment; filename=" + productkey + ".pdf" );
						response.setHeader("Expires", " 0");
						response.setHeader("Cache-Control", " must-revalidate, post-check=0, pre-check=0" );
						response.setHeader("Pragma" , "public");
						byteArrayOutputStream.writeTo(response.getOutputStream());
						response.getOutputStream().flush();
					}
				} else {
					byte[] byteArray = sDLDMSDocument.getFile();
					String fileExtension = sDLDMSDocument.getFileExtension();
					
					if(fileExtension!=null && byteArray!=null) {	
						
						if(fileExtension.contains("pdf")) {
							if(item.isCertified()){
								this.createCertifiedCopyFromPdfByteArray(byteArray, locationName, uniqueidentifier, item.getCertifiedDocumentNumber(), caseNumber, caseTitle, caseEvent, byteArrayOutputStream);
							} else {
								byteArrayOutputStream = PageStyleUtil.getByteArrayOutputStreamFromByteArray(byteArray);
							}
						} else if(fileExtension.contains("tif")) {							
							if(item.isCertified()){
								this.createCertifiedCopyFromTiffArray(byteArray, locationName, uniqueidentifier, item.getCertifiedDocumentNumber(), caseNumber, caseTitle, caseEvent, byteArrayOutputStream);
							} else {
								TIFFToPDFConverter.convert(byteArray, waterMarkStr,
										waterMarkRedColorCode, waterMarkGreenColorCode, waterMarkBlueColorCode, fontSize, pageCount,
										isPrintingAllowed, byteArrayOutputStream, isFillWatermark, opacity, keepOriginalPageSize);
							}			
							
						} else {
							logger.error("Exeption While Retriving Doc From DocumentManagementSystem DMS ID: {}. File Extension is neither pdf nor tif. ",
									productName);
						}
						
						response.reset();
						response.resetBuffer();
						response.setContentType("application/x-download");
						response.setHeader("Content-Disposition",  "attachment; filename=" + productkey + ".pdf" );
						response.setHeader("Expires", " 0");
						response.setHeader("Cache-Control", " must-revalidate, post-check=0, pre-check=0" );
						response.setHeader("Pragma" , "public");
						byteArrayOutputStream.writeTo(response.getOutputStream());
						response.getOutputStream().flush();
						
					} else {
						logger.error("Exeption While Retriving Doc From DocumentManagementSystem DMS ID: {} Error Code: {} Error Description: {}",
								productName, sDLDMSDocument.getErrorCode(), sDLDMSDocument.getErrorDescription());
					}
					
					
					
				}
			} else {
				return ECOM_IMAGE_UNAVAILABLE;
			}
		} catch (IOException iOException) {
			logger.error("Error Occured in getting the Image" , iOException);
		} catch (DocumentException docException) {
			logger.error("Error Occured in getting the Image" , docException);
		}
		return null;
	}

	@RequestMapping(value="/viewPurchasedDocument.admin")
	public String viewPurchasedDocument(HttpServletRequest request,
							   	   HttpServletResponse response,
							   	   @RequestParam String imagelocation,
							   	   @RequestParam String waterMarkStr,
							   	   @RequestParam int waterMarkRedColorCode,
							   	   @RequestParam int waterMarkGreenColorCode,
							   	   @RequestParam int waterMarkBlueColorCode,
							   	   @RequestParam int fontSize,
							   	   @RequestParam int pageCount,
							   	   @RequestParam boolean isPrintingAllowed,
							   	   @RequestParam boolean isFillWatermark,
							   	   @RequestParam float opacity,
							   	   @RequestParam String productkey,
							   	   @RequestParam String uniqueidentifier,
							   	   @RequestParam boolean keepOriginalPageSize,
							   	   @RequestParam(required=false) String locationName,
							   	   @RequestParam (required=false) String caseNumber,
							   	   @RequestParam (required=false) String caseTitle,
							   	   @RequestParam (required=false) String caseEvent,
							   	   @RequestParam(required=false) String docRetrievalMode,
							   	   @RequestParam (required=false) String productName
							   	   ) {
		try {
			SDLDMSDocument sDLDMSDocument = null;
			PayAsUGoTxItem item = this.getService().getPayAsUGoTxIdForPurchasedDoc(request.getRemoteUser(), productkey, uniqueidentifier);
			if (item != null) {
				ByteArrayOutputStream byteArrayOutputStream =  new ByteArrayOutputStream();
				boolean isFileConverted = false;				
				if(!StringUtils.isBlank(docRetrievalMode) && docRetrievalMode.equals("DMS")) {
					DocumentManagementSystemAdapter documentManagementSystemAdapter = new DocumentManagementSystemAdapter();
					sDLDMSDocument = documentManagementSystemAdapter.getDocumentByDMSID(productName);
				}				
				if(sDLDMSDocument==null) {
					if(item.isCertified()){
						this.createCertifiedCopyFromTiffFile(imagelocation, locationName, uniqueidentifier, item.getCertifiedDocumentNumber(), caseNumber, caseTitle, caseEvent, byteArrayOutputStream);
						isFileConverted = true;
					} else {
						isFileConverted = TIFFToPDFConverter.convert(imagelocation + ".burned", waterMarkStr,
							waterMarkRedColorCode, waterMarkGreenColorCode, waterMarkBlueColorCode, fontSize, pageCount,
							isPrintingAllowed, byteArrayOutputStream, isFillWatermark, opacity, keepOriginalPageSize);
					}
					if (!isFileConverted) {
						isFileConverted = TIFFToPDFConverter.convert(imagelocation, waterMarkStr,
							waterMarkRedColorCode, waterMarkGreenColorCode, waterMarkBlueColorCode, fontSize, pageCount,
							isPrintingAllowed, byteArrayOutputStream, isFillWatermark, opacity, keepOriginalPageSize);
					}
					if (isFileConverted) {
						response.reset();
						response.resetBuffer();
						response.setContentType("application/pdf");
						response.setHeader("Content-Disposition",  "inline; filename=" + productkey + ".pdf" );
						response.setHeader("Expires", " 0");
						response.setHeader("Cache-Control", " must-revalidate, post-check=0, pre-check=0" );
						response.setHeader("Pragma" , "public");
						byteArrayOutputStream.writeTo(response.getOutputStream());
						response.getOutputStream().flush();
					}
				} else {
					byte[] byteArray = sDLDMSDocument.getFile();
					String fileExtension = sDLDMSDocument.getFileExtension();					
					if(fileExtension!=null && byteArray!=null) {						
						if(fileExtension.contains("pdf")) {
							if(item.isCertified()){
								this.createCertifiedCopyFromPdfByteArray(byteArray, locationName, uniqueidentifier, item.getCertifiedDocumentNumber(), caseNumber, caseTitle, caseEvent, byteArrayOutputStream);
							} else {
								byteArrayOutputStream = PageStyleUtil.getByteArrayOutputStreamFromByteArray(byteArray);
							}
						} else if(fileExtension.contains("tif")) {							
							if(item.isCertified()){
								this.createCertifiedCopyFromTiffArray(byteArray, locationName, uniqueidentifier, item.getCertifiedDocumentNumber(), caseNumber, caseTitle, caseEvent, byteArrayOutputStream);
							} else {
								TIFFToPDFConverter.convert(byteArray, waterMarkStr,
										waterMarkRedColorCode, waterMarkGreenColorCode, waterMarkBlueColorCode, fontSize, pageCount,
										isPrintingAllowed, byteArrayOutputStream, isFillWatermark, opacity, keepOriginalPageSize);
							}			
							
						} else {
							logger.error("Exeption While Retriving Doc From DocumentManagementSystem DMS ID: {}. File Extension is neither pdf nor tif. ",
									productName);
						}
						response.reset();
						response.resetBuffer();
						response.setContentType("application/pdf");
						response.setHeader("Content-Disposition",  "inline; filename=" + productkey + ".pdf" );
						response.setHeader("Expires", " 0");
						response.setHeader("Cache-Control", " must-revalidate, post-check=0, pre-check=0" );
						response.setHeader("Pragma" , "public");
						byteArrayOutputStream.writeTo(response.getOutputStream());
						response.getOutputStream().flush();						
					} else {
						logger.error("Exeption While Retriving Doc From DocumentManagementSystem DMS ID: {} Error Code: {} Error Description: {}",
								productName, sDLDMSDocument.getErrorCode(), sDLDMSDocument.getErrorDescription());
					}
				}
			} else {
				return ECOM_IMAGE_UNAVAILABLE;
			}
		} catch (IOException iOException) {
			logger.error("Error Occured in getting the Image" , iOException);
		} catch (DocumentException docException) {
			logger.error("Error Occured in getting the Image" , docException);
		}
		return null;
	}

	private void createCertifiedCopyFromTiffFile(String imageLocation, String locationName, String accessName,
			String certifiedDocumentNumber, String caseNumber,
			String caseTitle, String caseEvent,	ByteArrayOutputStream byteArrayOutputStream)
					throws DocumentException, IOException {
		Location location = cachedService.findLocationByNameAndAccessName(locationName, accessName);
		String clerkName = location.getClerkName();
		String designation = location.getDesignation();
		String state = location.getStateDescription();
		byte[] sealOfAuthenticity = location.getSealOfAuthenticity();
		byte[] signature = location.getSignature();

		ByteArrayOutputStream convertedTiffImage = new ByteArrayOutputStream();
		ByteArrayOutputStream coverPage = new ByteArrayOutputStream();
		ByteArrayOutputStream internalPage = new ByteArrayOutputStream();
		ByteArrayOutputStream certifiedDocumentInternal = new ByteArrayOutputStream();

		Date date = new Date();
		String footerText = super.watermarkText.concat(" - ").concat(" ").concat(certifiedDocumentNumber).concat(" -").
			 concat(" ").concat(PageStyleUtil.format(date.toString(), super.inputFormat, super.footerDateFormat));
		String signatureFooter = clerkName.toUpperCase().concat("\n").concat(designation).concat("\n")
				.concat("County Of ").concat(locationName).concat("\n").concat(state).concat(".");
		String noteOfAuthenticity = "I, ".concat(clerkName).concat(", ").concat(designation).concat(" of the State of Arizona")
				.concat(", in and for the County of ").concat(locationName).concat(", " +
				location.getNoteOfAuthenticity()  +	"  \n\nAttest: ".concat(PageStyleUtil.format(date.toString(),
						super.inputFormat, super.attestedDateFormat))
						.concat("\nCertified Document Number:  " + certifiedDocumentNumber));

		TIFFToPDFConverter.convert(imageLocation, null, 0,0,0,0,0, true, convertedTiffImage, false, 0.5f, false);
		String roaInformation = "Certification Page For: \nCase No. " + caseNumber + " - " + caseTitle + " - " + caseEvent;
		Certification.createCoverPage(coverPage, roaInformation, sealOfAuthenticity, noteOfAuthenticity,
			 signature, signatureFooter, footerText, super.urlVerification);

		Certification.createInternalPage(internalPage, footerText);
		Certification.stampTheDocument(certifiedDocumentInternal, convertedTiffImage.toByteArray(),
				internalPage.toByteArray());

		Certification.concatenate(coverPage.toByteArray(), certifiedDocumentInternal.toByteArray(), byteArrayOutputStream);
		convertedTiffImage.close();
		coverPage.close();
		internalPage.close();
		certifiedDocumentInternal.close();
	}
	
	private void createCertifiedCopyFromTiffArray(byte[] byteArray, String locationName, String accessName,
			String certifiedDocumentNumber, String caseNumber,
			String caseTitle, String caseEvent,	ByteArrayOutputStream byteArrayOutputStream)
					throws DocumentException, IOException {
		Location location = cachedService.findLocationByNameAndAccessName(locationName, accessName);
		String clerkName = location.getClerkName();
		String designation = location.getDesignation();
		String state = location.getStateDescription();
		byte[] sealOfAuthenticity = location.getSealOfAuthenticity();
		byte[] signature = location.getSignature();

		ByteArrayOutputStream convertedTiffImage = new ByteArrayOutputStream();
		ByteArrayOutputStream coverPage = new ByteArrayOutputStream();
		ByteArrayOutputStream internalPage = new ByteArrayOutputStream();
		ByteArrayOutputStream certifiedDocumentInternal = new ByteArrayOutputStream();

		Date date = new Date();
		String footerText = super.watermarkText.concat(" - ").concat(" ").concat(certifiedDocumentNumber).concat(" -").
			 concat(" ").concat(PageStyleUtil.format(date.toString(), super.inputFormat, super.footerDateFormat));
		String signatureFooter = clerkName.toUpperCase().concat("\n").concat(designation).concat("\n")
				.concat("County Of ").concat(locationName).concat("\n").concat(state).concat(".");
		String noteOfAuthenticity = "I, ".concat(clerkName).concat(", ").concat(designation).concat(" of the State of Arizona")
				.concat(", in and for the County of ").concat(locationName).concat(", " +
				location.getNoteOfAuthenticity()  +	"  \n\nAttest: ".concat(PageStyleUtil.format(date.toString(),
						super.inputFormat, super.attestedDateFormat))
						.concat("\nCertified Document Number:  " + certifiedDocumentNumber));

		TIFFToPDFConverter.convert(byteArray, null, 0,0,0,0,0, true, convertedTiffImage, false, 0.5f, false);
		String roaInformation = "Certification Page For: \nCase No. " + caseNumber + " - " + caseTitle + " - " + caseEvent;
		Certification.createCoverPage(coverPage, roaInformation, sealOfAuthenticity, noteOfAuthenticity,
			 signature, signatureFooter, footerText, super.urlVerification);

		Certification.createInternalPage(internalPage, footerText);
		Certification.stampTheDocument(certifiedDocumentInternal, convertedTiffImage.toByteArray(),
				internalPage.toByteArray());

		Certification.concatenate(coverPage.toByteArray(), certifiedDocumentInternal.toByteArray(), byteArrayOutputStream);
		convertedTiffImage.close();
		coverPage.close();
		internalPage.close();
		certifiedDocumentInternal.close();
	}
	
	private void createCertifiedCopyFromPdfByteArray(byte[] byteArray, String locationName, String accessName,
			String certifiedDocumentNumber, String caseNumber,
			String caseTitle, String caseEvent,	ByteArrayOutputStream byteArrayOutputStream)
					throws DocumentException, IOException {
		Location location = cachedService.findLocationByNameAndAccessName(locationName, accessName);
		String clerkName = location.getClerkName();
		String designation = location.getDesignation();
		String state = location.getStateDescription();
		byte[] sealOfAuthenticity = location.getSealOfAuthenticity();
		byte[] signature = location.getSignature();

		
		ByteArrayOutputStream coverPage = new ByteArrayOutputStream();
		ByteArrayOutputStream internalPage = new ByteArrayOutputStream();
		ByteArrayOutputStream certifiedDocumentInternal = new ByteArrayOutputStream();

		Date date = new Date();
		String footerText = super.watermarkText.concat(" - ").concat(" ").concat(certifiedDocumentNumber).concat(" -").
			 concat(" ").concat(PageStyleUtil.format(date.toString(), super.inputFormat, super.footerDateFormat));
		String signatureFooter = clerkName.toUpperCase().concat("\n").concat(designation).concat("\n").concat("State of Arizona").concat("\n")
				.concat("County Of ").concat(locationName);
		
		String noteOfAuthenticity = "I, ".concat(clerkName).concat(", ").concat(designation).concat(" of the State of Arizona")
				.concat(", in and for the County of ").concat(locationName).concat(", " +
				location.getNoteOfAuthenticity()  +	"  \n\nAttest: ".concat(PageStyleUtil.format(date.toString(),
						super.inputFormat, super.attestedDateFormat))
						.concat("\nCertified Document Number:  " + certifiedDocumentNumber));

		//TIFFToPDFConverter.convert(imageLocation, null, 0,0,0,0,0, true, convertedTiffImage, false, 0.5f, false);
		String roaInformation = "Certification Page For: \nCase No. " + caseNumber + " - " + caseTitle + " - " + caseEvent;
		Certification.createCoverPage(coverPage, roaInformation, sealOfAuthenticity, noteOfAuthenticity,
			 signature, signatureFooter, footerText, super.urlVerification);

		Certification.createInternalPage(internalPage, footerText);
		Certification.stampTheDocument(certifiedDocumentInternal, byteArray,
				internalPage.toByteArray());

		Certification.concatenate(coverPage.toByteArray(), certifiedDocumentInternal.toByteArray(), byteArrayOutputStream);
		coverPage.close();
		internalPage.close();
		certifiedDocumentInternal.close();
	}


	@RequestMapping(value="/removeitem.admin")
	public ModelAndView removeItemFromCart(HttpServletRequest request, @RequestParam String productkey) {
		ModelAndView modelAndView = getModelAndView(request, ECOM_REDIRECT_REVIEW_SHOPPING_CART);
		List<ShoppingCartItem> shoppingCart = null;
		Map<String, ShoppingCartItem> shoppingCartMap = null;
		if (request.getSession().getAttribute(SHOPPING_CART + request.getRemoteUser()) != null) {
			shoppingCartMap = (HashMap<String, ShoppingCartItem>)request.getSession()
					.getAttribute(SHOPPING_CART + request.getRemoteUser());
			ShoppingCartItem shoppintCartItem = shoppingCartMap.get(productkey);
			if (shoppintCartItem != null) {
				this.getService().deleteShoppingCartItem(shoppintCartItem);
			}
			shoppingCartMap.remove(productkey);
			if (shoppingCartMap.size() == 0)  {
				request.getSession().removeAttribute(SHOPPING_CART + request.getRemoteUser());
			}
			if (shoppingCartMap != null) {
				shoppingCart = new LinkedList<ShoppingCartItem>(shoppingCartMap.values());
			}
		}
		request.getSession().setAttribute(SHOPPING_CART + request.getRemoteUser(), shoppingCartMap);
		modelAndView.addObject(SHOPPING_CART, shoppingCart);
		return modelAndView;
	}

	@RequestMapping(value="/viewImage.admin", produces="application/json")
	@ResponseBody
	public List<ErrorCode> viewImage(HttpServletRequest request,
					   	   HttpServletResponse response,
					   	   @RequestParam String imagelocation,
					   	   @RequestParam String waterMarkStr,
					   	   @RequestParam int waterMarkRedColorCode,
					   	   @RequestParam int waterMarkGreenColorCode,
					   	   @RequestParam int waterMarkBlueColorCode,
					   	   @RequestParam String productkey,
					   	   @RequestParam String producttype,
					   	   @RequestParam (required=false) String productName,
					   	   @RequestParam String accessname,
					   	   @RequestParam int numberofpages,
					   	   @RequestParam String uniqueidentifier,
					   	   @RequestParam String application,
					   	   @RequestParam(required=false) String barNumber,
					   	   @RequestParam(required=false) String locationName,
					   	   @RequestParam(required=false) String state,
					   	   @RequestParam(required=false) String docRetrievalMode,
					   	   @RequestParam(required=false, defaultValue="false") String isCertifiedString,
					   	   HttpSession httpSession,
					   	   @RequestParam(defaultValue="false") boolean isImageAvailable) {
		boolean isCertified = Boolean.parseBoolean(isCertifiedString);
		
		List<ErrorCode> errors = new LinkedList<ErrorCode>();
		
		
		if(!StringUtils.isBlank(docRetrievalMode) && docRetrievalMode.equals("DMS")) {
			SDLDMSDocument sDLDMSDocument = null;
			DocumentManagementSystemAdapter documentManagementSystemAdapter = new DocumentManagementSystemAdapter();
			sDLDMSDocument = documentManagementSystemAdapter.getDocumentByDMSID(productName);			
			if(sDLDMSDocument.getErrorCode()!=null) {
				response.reset();
				response.resetBuffer();
				ErrorCode error = new ErrorCode();
				error.setCode("ERROR");
				error.setDescription("Image not available");
				errors.add(error);
				return errors;
			}			
			
		} else {
			boolean isImgAvailableInFileSystem = false;
			if (isImageAvailable) {
				isImgAvailableInFileSystem = TIFFToPDFConverter.isImageAvailable(imagelocation);
			}
			if (!isImageAvailable || !isImgAvailableInFileSystem) {
				response.reset();
				response.resetBuffer();
				ErrorCode error = new ErrorCode();
				error.setCode("ERROR");
				error.setDescription("Image not available");
				errors.add(error);
				return errors;
			}
		}
	
			String requestImageUrl = request.getAttribute("requestImageUrl").toString();
			requestImageUrl = this.ecomClientURL + requestImageUrl;
			/**If the Document is not Available Added it to the Shopping Basket **/
			PayAsUGoTxItem item = this.getService().getPayAsUGoTxIdForPurchasedDoc(request.getRemoteUser(), productkey,
					uniqueidentifier);
			if (item == null) {
				response.reset();
				response.resetBuffer();
				ErrorCode error = this.addToTheShoppingCart(requestImageUrl, productkey, producttype, productName, numberofpages, accessname,
						uniqueidentifier, application, barNumber, locationName, state, isCertified, request);
				errors.add(error);
				return errors;
			} else {
					Map<String, ShoppingCartItem> shoppingCart = (HashMap<String, ShoppingCartItem>)request.getSession()
						.getAttribute(SHOPPING_CART + request.getRemoteUser());
					response.reset();
					response.resetBuffer();
					ErrorCode error = new ErrorCode();
					error.setCode("EXISTS");
					error.setModuleName(item.getPayAsUGoTxId() + "");
					error.setDescription(this.getMessage("ecom.webpurchase.itemalreadypurchased"));
					if (shoppingCart != null) {
						error.setCount(shoppingCart.size());
					}
					errors.add(error);
					return errors;
			}
		
	}

	@RequestMapping(value="/reviewShoppingCart.admin")
	public ModelAndView reviewShoppingCart(HttpServletRequest request, @RequestParam(defaultValue="false") boolean isReAu) {
		ModelAndView modelAndView = getModelAndView(request, ECOM_REVIEW_SHOPPING_CART);
		if (isReAu) {
			this.reAuthenticate(request);
		}
		List<ShoppingCartItem> itemList = new LinkedList<ShoppingCartItem>();
		itemList = this.getService().getShoppingBasketItems(request.getRemoteUser(), this.nodeName);
		List<ShoppingCartItem> shoppingCart = null;
		if (itemList != null && itemList.size() > 0) {
			try {
				PayAsUSubDTO payAsUGoTransactionDTO = new PayAsUSubDTO();
				payAsUGoTransactionDTO.setUserName(request.getRemoteUser());
				payAsUGoTransactionDTO.setShoppingCartItemList(itemList);
				shoppingCart = this.getService().doSalePayAsUGoInfo(payAsUGoTransactionDTO);
			} catch (SDLBusinessException sDLBusinessException) {
				request.getSession().setAttribute(BUSSINESS_EXCP, sDLBusinessException.getBusinessMessage());
			}
		}
		Map<String, ShoppingCartItem> shoppingCartMap = new LinkedHashMap<String, ShoppingCartItem>();
		if (itemList != null) {
			 for (ShoppingCartItem item : itemList) {
				 shoppingCartMap.put(item.getProductId() + item.getUniqueIdentifier(), item);
			 }
		}
		request.getSession().setAttribute(SHOPPING_CART + request.getRemoteUser(), shoppingCartMap);
		modelAndView.addObject(SHOPPING_CART, shoppingCart);
		return modelAndView;
	}

	@RequestMapping(value="/redirectPage.admin", method=RequestMethod.GET)
	public ModelAndView redirectPage(HttpServletRequest request) {
		ModelAndView modelAndView = getModelAndView(request, ECOM_REVIEW_SHOPPING_CART);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		request.getSession().removeAttribute(SHOPPING_CART + request.getRemoteUser());
		String url = this.ecomServerURL +  "secure/viewpayasugopaymentinfo.admin?"
		+ "token1="	  + PageStyleUtil.encrypt(((User)authentication.getPrincipal()).getUsername())
		+ "&token2="  + PageStyleUtil.encrypt(authentication.getCredentials().toString())
		+ "&token3="  + request.getParameter("return_url");
		modelAndView.setViewName("redirect:" + url);
		return modelAndView;
	}


	@RequestMapping(value="/updateShoppingCartComments.admin", method=RequestMethod.POST,  produces="application/json")
	@ResponseBody
	public JsonResponse updateShoppingCartComments(HttpServletRequest request, HttpServletResponse response,
			Long shoppingCartItemId, String comments){
		JsonResponse resp = new JsonResponse();
		try{
			boolean error = false;
			if(StringUtils.isBlank(comments)){
				resp.addError("Please Enter Comments");
				error = true;
			}
			if(shoppingCartItemId == null || shoppingCartItemId == 0){
				resp.addError("Shopping Cart item Id value is invalid");
				error = true;
			}
			if(!error){
				this.getService().updateShoppingCartComments(shoppingCartItemId, comments);
				resp.setErrorCode(SUCCESS);
				resp.addError("Comments Added Successfully.");
			}
		} catch(Throwable e){
			resp.setErrorCode(ERROR);
			resp.addError("Server Error, Please contact Administrator");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return resp;
	}



	private ErrorCode addToTheShoppingCart(String requestImageUrl, String productId, String producttype, String productName,
								 int numberofpages, String accessname, String uniqueidentifier, String application,
								 String barNumber, String locationName, String state, boolean isCertified, HttpServletRequest request) {
		ErrorCode error = new ErrorCode();
		UsernamePasswordAuthenticationToken userPasswordAuthToken
			= (UsernamePasswordAuthenticationToken) request.getUserPrincipal();
		User user = (User)userPasswordAuthToken.getPrincipal();

		ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
		shoppingCartItem.setCreatedBy(request.getRemoteUser());
		shoppingCartItem.setModifiedBy(request.getRemoteUser());
		shoppingCartItem.setModifiedDate(new Date());
		shoppingCartItem.setCreatedDate(new Date());
		shoppingCartItem.setUserId(user.getId());
		shoppingCartItem.setNodeName(this.nodeName);
		shoppingCartItem.setProductId(productId);
		shoppingCartItem.setProductType(producttype);
		shoppingCartItem.setProductName(productName);
		shoppingCartItem.setPageCount(numberofpages);
		shoppingCartItem.setDownloadURL(requestImageUrl);
		shoppingCartItem.setAccessName(accessname);
		shoppingCartItem.setUniqueIdentifier(uniqueidentifier);
		shoppingCartItem.setApplication(application);
		shoppingCartItem.setLocationName(locationName);
		shoppingCartItem.setStateCode(state);
		shoppingCartItem.setCertified(isCertified);
		if(!StringUtils.isBlank(barNumber)){
			shoppingCartItem.setBarNumber(barNumber);
		}
		Map<String, ShoppingCartItem> shoppingCart = (HashMap<String, ShoppingCartItem>)request.getSession()
			.getAttribute(SHOPPING_CART + request.getRemoteUser());
		/** This will be the First Time Scenario, where nothing Exist in the Session **/
		List<Code> codes = this.getService().getCodes("REGISTERED_APPLICATION");
		boolean isValidApplication = false;
		for (Code code : codes) {
			if (code.getCode().equals(application)) {
				isValidApplication = true;
			}
		}
		if (isValidApplication) {
			/**Shopping Cart is not Available in the Session **/
			if (shoppingCart == null) {
				shoppingCart =  new LinkedHashMap<String, ShoppingCartItem>();
				this.getService().saveShoppingCartItem(shoppingCartItem);
				shoppingCart.put(productId + uniqueidentifier, shoppingCartItem);
				error.setCode("SUCCESS");
				error.setDescription("Item Added Successfully");
				error.setCount(shoppingCart.size());
				request.getSession().setAttribute(SHOPPING_CART + request.getRemoteUser(), shoppingCart);
			/**Shopping Cart is Available in the Session **/
			} else {
				/** This Item is Not Available in the Shopping Cart **/
				if (shoppingCart.get(productId + uniqueidentifier) == null) {
					this.getService().saveShoppingCartItem(shoppingCartItem);
					shoppingCart.put(productId + uniqueidentifier, shoppingCartItem);
					error.setCode("SUCCESS");
					error.setDescription("Item Added Successfully");
					error.setCount(shoppingCart.size());
					request.getSession().setAttribute(SHOPPING_CART + request.getRemoteUser(), shoppingCart);
				} else {
					/** This Item is Already Available in the Shopping Cart **/
					error.setCode("ERROR");
					error.setDescription("Item is already added");
				}
			}
		} else {
			error.setCode("ERROR");
			error.setDescription("Invalid Application Name or Application is not registered with gateway.");
		}
		return error;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields(new String[] {
			"useExistingAccount",
			"accountName",
			"number",
			"expMonthS",
			"expYear",
			"cvv",
			"addressLine1",
			"addressLine2",
			"city",
			"state",
			"zip",
			"phoneNumber",
			"saveCreditCard",
			"login",
			"paynow",
			"reset",
			"ERROR",
			"SUCCESS",
			"btnPayNow",
			"accessname",
			"numberofpages",
			"prdtype",
			"prdkey",
			"uniqueidentifier"
		});
	}

}