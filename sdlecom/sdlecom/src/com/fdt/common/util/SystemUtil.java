package com.fdt.common.util;

import java.security.Provider;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemUtil {

	private static Logger logger = LoggerFactory.getLogger(SystemUtil.class.getName());

	private static String PASSWORD_KEY = "dookudu";

	public static String encrypt(String plainText) {
		String encryptedText = null;
		if (StringUtils.isBlank(plainText)) {
			return plainText;
		}
		try {
			StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
			encryptor.setPassword(PASSWORD_KEY);
			Provider p = new BouncyCastleProvider();
			encryptor.setProvider(p);
			encryptor.setStringOutputType("HEXADECIMAL");
			encryptedText = encryptor.encrypt(plainText);
		} catch (Exception exception) {
			logger.error("Exception Occured in Encrypting the Data plainText{}", plainText, exception);
		}
		return encryptedText;
	}

	public static String decrypt(String encryptedText) {
		String decryptedText = null;
		if (StringUtils.isBlank(encryptedText)) {
			return encryptedText;
		}
		try {
			StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
			encryptor.setPassword(PASSWORD_KEY);
			Provider p = new BouncyCastleProvider();
			encryptor.setProvider(p);
			encryptor.setStringOutputType("HEXADECIMAL");
			decryptedText = encryptor.decrypt(encryptedText);
		} catch (Exception exception) {
			logger.error("Exception Occured in Decrypting the Data encryptedText{}", encryptedText, exception);
		}
		return decryptedText;
	}

    public static String decrypt(String encryptedText, String algorithm) {
    	String decryptedText =  null;
		if (StringUtils.isBlank(encryptedText)) {
			return encryptedText;
		}
    	try {
	    	StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
	    	encryptor.setAlgorithm(algorithm);
			encryptor.setPassword(PASSWORD_KEY);
			Provider p = new BouncyCastleProvider();
			encryptor.setProvider(p);
			encryptor.setStringOutputType("HEXADECIMAL");
			decryptedText = encryptor.decrypt(encryptedText);
		} catch (Exception exception) {
			logger.error("Exception Occured in Decrypting the Data", exception);
		}
    	return decryptedText;
    }

    public static String encrypt(String plainText, String algorithm) {
    	String encryptedText =  null;
		if (StringUtils.isBlank(plainText)) {
			return encryptedText;
		}
    	try {
	    	StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
	    	encryptor.setAlgorithm(algorithm);
			encryptor.setPassword(PASSWORD_KEY);
			Provider p = new BouncyCastleProvider();
			encryptor.setProvider(p);
			encryptor.setStringOutputType("HEXADECIMAL");
			encryptedText = encryptor.encrypt(plainText);
		} catch (Exception exception) {
			logger.error("Exception Occured in Encrypting the Data", exception);
		}
    	return encryptedText;
    }

    public static Date changeTimeZone(Date date, TimeZone zone) {
        Calendar first = Calendar.getInstance(zone);
        first.setTimeInMillis(date.getTime());
        Calendar output = Calendar.getInstance();
        output.set(Calendar.YEAR, first.get(Calendar.YEAR));
        output.set(Calendar.MONTH, first.get(Calendar.MONTH));
        output.set(Calendar.DAY_OF_MONTH, first.get(Calendar.DAY_OF_MONTH));
        output.set(Calendar.HOUR_OF_DAY, first.get(Calendar.HOUR_OF_DAY));
        output.set(Calendar.MINUTE, first.get(Calendar.MINUTE));
        output.set(Calendar.SECOND, first.get(Calendar.SECOND));
        output.set(Calendar.MILLISECOND, first.get(Calendar.MILLISECOND));
        return output.getTime();
    }
    
    public static String getDateInTimezone(Date date, String timeZone) {
    	DateTime dateTime = new DateTime(date);
    	DateTime dateTimeInTimezone = dateTime.withZoneRetainFields(DateTimeZone.forID(timeZone));
    	DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm a zzz");
    	String dateAsString = format.print(dateTimeInTimezone);
    	return dateAsString;
    }

}