package com.fdt.sdl.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.U;

import org.apache.struts.action.ActionServlet;

/**
 * 
 * @
 */
public class SDLActionServlet extends ActionServlet {
    public static String encoding = null;
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        encoding = config.getInitParameter("input.encoding");
        if(U.isEmpty(encoding)) {
            encoding = WebserverStatic.getEncoding();
        }
        return;
    }

    
    protected void process(HttpServletRequest arg0, HttpServletResponse arg1) throws IOException, ServletException {
        if(encoding!=null) {
            String contentType = arg0.getContentType();
            if(contentType==null||!contentType.toLowerCase().startsWith("multipart/")) {
                ((ServletRequest) arg0).setCharacterEncoding(encoding);
            }
        }
        super.process(arg0, arg1);
    }

}
