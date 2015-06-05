package com.fdt.sdl.license;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import net.javacoding.xsearch.config.ConfigConstants;

import org.apache.commons.digester.Digester;

public class License {

    public String user;
    public String customerNumber;
    public String startDate;
    public String upgradeEndDate;
    public String endDate;
    public int maxIndexSize = ConfigConstants.FREE_SIZE_LIMIT;
    public List<String> allowedIpList;
    
    /*
     * 0 is free
     * 10 is standard    //limited by index size
     * 100 is enterprise //support remote index replication
     * 1000 is internet //support multiple data sources
     */
    public int licenseLevel;
    public int version;
    public String code;

    public String toXML() {
        StringBuffer sb = new StringBuffer();

        sb.append("<license>\n");
        sb.append(" <user><![CDATA[").append(this.user).append("]]></user>\n");
        sb.append(" <customer-number>").append(this.customerNumber).append("</customer-number>\n");
        sb.append(" <start-date>").append(this.startDate).append("</start-date>\n");
        if(version>=3) {
            sb.append(" <upgrade-end-date>").append(this.upgradeEndDate).append("</upgrade-end-date>\n");
        }
        sb.append(" <end-date>").append(this.endDate).append("</end-date>\n");
        sb.append(" <max-index-size>").append(this.maxIndexSize).append("</max-index-size>\n");
        sb.append(" <allowed-ip-list>\n");
        if(this.allowedIpList!=null)for(String ip : this.allowedIpList) {
            sb.append("  <string>").append(ip).append("</string>\n");
        }
        sb.append(" </allowed-ip-list>\n");
        sb.append(" <license-level>").append(this.licenseLevel).append("</license-level>\n");
        sb.append(" <version>").append(this.version).append("</version>\n");
        sb.append(" <code><![CDATA[\n");
        sb.append(this.code.trim()).append("\n");
        sb.append(" ]]></code>\n");
        sb.append("</license>");

        return sb.toString();
    }

    public static License fromXML(String xml) {
        try {
            Digester digester = new Digester();
            digester.setValidating( false );
            digester.addObjectCreate( "license", License.class );
            digester.addBeanPropertySetter("license/user", "user");
            digester.addBeanPropertySetter("license/customer-number", "customerNumber");
            digester.addBeanPropertySetter("license/start-date", "startDate");
            digester.addBeanPropertySetter("license/upgrade-end-date", "upgradeEndDate");
            digester.addBeanPropertySetter("license/end-date", "endDate");
            digester.addBeanPropertySetter("license/max-index-size", "maxIndexSize");
            digester.addObjectCreate("license/allowed-ip-list", java.util.ArrayList.class);
            digester.addCallMethod("license/allowed-ip-list/string", "add", 1);
            digester.addCallParam("license/allowed-ip-list/string", 0);
            digester.addSetNext("license/allowed-ip-list", "setAllowedIpList");
            digester.addBeanPropertySetter("license/license-level", "licenseLevel");
            digester.addBeanPropertySetter("license/version", "version");
            digester.addBeanPropertySetter("license/code", "code");

            Reader reader = new StringReader(xml);
            License license = (License)digester.parse( reader );

            return license;
        } catch( Exception exc ) {
            exc.printStackTrace();
        }
        return null;
    }

    
    //getters and setters

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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

    public String getUpgradeEndDate() {
        return upgradeEndDate;
    }

    public void setUpgradeEndDate(String upgradeEndDate) {
        this.upgradeEndDate = upgradeEndDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getMaxIndexSize() {
        return maxIndexSize;
    }

    public void setMaxIndexSize(int maxIndexSize) {
        this.maxIndexSize = maxIndexSize;
    }

    public List<String> getAllowedIpList() {
        return allowedIpList;
    }

    public void setAllowedIpList(List<String> allowedIpList) {
        this.allowedIpList = allowedIpList;
    }

    public int getLicenseLevel() {
        return licenseLevel;
    }

    public void setLicenseLevel(int licenseLevel) {
        this.licenseLevel = licenseLevel;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code==null? "" : code.trim();
    }
}
