package com.fdt.sdl.license;

import org.apache.commons.digester.Digester;
import java.io.StringReader;

public class FDTLicense {

	private String customerName = null;

	private String customerNumber = null;

	private String startDate = null;

	private String endDate = null;

	private String numberOfrecords = null;

	private String numberOfDatabase = null;

	private String ipAddress = null;

	public String toXML() {
		StringBuffer localStringBuffer = new StringBuffer();

		localStringBuffer.append("<license>\n");
		localStringBuffer.append("<customer-name><![CDATA[").append(this.customerName).append("]]></customer-name>\n");
		localStringBuffer.append("<customer-number><![CDATA[").append(this.customerNumber).append("]]></customer-number>\n");
		localStringBuffer.append("<start-date><![CDATA[").append(this.startDate).append("]]></start-date>\n");
		localStringBuffer.append("<end-date><![CDATA[").append(this.endDate).append("]]></end-date>\n");
		localStringBuffer.append("<number-of-records><![CDATA[").append(this.numberOfrecords).append("]]></number-of-records>\n");
		localStringBuffer.append("<number-of-database><![CDATA[").append(this.numberOfDatabase).append("]]></number-of-database>\n");
		localStringBuffer.append("<ip-address><![CDATA[").append(this.ipAddress).append("]]></ip-address>\n");
		localStringBuffer.append("</license>");
		return localStringBuffer.toString();
	}

	public static FDTLicense fromXML(String paramString) throws Exception {
		FDTLicense licence = null;
		Digester localDigester = new Digester();
		localDigester.setValidating(false);
		localDigester.addObjectCreate("license", FDTLicense.class);
		localDigester.addBeanPropertySetter("license/customer-name", "customerName");
		localDigester.addBeanPropertySetter("license/customer-number", "customerNumber");
		localDigester.addBeanPropertySetter("license/start-date", "startDate");
		localDigester.addBeanPropertySetter("license/end-date", "endDate");
		localDigester.addBeanPropertySetter("license/number-of-records", "numberOfrecords");
		localDigester.addBeanPropertySetter("license/number-of-database", "numberOfDatabase");
		localDigester.addBeanPropertySetter("license/ip-address", "ipAddress");
		StringReader localStringReader = new StringReader(paramString);
		licence = (FDTLicense) localDigester.parse(localStringReader);
		return licence;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getNumberOfrecords() {
		return numberOfrecords;
	}

	public void setNumberOfrecords(String numberOfrecords) {
		this.numberOfrecords = numberOfrecords;
	}

	public String getNumberOfDatabase() {
		return numberOfDatabase;
	}

	public void setNumberOfDatabase(String numberOfDatabase) {
		this.numberOfDatabase = numberOfDatabase;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
}