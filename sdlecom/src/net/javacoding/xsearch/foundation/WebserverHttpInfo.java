package net.javacoding.xsearch.foundation;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.XMLSerializable;
import net.javacoding.xsearch.utility.U;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("http-info")
public class WebserverHttpInfo extends XMLSerializable{
	
	@XStreamAlias("local-url")
	public String localUrl;
	
	@XStreamAlias("server-url")
	public String serverUrl;
	
	@XStreamAlias("basic-authentication-username")
	public String basicAuthenticationUsername;
	
	@XStreamAlias("basic-authentication-password")
	public String basicAuthenticationPassword;
		
	public WebserverHttpInfo() {
		super();
	}
	
	public WebserverHttpInfo(HttpServletRequest request){
        if(request==null) return;
        String requestURI = request.getRequestURI();
        int theEnd = 0;
        if(requestURI!=null){
            theEnd = request.getRequestURI().lastIndexOf("/");
        }
        serverUrl = request.getScheme() +"://" + request.getServerName()
                +":"+request.getServerPort()
                +requestURI.substring(0,theEnd+1);
        if("https".equals(request.getScheme())){
	        localUrl = "http://127.0.0.1:80"+requestURI.substring(0, theEnd+1);
        } else {
	        localUrl = request.getScheme()
	                +"://" + "127.0.0.1"
	                +":"+request.getServerPort()
	                +requestURI.substring(0,theEnd+1);
        }
	}
	public String getServerName(){
    	String url = serverUrl;
    	int start, stop;
    	if(url==null) return null;
    	if((start=url.indexOf("://"))<0) return null;
    	if((stop=url.indexOf(":", start+3))<0) return null;
    	return url.substring(start+3,stop);
	}
    public String getServerIP() throws UnknownHostException{
    	return addressToString(InetAddress.getByName(getServerName()));
    }
    public int getServerPort(){
        String url = serverUrl;
        int start, stop;
        if(url==null) return -1;
        if((start=url.indexOf("://"))<0) return -1;
        if((start=url.indexOf(":", start+3))<0) return 80;
        if((stop=url.indexOf("/", start+1))<0) return 80;
        return U.getInt(url.substring(start+1,stop),80);
    }
    private static String addressToString(InetAddress addr){
        // Get IP Address
        byte[] ipAddr = addr.getAddress();
        // Convert to dot representation
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }

	public static WebserverHttpInfo load( File theFile ) throws IOException {
    	return (WebserverHttpInfo)fromXML(theFile);
    }
	
}
