package com.fdt.sdl.admin.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.U;

import org.apache.velocity.tools.view.servlet.VelocityLayoutServlet;

/**
 *  @
 */
public class VelocityLayoutEncodingServlet extends VelocityLayoutServlet {
    private static String encoding = null;
    private static String contentType = "text/html; charset=";
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        encoding = config.getInitParameter("output.encoding");
        if(!U.isEmpty(encoding)) {
            contentType += encoding;
        }else {
            contentType += WebserverStatic.getEncoding();
        }
        return;
    }
    protected void setContentType(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(contentType);
    }
}
