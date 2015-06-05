package net.javacoding.xsearch.utility;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketOptions;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Properties;

import net.javacoding.xsearch.foundation.WebserverHttpInfo;

import com.thoughtworks.xstream.core.util.Base64Encoder;

public class HttpPost {
    ArrayList<String> names  = new ArrayList<String>();
    ArrayList<String> values = new ArrayList<String>();
    String m_url  = null;
    
    String basicAuthenticationUsername = null;
    String basicAuthenticationPassword = null;

    /**
     * @param url
     *            the url to send http request to
     */
    public HttpPost(String url) {
        m_url = url;
    }

    private static boolean initialized = false;

    public static void init(String proxyHost, String proxyPort) {
        if (initialized) return;
        Properties prop = System.getProperties();
        if (proxyHost != null) prop.put("http.proxyHost", proxyHost);
        if (proxyPort != null) prop.put("http.proxyPort", proxyPort);
        initialized = true;
    }

    public void add(String name, String value) {
        if (name == null) return;
        if (value == null) return;
        names.add(name);
        values.add(value);
    }

    public void setURL(String theUrl) {
        m_url = theUrl;
    }

    public void setHttpInfo(WebserverHttpInfo whi) {
        m_url = whi.localUrl;
        basicAuthenticationUsername = whi.basicAuthenticationUsername;
        basicAuthenticationPassword = whi.basicAuthenticationPassword;
    }

    public String getURL() {
        return m_url;
    }

    public String send() {
        StringBuffer responseBody = new StringBuffer();
        try {
            URL aUrl = new URL(getURL());
            URLConnection conn = aUrl.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if(basicAuthenticationUsername!=null){
            	conn.setRequestProperty ("Authorization", "Basic " + new Base64Encoder().encode((basicAuthenticationUsername+":"+basicAuthenticationPassword).getBytes()));
            }
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            for (int i = 0; i < names.size(); i++) {
                if (i > 0) out.writeBytes("&");
                out.writeBytes((String) names.get(i));
                out.writeBytes("=");
                out.writeBytes(URLEncoder.encode((String) values.get(i), "UTF-8"));
            }
            out.flush();
            out.close();

            // ------------------------ Response Header
            // -------------------------------
            // log("getting response header");
            for (int i = 0; conn.getHeaderFieldKey(i) != null || conn.getHeaderField(i) != null; i++) {
                // log("["+i+"] "+conn.getHeaderFieldKey(i)+":
                // "+conn.getHeaderField(i));
            }
            // ------------------------ Response Body (line by line)
            // ------------------
            String inputLine;

            // log("getting response body");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()), SocketOptions.SO_RCVBUF);
            while ((inputLine = in.readLine()) != null) {
                responseBody.append(inputLine);
            }
            in.close();
            // ------------------------------------------------------------------------
        } catch (Exception e) {
            // log("Error in send with error:" + e.toString());
            return "Error in Sending to " + getURL() + "\n" + e;
        }
        return responseBody.toString();
    }

    public String toHTML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<html>\n<head>\n");
        sb.append("<SCRIPT LANGUAGE=\"JavaScript\">\n");
        sb.append("function init(){\n");
        sb.append("     document.sendForm.submit();\n");
        sb.append("}\n");
        sb.append("</SCRIPT>\n");
        sb.append("</head>\n");
        sb.append("<BODY onLoad='init()'>Loading...\n");
        sb.append("<FORM method=\"post\" name=\"sendForm\" action=\"");
        sb.append(getURL());
        sb.append("\">\n");
        for (int i = 0; i < names.size(); i++) {
            sb.append("<input type=hidden name=");
            sb.append((String) names.get(i));
            sb.append(" value=");
            try {
                sb.append(URLEncoder.encode((String) values.get(i), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            sb.append(">\n");
        }
        sb.append("</FORM>\n");
        sb.append("</BODY>\n");
        sb.append("</HTML>\n");
        return sb.toString();
    }

    private static void main(String[] args) {
        HttpPost hp = new HttpPost("http://localhost");
        hp.add("COLLATERAL_ID", "11179");
        hp.add("ACTION", "ADD_COLLATERAL");
        System.out.println(hp.send());
    }
}
