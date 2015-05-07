package com.fdt.test;

import static com.fdt.common.SystemConstants.CERTIFIED_DOCUMENT_NUMBER_LENGTH;

import java.util.UUID;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class EcomTest {

    public EcomTest() {
    }



    //@Test
    public void testPhoneRegularExpression() {
    	String phoneExp = "^\\(\\d{3}\\) ?\\d{3}( |-)?\\d{4}|^\\d{3}( |-)?\\d{3}( |-)?\\d{4}";

    	Assert.assertTrue(Pattern.matches(phoneExp, "1112223333"));
    	Assert.assertTrue(Pattern.matches(phoneExp, "111-222-3333"));
    	Assert.assertTrue(Pattern.matches(phoneExp, "(111) 222 3333"));
    	Assert.assertTrue(Pattern.matches(phoneExp, "(111)222-3333"));
    	Assert.assertTrue(Pattern.matches(phoneExp, "(111) 222 3333"));
    	Assert.assertTrue(Pattern.matches(phoneExp, "111 222 3333"));

    	Assert.assertFalse(Pattern.matches(phoneExp, "(11122 3333"));
    	Assert.assertFalse(Pattern.matches(phoneExp, "(111) 222 333"));
    	Assert.assertFalse(Pattern.matches(phoneExp, "(111) 22 3333"));
    	Assert.assertFalse(Pattern.matches(phoneExp, "(11) 222 3333"));

    }


    //@Test
    public void testEmailRegularExpression() {
    	String emailExp = "\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b";

    	Assert.assertTrue(Pattern.matches(emailExp, "abc@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "abc1@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "abc_abc@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "abc9.abc@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "abc9_abc.com@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "123abc@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "abc123@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "ab123c@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "ab123c@te_st.com"));

    	Assert.assertTrue(Pattern.matches(emailExp, "abc@test"));
    	Assert.assertTrue(Pattern.matches(emailExp, "_abc@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "a'bc@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "ab'_c@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "a%bc@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "abc._@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "abc@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "abc@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "abc@test.com"));
    	Assert.assertTrue(Pattern.matches(emailExp, "abc@test.com"));

    }


    @Test
    public void testAssertState(){
    	double amtToCharge = 0.2;
    	org.springframework.util.Assert.state(amtToCharge > 0.0, "Amount To be chaged must be greater than zero!");
    	org.springframework.util.Assert.state(amtToCharge < 5, "Amount To be chaged must be greater than zero!");

    }

    @Test
    public void generateRandomString(){
    	String randomString = UUID.randomUUID().toString();
    	randomString = randomString.replaceAll("-", "");
    	randomString = randomString.substring(0, CERTIFIED_DOCUMENT_NUMBER_LENGTH).toUpperCase();
    	System.out.println(randomString);
    	String certifiedDocumentNumber =
    			randomString.substring(0, 3) + "-" + randomString.substring(3, 7) + "-"
    			+ randomString.substring(7, 11) + "-" + randomString.substring(11, 15);
    	System.out.println(certifiedDocumentNumber);
    }


}