package com.fdt.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Provider;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.ecom.entity.enums.CardType;

public class SystemUtil {

    private static Logger logger = LoggerFactory.getLogger(SystemUtil.class.getName());

    private static String PASSWORD_KEY = "dookudu";

    private static StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

    /**
     * This has been added to avoid Memory Leak. As Bounty Castle is causing Memory Leaks.
     */
    static {
        Provider p = new BouncyCastleProvider();
        encryptor.setProvider(p);
        encryptor.setPassword(PASSWORD_KEY);
        encryptor.setStringOutputType("HEXADECIMAL");
    }

    public static String[] tokenizeToStringArray(String str, String delimiters,	boolean trimTokens, boolean ignoreEmptyTokens) {
		if (str == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0) {
				tokens.add(token);
			}
		}
		return tokens.toArray(new String[tokens.size()]);
	}
    
    public static void main (String args[]) {
        System.out.println(encrypt("vivek"));
    }
    public static String encrypt(String plainText) {
        String encryptedText = null;
        try {
            encryptedText = encryptor.encrypt(plainText);
        } catch (Exception exception) {
            logger.error("Exception Occured in Encrypting the Data plainText{}", plainText, exception);
        }
        return encryptedText;
    }

    public static String decrypt(String encryptedText) {
        String decryptedText = null;
        try {
            decryptedText = encryptor.decrypt(encryptedText);
        } catch (Exception exception) {
            logger.error("Exception Occured in Decrypting the Data encryptedText{}", encryptedText, exception);
        }
        return decryptedText;
    }

    public static String format(String inputDateStr, String inputFormat, String outputFormat) {
        String outputDateStr = "";
        if (inputDateStr == null || inputDateStr.isEmpty()) {
               return inputDateStr;
        }
        try {
            DateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
            Date inputDate = inputDateFormat.parse(inputDateStr);
            DateFormat outputDateFormat = new SimpleDateFormat(outputFormat);
            outputDateStr =  outputDateFormat.format(inputDate);
        } catch (ParseException parseException) {
            logger.error("Error Occured in format", parseException);
        }
        return outputDateStr;
    }

    public static String format(String inputDateStr) {
        String outputDateStr = null;
        outputDateStr= format(inputDateStr, "EEE MMM dd hh:mm:ss zzz yyyy","yyyy/MM/dd hh:mm:ss a");
        return outputDateStr;
    }

    public static String decodeURL(String aURLFragment) {
        if (aURLFragment == null)
            return "";
        String result = null;
        try {
            result = URLDecoder.decode(aURLFragment, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 not supported", ex);
        }
        return result;
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

    public static DateTime getNextBillingDate(String paymentPeriod) {
        DateTime dateTime = new DateTime();
        if ("WEEK".equalsIgnoreCase(paymentPeriod)) {
            dateTime = dateTime.plusWeeks(1);
        } else if ("BIWK".equalsIgnoreCase(paymentPeriod)) {
            dateTime = dateTime.plusWeeks(2);
        } else if ("FRWK".equalsIgnoreCase(paymentPeriod)) {
            dateTime = dateTime.plusWeeks(4);
        } else if ("MONT".equalsIgnoreCase(paymentPeriod)) {
            dateTime = dateTime.plusMonths(1);
        } else if ("QTER".equalsIgnoreCase(paymentPeriod)) {
            dateTime = dateTime.plusMonths(3);
        } else if ("SMYR".equalsIgnoreCase(paymentPeriod)) {
            dateTime = dateTime.plusMonths(6);
        } else if ("YEAR".equalsIgnoreCase(paymentPeriod)) {
            dateTime = dateTime.plusMonths(12);
        }
        return dateTime.withTimeAtStartOfDay().toDateTime();
    }

	public static CardType getCardType(String accountNumber) {

		if(StringUtils.isBlank(accountNumber)) {
            return null;
        }
        Matcher masterCardMatcher = Pattern.compile("^5[0-9]{15}$").matcher(accountNumber);
        if(masterCardMatcher.matches()) {
            return CardType.MASTER;
        }
        Matcher visaMatcher = Pattern.compile("^4[0-9]{12,15}$").matcher(accountNumber);
        if(visaMatcher.matches()) {
            return CardType.VISA;
        }
        Matcher amexMatcher = Pattern.compile("^3[47][0-9]{13}$").matcher(accountNumber);
        if(amexMatcher.matches()) {
            return CardType.AMEX;
        }
        Matcher discoverMatcher = Pattern.compile("^6011[0-9]{12}$").matcher(accountNumber);
        if(discoverMatcher.matches()) {
            return CardType.DISCOVER;
        }
        return null;
    }

	public static Set<String> getPaymentTokens(String paymentTokens) {
		Set<String>  tokenList = new HashSet<String>();
		StringTokenizer tokenizer = new StringTokenizer(paymentTokens, "|");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			tokenList.add(token);		   
		}
		return tokenList;
	}
}
