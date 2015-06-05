package com.fdt.sdl.admin.servlet;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.HttpUtil;
import net.javacoding.xsearch.utility.U;
import net.javacoding.xsearch.utility.VMTool;

import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;

import com.fdt.sdl.styledesigner.util.PageStyleUtil;
import com.fdt.sdl.styledesigner.util.TemplateUtil;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.ResourceBundleModel;
import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateScalarModel;
import freemarker.template.utility.ObjectConstructor;

public class SDLFreemarkerServlet extends FreemarkerServlet {

	private static final long serialVersionUID = -544987217172411921L;

	protected Configuration createConfiguration() {
        BeansWrapper x = DefaultObjectWrapper.getDefaultInstance();
        x.setNullModel(TemplateScalarModel.NOTHING);
        x.setStrict(false); // false is freemarker default
        x.setExposeFields(true);
        // Below Line is commented to make the Regular JSON to work. This will make the instace timer screen unrendered.
        //x.setSimpleMapWrapper(true);
		Configuration c = super.createConfiguration();
		c.addAutoInclude("/WEB-INF/view/admin/lib/initialize.stl");
		/** SET SL4j as the Logger for Freemarker **/
		try {
			freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_SLF4J);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return c;
	}

	protected ObjectWrapper createObjectWrapper() {
		ObjectWrapper wrapper = super.createObjectWrapper();
		if(wrapper == ObjectWrapper.BEANS_WRAPPER){
			BeansWrapper bw = (BeansWrapper) wrapper;
			bw.setExposureLevel(BeansWrapper.EXPOSE_ALL);
		}
		return wrapper;
	}

	static U u = new U();
	static WebserverStatic ws = new WebserverStatic();
	static TemplateUtil templateUtil = new TemplateUtil();
	static HttpUtil httpUtil = new HttpUtil();
	static PageStyleUtil pageStyleUtil = new PageStyleUtil();
	static VMTool vmTool = new VMTool();
	static ResourceBundleModel rsbm = new ResourceBundleModel(ResourceBundle.getBundle("ApplicationResources"),new BeansWrapper());
	static ObjectConstructor constructor = new ObjectConstructor();
	static NumberTool number = new NumberTool();
	static DateTool date = new DateTool();
	protected boolean preTemplateProcess(HttpServletRequest request, HttpServletResponse response, Template template, TemplateModel data) throws ServletException, IOException {
		((SimpleHash) data).put("U", u);
		((SimpleHash) data).put("request", request);
		((SimpleHash) data).put("response", response);
		((SimpleHash) data).put("WebserverStatic", ws);
		((SimpleHash) data).put("templateUtil", templateUtil);
		((SimpleHash) data).put("httpUtil", httpUtil);
		((SimpleHash) data).put("pageStyleUtil", pageStyleUtil);
		((SimpleHash) data).put("vmTool", vmTool);
		((SimpleHash) data).put("bundle", rsbm);
		((SimpleHash) data).put("new", constructor);
        ((SimpleHash) data).put("number", number);
        ((SimpleHash) data).put("date", date);
        return true;
	}

}
