package com.fdt.common.util.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.ws.client.Document;
import com.fdt.sdl.ws.client.DocumentRetrievalService;
import com.fdt.sdl.ws.exception.DocumentRetrievalServiceException;


public class DocumentManagementSystemAdapter {

	private final static Logger logger = LoggerFactory.getLogger(DocumentManagementSystemAdapter.class);
	
	private static DocumentRetrievalService documentRetrievalService = null;
	
	private static String documentManagementSystemWsdl = null;
	
	
	public SDLDMSDocument getDocumentBySeqKey(String seqKey){
		SDLDMSDocument sdldmsDocument = new SDLDMSDocument();
		
		try {
			logger.info("Retriving Doc From DocumentManagementSystem wsdl: {}, Seq Key: {}", documentManagementSystemWsdl, seqKey);
			Document document = documentRetrievalService.getDocumentBySeqKey(documentManagementSystemWsdl, seqKey);
			sdldmsDocument.setDocType(document.getDocType());
			sdldmsDocument.setFileExtension(document.getFileExtension());
			sdldmsDocument.setFile(document.getFile());
		} catch (DocumentRetrievalServiceException e) {
			logger.error("Exeption While Retriving Doc From DocumentManagementSystem wsdl: {}, Seq Key: {} Error Code: {} Error Description: {}", documentManagementSystemWsdl, seqKey, e.getErrorCode(), e.getErrorDescription());
			sdldmsDocument.setErrorCode(e.getErrorCode());
			sdldmsDocument.setErrorDescription(e.getErrorDescription());
		}
		return sdldmsDocument;
	}
	
	public SDLDMSDocument getDocumentByDMSID(String dMSID){
		SDLDMSDocument sdldmsDocument = new SDLDMSDocument();
		try {
			logger.info("Retriving Doc From DocumentManagementSystem wsdl: {}, DMS ID: {} ", documentManagementSystemWsdl, dMSID);
			Document document = documentRetrievalService.getDocumentByDMSID(documentManagementSystemWsdl, dMSID);
			sdldmsDocument.setDocType(document.getDocType());
			sdldmsDocument.setFileExtension(document.getFileExtension());
			sdldmsDocument.setFile(document.getFile());
		} catch (DocumentRetrievalServiceException e) {
			logger.error("Exeption While Retriving Doc From DocumentManagementSystem wsdl: {}, DMS ID: {} Error Code: {} Error Description: {}", documentManagementSystemWsdl, dMSID, e.getErrorCode(), e.getErrorDescription());
			sdldmsDocument.setErrorCode(e.getErrorCode());
			sdldmsDocument.setErrorDescription(e.getErrorDescription());
		}
		return sdldmsDocument;		
	}

	public static void setDocumentManagementSystemWsdl(String documentManagementSystemWsdl) {
		DocumentManagementSystemAdapter.documentManagementSystemWsdl = documentManagementSystemWsdl;
	}

	public static void setDocumentRetrievalService(DocumentRetrievalService documentRetrievalService) {
		DocumentManagementSystemAdapter.documentRetrievalService = documentRetrievalService;
	}	
	
}
