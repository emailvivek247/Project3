package net.javacoding.xsearch.config;

import net.javacoding.xsearch.utility.U;

public class LoggingConfiguration {
    public static final String[] LEVELS = {"ERROR"};
    
    boolean isEnabled;
    String smtpHost = "localhost";
    int    smtpPort = 25;
    String toAddress;
    String fromAddress;
    String threshold;
    /**
     * Returns an XML representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("  <logging-configuration>\n");
        if (isEnabled) {
            sb.append("    <enabled>").append(isEnabled).append("</enabled>\n");
        }
        if (!U.isEmpty(smtpHost)) {
            sb.append("    <smtp-host><![CDATA[").append(smtpHost).append("]]></smtp-host>\n");
        }
        if (smtpPort != 25) {
            sb.append("    <smtp-port>").append(smtpPort).append("</smtp-port>\n");
        }
        if (!U.isEmpty(toAddress)) {
            sb.append("    <to-address><![CDATA[").append(toAddress).append("]]></to-address>\n");
        }
        if (!U.isEmpty(fromAddress)) {
            sb.append("    <from-address><![CDATA[").append(fromAddress).append("]]></from-address>\n");
        }
        if (!U.isEmpty(threshold)&&!"OFF".equals(threshold)) {
            sb.append("    <threshold>").append(threshold).append("</threshold>\n");
        }
        sb.append("  </logging-configuration>\n");

        return sb.toString();
    }
    public boolean getIsEnabled() {
        return isEnabled;
    }
    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    public String getSmtpHost() {
        return smtpHost;
    }
    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }
    public int getSmtpPort() {
        return smtpPort;
    }
    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }
    public String getThreshold() {
        return threshold;
    }
    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }
    public String getToAddress() {
        return toAddress;
    }
    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }
    public String getFromAddress() {
        return fromAddress;
    }
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    
}
