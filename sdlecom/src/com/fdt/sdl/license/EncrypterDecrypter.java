package com.fdt.sdl.license;


public class EncrypterDecrypter {

	
	public static String decryptXML(String licenseXML) throws Exception {
		String deCryptedXML = null;
		FDTLicense license = FDTLicense.fromXML(licenseXML);
		Decrypter deCrypter = new Decrypter();

		/** Decode the Customer Name **/	
		String customerName = deCrypter.deCrypt(license.getCustomerName());
		/** Decode the Customer Number **/		
		String customerNumber = deCrypter.deCrypt(license.getCustomerNumber());
		/** Decode the Start Date **/		
		String startDate = deCrypter.deCrypt(license.getStartDate());
		/** Decode the End Date **/		
		String endDate = deCrypter.deCrypt(license.getEndDate());
		/** Decode the Number Of Records **/
		String noOfRecords = deCrypter.deCrypt(license.getNumberOfrecords());
		/** Decode the Number of Databases **/
		String noOfDatabases = deCrypter.deCrypt(license.getNumberOfDatabase());
		/** Decode the IP Address **/
		String ipAddress = deCrypter.deCrypt(license.getIpAddress());
		
		license.setCustomerName(customerName);
		license.setCustomerNumber(customerNumber);
		license.setStartDate(startDate);
		license.setEndDate(endDate);
		license.setNumberOfrecords(noOfRecords);
		license.setNumberOfDatabase(noOfDatabases);
		license.setIpAddress(ipAddress);
		deCryptedXML = license.toXML();
		return deCryptedXML;
	}	

}