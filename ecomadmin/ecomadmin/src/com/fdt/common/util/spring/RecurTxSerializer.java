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
import com.fdt.recurtx.entity.RecurTx;

public class RecurTxSerializer extends JsonSerializer<RecurTx> {

	@Override
	public Class<RecurTx> handledType()
	{
	  return RecurTx.class;
	}

	@Override
	public void serialize(RecurTx recurtx, JsonGenerator jsonGenerator, SerializerProvider provider)
			throws IOException, JsonGenerationException {
        jsonGenerator.writeStartObject();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
        if (recurtx.getSite() != null && recurtx.getSite().getTimeZone() != null) {
	        String timeZone = recurtx.getSite().getTimeZone();
	        String txDate = SystemUtil.getDateInTimezone(formatter.parseDateTime(
	        		recurtx.getTransactionDate().toString()).toDate(), timeZone.toString());
	       	jsonGenerator.writeStringField("transactionDate", txDate);
        }

       	jsonGenerator.writeStringField("id", String.valueOf(recurtx.getId()));

       	if (recurtx.getTxRefNum() != null) {
       		jsonGenerator.writeStringField("txRefNum", recurtx.getTxRefNum());
       	}

       	if (recurtx.getBaseAmount()!= null) {
       		jsonGenerator.writeStringField("baseAmount", String.valueOf(recurtx.getBaseAmount()) + "0");
       	}

       	if (recurtx.getCardType() != null) {
       		jsonGenerator.writeStringField("cardType", recurtx.getCardType().toString());
       	}

       	if (recurtx.getSettlementStatus() != null) {
       		jsonGenerator.writeStringField("settlementStatus", recurtx.getSettlementStatus().toString());
       	}

       	if (recurtx.getTransactionType() != null) {
       		jsonGenerator.writeStringField("transactionType", WordUtils.capitalizeFully(recurtx.getTransactionType().toString()));
       	}

       	if (recurtx.getCardNumber() != null) {
       		jsonGenerator.writeStringField("cardNumber", recurtx.getCardNumber());
       	}

       	if (recurtx.getAccountName() != null) {
       		jsonGenerator.writeStringField("accountName", WordUtils.capitalizeFully(recurtx.getAccountName()));
       	}

       	if (recurtx.getAuthCode() != null) {
       		jsonGenerator.writeStringField("authCode", recurtx.getAuthCode());
       	}

       	if (recurtx.getMachineName() != null) {
       		jsonGenerator.writeStringField("machineName", recurtx.getMachineName());
       	}

       	if (recurtx.getCheckNum() != null) {
       		jsonGenerator.writeStringField("checkNum", recurtx.getCheckNum());
       	}

       	if (recurtx.getOrigTxRefNum() != null) {
       		jsonGenerator.writeStringField("origTxRefNum", recurtx.getOrigTxRefNum());
       	}

		if (recurtx.getSite() != null) {
			jsonGenerator.writeStringField("siteDescription", recurtx.getSite().getDescription());
		}
		
		if (recurtx.getAccess() != null) {
       		jsonGenerator.writeStringField("accessDescription", recurtx.getAccess().getDescription());
       	}
		
		if (recurtx.getCreatedBy() != null) {
       		jsonGenerator.writeStringField("createdBy", recurtx.getCreatedBy());
       	}

		jsonGenerator.writeEndObject();
	}
}
