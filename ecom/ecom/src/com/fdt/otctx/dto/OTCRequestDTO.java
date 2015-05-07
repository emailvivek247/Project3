package com.fdt.otctx.dto;


public class OTCRequestDTO {

	private String productType = null;

	private String itemName = null;

    private String siteName =  null;

    private double actualAmtToCharge = 0.0d;

    private String machineName =  null;

    private String userLogged =  null;

    private String encTrackOne =  null;

    private String encTrackTwo =  null;

    private String encTrackThree =  null;

    private String encMp =  null;

    private String ksn =  null;

    private String mPrintStatus =  null;

    private byte[] signature =  null;

    private String officeLoc =  null;

    private String comments = null;

    private String officeLocAdr1 =  null;

    private String officeLocAdr2 =  null;

    private String officeLocCity =  null;

    private String officeLocState =  null;

    private String officeLocZip =  null;

    private String officeLocPhone =  null;

    private String officeLocComments1 =  null;

    private String officeLocComments2 =  null;

    private String invoiceNumber = null;

    private double tax = 0.0d;

    public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public double getActualAmtToCharge() {
        return actualAmtToCharge;
    }

    public void setActualAmtToCharge(double actualAmtToCharge) {
        this.actualAmtToCharge = actualAmtToCharge;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getUserLogged() {
        return userLogged;
    }

    public void setUserLogged(String userLogged) {
        this.userLogged = userLogged;
    }

    public String getEncTrackOne() {
        return encTrackOne;
    }

    public void setEncTrackOne(String encTrackOne) {
        this.encTrackOne = encTrackOne;
    }

    public String getEncTrackTwo() {
        return encTrackTwo;
    }

    public void setEncTrackTwo(String encTrackTwo) {
        this.encTrackTwo = encTrackTwo;
    }

    public String getEncTrackThree() {
        return encTrackThree;
    }

    public void setEncTrackThree(String encTrackThree) {
        this.encTrackThree = encTrackThree;
    }

    public String getEncMp() {
        return encMp;
    }

    public void setEncMp(String encMp) {
        this.encMp = encMp;
    }

    public String getKsn() {
        return ksn;
    }

    public void setKsn(String ksn) {
        this.ksn = ksn;
    }

    public String getMprintStatus() {
        return mPrintStatus;
    }

    public void setMprintStatus(String mPrintStatus) {
        this.mPrintStatus = mPrintStatus;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public String getOfficeLoc() {
        return officeLoc;
    }

    public void setOfficeLoc(String officeLoc) {
        this.officeLoc = officeLoc;
    }

    public String getOfficeLocAdr1() {
        return officeLocAdr1;
    }

    public void setOfficeLocAdr1(String officeLocAdr1) {
        this.officeLocAdr1 = officeLocAdr1;
    }

    public String getOfficeLocAdr2() {
        return officeLocAdr2;
    }

    public void setOfficeLocAdr2(String officeLocAdr2) {
        this.officeLocAdr2 = officeLocAdr2;
    }

    public String getOfficeLocCity() {
        return officeLocCity;
    }

    public void setOfficeLocCity(String officeLocCity) {
        this.officeLocCity = officeLocCity;
    }

    public String getOfficeLocState() {
        return officeLocState;
    }

    public void setOfficeLocState(String officeLocState) {
        this.officeLocState = officeLocState;
    }

    public String getOfficeLocZip() {
        return officeLocZip;
    }

    public void setOfficeLocZip(String officeLocZip) {
        this.officeLocZip = officeLocZip;
    }

    public String getOfficeLocPhone() {
        return officeLocPhone;
    }

    public void setOfficeLocPhone(String officeLocPhone) {
        this.officeLocPhone = officeLocPhone;
    }

    public String getOfficeLocComments1() {
        return officeLocComments1;
    }

    public void setOfficeLocComments1(String officeLocComments1) {
        this.officeLocComments1 = officeLocComments1;
    }

    public String getOfficeLocComments2() {
        return officeLocComments2;
    }

    public void setOfficeLocComments2(String officeLocComments2) {
        this.officeLocComments2 = officeLocComments2;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	@Override
	public String toString() {
		return "OTCRequestDTO [siteName=" + siteName + ", actualAmtToCharge="
				+ actualAmtToCharge + ", machineName=" + machineName
				+ ", userLogged=" + userLogged + ", officeLoc=" + officeLoc
				+ ", comments=" + comments + ", invoiceNumber=" + invoiceNumber
				+ "]";
	}


}