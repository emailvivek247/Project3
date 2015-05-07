package com.fdt.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Provider;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;

import com.fdt.ecom.entity.enums.CardType;

public class SystemUtil {

    private static Logger logger = LoggerFactory.getLogger(SystemUtil.class.getName());

    @Value("${role.psotxadmin}")
    private String PSOTxAdmin = null;

    @Value("${role.psosuperadmin}")
    private String PSOSuperAdmin = null;

    @Value("${role.psouseradmin}")
    private String PSOUserAdmin = null;

    @Value("${tx.ValidityPeriod}")
    /* Default Value is 60 Days */
    private String txValidityPeriod = "60";

    private static String PASSWORD_KEY = "dookudu";

    private static StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

    static {
        Provider p = new BouncyCastleProvider();
        encryptor.setProvider(p);
        encryptor.setPassword(PASSWORD_KEY);
        encryptor.setStringOutputType("HEXADECIMAL");
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

    public boolean isValidRefund(Date startDate) {
        boolean isValidTxForRefund = true;
        if (Days.daysBetween(new DateTime(startDate).toDateMidnight(),
                new DateTime().toDateMidnight()).getDays()  > Integer.valueOf(txValidityPeriod)) {
            isValidTxForRefund = false;
        }
        return isValidTxForRefund;
    }

    public static String format(String inputDateStr, String inputFormat, String outputFormat) {
        String outputDateStr = null;
        if (inputDateStr == null || inputDateStr.isEmpty()) {
            return inputDateStr;
        }
        try {
            DateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
            Date inputDate = inputDateFormat.parse(inputDateStr);
            DateFormat outputDateFormat = new SimpleDateFormat(outputFormat);
            outputDateStr = outputDateFormat.format(inputDate);
        } catch (ParseException parseException) {
            parseException.printStackTrace();
        }
        return outputDateStr;
    }

    public static String getBuildVersion() {
        String majorNumber = getSystemValue(null, "build.major.number");
        String minorNumber = getSystemValue(null, "build.minor.number");
        String revisionNumber = getSystemValue(null, "build.revision.number");
        return majorNumber + "." + minorNumber +  "." + revisionNumber;
    }

    public static String getBuildDate() {
        String buildDate = getSystemValue(null, "build.date");
        return buildDate;
    }

    public static String getSystemValue(String bundle, String key) {
        String returnedValue = null;
        ResourceBundle resourceBundle = null;
        try {
            if (bundle == null) {
                resourceBundle = ResourceBundle.getBundle("build");
            } else {
                resourceBundle = ResourceBundle.getBundle(bundle);
            }
            returnedValue = (String)resourceBundle.getString(key);
        } catch (Exception exp) {
            logger.error("The Resource bundle/Key is not Found " + key);
        }
        return returnedValue;
    }

    public boolean isPSOSuperAdmin(HttpServletRequest request) {
        return request.isUserInRole(PSOSuperAdmin);
    }

    public boolean isPSOTxAdmin(HttpServletRequest request) {
        return request.isUserInRole(PSOTxAdmin);
    }

    public boolean isPSOUserAdmin(HttpServletRequest request) {
        return request.isUserInRole(PSOUserAdmin);
    }

    public boolean isInternalUser(HttpServletRequest request) {
        boolean isInternaluser = false;
        UsernamePasswordAuthenticationToken userPasswordAuthToken
                = (UsernamePasswordAuthenticationToken) request.getUserPrincipal();
        Object user = userPasswordAuthToken.getPrincipal();
        if (user instanceof LdapUserDetailsImpl ){
            isInternaluser = true;
        }
        return isInternaluser;
    }

	public static String encodeURL(String aURLFragment) {
		if (aURLFragment == null)
			return "";
		String result = null;
		try {
			result = URLEncoder.encode(aURLFragment, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("UTF-8 not supported", ex);
		}
		return result;
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
	
    public static String convertCamelCase(String s) {
    	return WordUtils.capitalizeFully(s);
    }

    public static String getDateInTimezone(Date date, String timeZone) {
    	DateTime dateTime = new DateTime(date);
    	DateTime dateTimeInTimezone = dateTime.withZoneRetainFields(DateTimeZone.forID(timeZone));
    	DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm a zzz");
    	String dateAsString = format.print(dateTimeInTimezone);
    	return dateAsString;
    }
}