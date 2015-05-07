package com.fdt.ecom.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.fdt.ecom.entity.enums.CardType;

public class CreditCardUtil {

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
}
