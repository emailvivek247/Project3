package com.fdt.ecom.ui.util;

import java.util.ArrayList;
import java.util.List;

public class ExportConstants {
	
	private static String USERNAME = "User Name";
	private static String PAYMENT_DATE = "Payment Date";
	private static String TRANSACTION_REF_NUMBER = "Transaction Reference Number";
	private static String SITE_NAME = "Site Name";
	private static String SUBSCRIPTION = "Subscription";
	private static String CARD_CHARGED = "Card Charged";
	private static String NAME_ON_CARD = "Name on Card";
	private static String TRANSACTION_TYPE = "Transaction Type";
	private static String AMOUNT = "Amount";
	

	
	public static List<String> getRecurHeaders(){
		List<String> headers = new ArrayList<String>();
		headers.add(PAYMENT_DATE);
		headers.add(TRANSACTION_REF_NUMBER);
		headers.add(SITE_NAME);
		headers.add(SUBSCRIPTION);
		headers.add(CARD_CHARGED);
		headers.add(NAME_ON_CARD);
		headers.add(TRANSACTION_TYPE);
		headers.add(AMOUNT);
		return headers;
	}
	
	public static List<String> getPayAsUGoHeaders(){
		List<String> headers = new ArrayList<String>();
		headers.add(USERNAME);
		headers.add(PAYMENT_DATE);
		headers.add(TRANSACTION_REF_NUMBER);
		headers.add(TRANSACTION_TYPE);
		headers.add(SITE_NAME);
		headers.add(SUBSCRIPTION);
		headers.add(CARD_CHARGED);
		headers.add(NAME_ON_CARD);
		headers.add(AMOUNT);
		return headers;
	}
}