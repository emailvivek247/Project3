package com.fdt.common.util.spring;

import java.io.IOException;

import org.apache.commons.lang.WordUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fdt.common.util.SystemUtil;
import com.fdt.payasugotx.entity.PayAsUGoTx;

public class PayAsUGoTxSerializer extends JsonSerializer<PayAsUGoTx> {

	@Override
	public Class<PayAsUGoTx> handledType()
	{
	  return PayAsUGoTx.class;
	}

	@Override
	public void serialize(PayAsUGoTx payAsUGoTx, JsonGenerator jsonGenerator, SerializerProvider provider)
			throws IOException, JsonGenerationException {
        jsonGenerator.writeStartObject();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
        if (payAsUGoTx.getSite() != null && payAsUGoTx.getSite().getTimeZone() != null) {
	        String timeZone = payAsUGoTx.getSite().getTimeZone();
	        String txDate = SystemUtil.getDateInTimezone(formatter.parseDateTime(
	        	payAsUGoTx.getTransactionDate().toString()).toDate(), timeZone.toString());
	       	jsonGenerator.writeStringField("transactionDate", txDate);
        }

       	jsonGenerator.writeStringField("id", String.valueOf(payAsUGoTx.getId()));

       	if (payAsUGoTx.getTxRefNum() != null) {
       		jsonGenerator.writeStringField("txRefNum", payAsUGoTx.getTxRefNum());
       	}

       	if (payAsUGoTx.getBaseAmount()!= null) {
       		jsonGenerator.writeStringField("baseAmount", String.valueOf(payAsUGoTx.getBaseAmount()));
       	}

       	jsonGenerator.writeStringField("serviceFee", String.valueOf(payAsUGoTx.getServiceFee()));

       	if (payAsUGoTx.getTotalTxAmount()!= null) {
       		jsonGenerator.writeStringField("totalTxAmount", String.valueOf(payAsUGoTx.getTotalTxAmount()));
       	}

       	if (payAsUGoTx.getCardType() != null) {
       		jsonGenerator.writeStringField("cardType", payAsUGoTx.getCardType().toString());
       	}

       	if (payAsUGoTx.getSettlementStatus() != null) {
       		jsonGenerator.writeStringField("settlementStatus", payAsUGoTx.getSettlementStatus().toString());
       	}

       	if (payAsUGoTx.getTransactionType() != null) {
       		jsonGenerator.writeStringField("transactionType", WordUtils.capitalizeFully(payAsUGoTx.getTransactionType().toString()));
       	}

       	if (payAsUGoTx.getCardNumber() != null) {
       		jsonGenerator.writeStringField("cardNumber", payAsUGoTx.getCardNumber());
       	}

       	if (payAsUGoTx.getAccountName() != null) {
       		jsonGenerator.writeStringField("accountName", WordUtils.capitalizeFully(payAsUGoTx.getAccountName()));
       	}

       	if (payAsUGoTx.getAuthCode() != null) {
       		jsonGenerator.writeStringField("authCode", payAsUGoTx.getAuthCode());
       	}

       	if (payAsUGoTx.getMachineName() != null) {
       		jsonGenerator.writeStringField("machineName", payAsUGoTx.getMachineName());
       	}

       	if (payAsUGoTx.getCheckNum() != null) {
       		jsonGenerator.writeStringField("checkNum", payAsUGoTx.getCheckNum());
       	}

       	if (payAsUGoTx.getOrigTxRefNum() != null) {
       		jsonGenerator.writeStringField("origTxRefNum", payAsUGoTx.getOrigTxRefNum());
       	}

		if (payAsUGoTx.getSite() != null) {
			jsonGenerator.writeStringField("siteDescription", payAsUGoTx.getSite().getDescription());
		}
		
		if (payAsUGoTx.getAccess() != null) {
       		jsonGenerator.writeStringField("accessDescription", payAsUGoTx.getAccess().getDescription());
       	}
		
		if (payAsUGoTx.getCreatedBy() != null) {
       		jsonGenerator.writeStringField("createdBy", payAsUGoTx.getCreatedBy());
       	}
		
		if (payAsUGoTx.getItemsPurchased() != null) {
       		jsonGenerator.writeStringField("itemsPurchased", String.valueOf(payAsUGoTx.getItemsPurchased()));
       	}
		
		

		jsonGenerator.writeEndObject();
	}
}
