package com.fdt.common.util.cxf;

import java.io.File;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.FileUtil;

import org.apache.ws.security.WSPasswordCallback;

import com.fdt.alerts.util.AlertUtil;
import com.fdt.sdl.styledesigner.util.PageStyleUtil;

public class ClientPasswordCallback implements CallbackHandler {

	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
        String password = null;
        String algorithm = "PBEWITHSHA256AND256BITAES-CBC-BC";
        
        if (this.password != null) {
        	password = this.password;
        } else {
        	File propertiesFile = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "conf", "spring", 
        			"properties", "client.properties");
        	String fileName = propertiesFile.getAbsolutePath();
    		try {
    			password = AlertUtil.readProperty("webservice.password", fileName);
    			System.out.println("password From Properties file: " + password);
    			password = password.replace("ENC(", "");
    			password = password.replace(")", "");
    			System.out.println("Encrypted text: " + password);
    			password = PageStyleUtil.decrypt(password, algorithm);
    			
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        }
        pc.setPassword(password);
	}

}
