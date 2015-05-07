package net.javacoding.xsearch.config;

import java.util.ArrayList;
import java.util.List;

import com.fdt.sdl.license.License;

import net.javacoding.xsearch.utility.U;


class RegistrationInformation {
	int version = 1; 
    String user;
    /**
     * 0 is free
     * 1 is standard
     * 2 is enterprise
     */
    int licenseLevel = 0;
    String startDate;
    String upgradeEndDate;
    String endDate;
    int maxIndexSize = ConfigConstants.FREE_SIZE_LIMIT;
    String registrationNumber;
    List<String> allowedIpList;

    String registrationCode;

    void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    void setUpgradeEndDate(String upgradeEndDate) {
        this.upgradeEndDate = upgradeEndDate;
    }
    String getUpgradeEndDate() {
        return upgradeEndDate;
    }

    String getEndDate() {
        return endDate;
    }

    void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    int getLicenseLevel() {
        return licenseLevel;
    }

    void setLicenseLevel(int licenseLevel) {
        this.licenseLevel = licenseLevel;
    }

    String getRegistrationCode() {
        return registrationCode;
    }

    void setRegistrationCode(String registrationCode) {
        this.registrationCode = (registrationCode==null?null:registrationCode.trim());
    }

    int getMaxIndexSize() {
        return maxIndexSize;
    }

    void setMaxIndexSize(int indexSize) {
        this.maxIndexSize = indexSize;
    }

    String getRegistrationNumber() {
        return registrationNumber;
    }

    void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = (registrationNumber==null?null:registrationNumber.trim());
    }

    String getUser() {
        return user;
    }

    void setUser(String user) {
        this.user = (user==null?null:user.trim());
    }

    protected static String formatCode(String code) {
        if(code==null) return "";
        code = code.trim();
        code = code.replaceAll("[\\r]\\n", "");
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<code.length();i+=60) {
            sb.append("\n").append(code.substring(i, (i+60>code.length()? code.length() : i + 60)));
        }
        sb.append("\n");
        return sb.toString();
    }
    
    boolean isValidFormat() {
        return true;
    }
    /**
     * Returns an XML representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("  <registration-information>\n");
        sb.append("  </registration-information>\n");

        return sb.toString();
    }
    
    /*
     * Current release is valid for this license
     */
    boolean canLicenseAppliedToThisRelease() {
        return true;
    }
    
    void resetLicenseCache(){
    }
    int getAllowedLicenseLevel() {
        return getLicenseLevel();
    }
    int getAllowedMaxIndexSize() {
        return getMaxIndexSize();
    }

	public List<String> getAllowedIpList() {
		return allowedIpList;
	}

	public void setAllowedIpsString(String allowedIpsString) {
        this.allowedIpList = new ArrayList<String>();
        if(allowedIpsString!=null) {
            for(String ip : allowedIpsString.split(",")) {
                if(!U.isEmpty(ip)) {
                    this.allowedIpList.add(ip);
                }
            }
        }
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

    public void setLicense(License license) {
        this.version = license.version; 
        this.user = license.user;
        this.licenseLevel = license.licenseLevel;
        this.startDate = license.startDate;
        this.upgradeEndDate = license.upgradeEndDate;
        this.endDate = license.endDate;
        this.maxIndexSize = license.maxIndexSize;
        this.registrationNumber = license.customerNumber;
        this.allowedIpList = license.allowedIpList;

        this.registrationCode = license.code;
    }

    public License getLicense() {
        License x = new License();
        x.user = this.user;
        x.version = this.version;
        x.licenseLevel = this.licenseLevel;
        x.startDate = this.startDate;
        x.upgradeEndDate = this.upgradeEndDate;
        x.endDate = this.endDate;
        x.maxIndexSize = this.maxIndexSize;
        x.customerNumber = this.registrationNumber;
        x.allowedIpList = this.allowedIpList;
        x.code = this.registrationCode;
        return x;
    }

}
