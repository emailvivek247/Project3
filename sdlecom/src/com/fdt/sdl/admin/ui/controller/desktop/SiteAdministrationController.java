package com.fdt.sdl.admin.ui.controller.desktop;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.DataSource;
import net.javacoding.xsearch.config.ServerConfiguration;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import com.fdt.common.entity.ErrorCode;
import com.fdt.sdl.admin.ui.controller.AbstractBaseSDLController;

@Controller
public class SiteAdministrationController extends AbstractBaseSDLController {
	
	@RequestMapping(value = "/admin/saveInstanceDbConnection.admin", produces="application/json")
	@ResponseBody
	public List<ErrorCode> saveInstanceConnection(HttpServletRequest request,
			@RequestParam (required=false) String driverDirectoryName,
			@RequestParam (required=false) String jdbcDriver,
			@RequestParam (required=false) String jdbcDriverName,
			@RequestParam (required=false) String dbUrl,
			@RequestParam (required=false) String dbUsername,
			@RequestParam (required=false) String dbPassword,
			@RequestParam (required=false) String dbcpValidationQuery,
			@RequestParam (required=false) String noTestDBConf) {
		List<ErrorCode> errors = new LinkedList<ErrorCode>();
		ErrorCode error = new ErrorCode();
		errors.add(error);
		ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
		DataSource dataSource = sc.getServerDataSource();
		if(dataSource == null) {
			dataSource = new DataSource();
		}
		error = this.checkForEmptyString(error, driverDirectoryName, jdbcDriver, jdbcDriverName, dbUrl, dbUsername, dbPassword, 
				dbcpValidationQuery, noTestDBConf);
		if(error.getCode() != null) {
			return errors;
		}
		dataSource.setJdbcdriver(jdbcDriver);
		dataSource.setDbUrl(dbUrl);
		dataSource.setDbUsername(dbUsername);		
		dataSource.setDbPassword(dbPassword);
		dataSource.setDbcpValidationQuery(dbcpValidationQuery);
		dataSource.setName(jdbcDriverName);
		dataSource.setDriverDirectoryName(driverDirectoryName);
		StringBuffer msg = new StringBuffer();
		boolean testConnection = true;
		if (noTestDBConf.equalsIgnoreCase("true")) {
			testConnection = true;
		} else {
			testConnection = dataSource.testConnection(msg);
		}
		if(testConnection) {
			error.setCode("SUCCESS");
			error.setDescription(this.getMessage("desktop.instancedbconnection.success.save"));	
			try {
				ServerConfiguration.getServerConfiguration().setServerDataSource(dataSource);
				ServerConfiguration.getServerConfiguration().save();
			} catch (IOException e) {
				error.setCode("ERROR");
				error.setDescription("IO Exception");				
			}
		} else {
				error.setCode("ERROR");
				error.setDescription(msg.toString());		
		}
		return errors;
	}
	
	
	@RequestMapping(value = "/admin/testInstanceDbConnection.admin", produces="application/json")
	@ResponseBody
	public List<ErrorCode> testInstanceConnection(HttpServletRequest request,
			@RequestParam (required=false) String driverDirectoryName,
			@RequestParam (required=false) String jdbcDriver,
			@RequestParam (required=false) String jdbcDriverName,
			@RequestParam (required=false) String dbUrl,
			@RequestParam (required=false) String dbUsername,
			@RequestParam (required=false) String dbPassword,
			@RequestParam (required=false) String dbcpValidationQuery,
			@RequestParam (required=false) String noTestDBConf) {
		List<ErrorCode> errors = new LinkedList<ErrorCode>();
		ErrorCode error = new ErrorCode();
		errors.add(error);
		ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
		DataSource dataSource = sc.getServerDataSource();
		if(dataSource == null) {
			dataSource = new DataSource();
		}
		error = this.checkForEmptyString(error, driverDirectoryName, jdbcDriver, jdbcDriverName, dbUrl, dbUsername, dbPassword, 
				dbcpValidationQuery, noTestDBConf);
		if(error.getCode() != null) {
			return errors;
		}
		dataSource.setJdbcdriver(jdbcDriver);
		dataSource.setDbUrl(dbUrl);
		dataSource.setDbUsername(dbUsername);
		dataSource.setDbPassword(dbPassword);		
		dataSource.setDbcpValidationQuery(dbcpValidationQuery);
		dataSource.setName(jdbcDriverName);
		dataSource.setDriverDirectoryName(driverDirectoryName);
		StringBuffer msg = new StringBuffer();
		boolean testConnection = dataSource.testConnection(msg);
		if(testConnection) {
			error.setCode("SUCCESS");
			error.setDescription(this.getMessage("desktop.instancedbconnection.success.test"));			
		} else {
			error.setCode("ERROR");
			error.setDescription(msg.toString());		
		}
		return errors;
	}
	
	
	private ErrorCode checkForEmptyString(ErrorCode error, String driverDirectoryName, String jdbcDriver, 
			String jdbcDriverName, String dbUrl, String dbUsername, String dbPassword, String dbcpValidationQuery, 
			String noTestDBConf) {
		StringBuilder description = new StringBuilder();
		if(StringUtils.isBlank(driverDirectoryName)){
			description.append(this.getMessage("desktop.instancedbconnection.error.driverDirectory"));			
		}
		if(StringUtils.isBlank(jdbcDriver)){
			description.append(this.getMessage("desktop.instancedbconnection.error.jdbcDriver"));
		}
		if(StringUtils.isBlank(jdbcDriverName)){
			description.append(this.getMessage("desktop.instancedbconnection.error.jdbcDriverName"));
		}
		if(StringUtils.isBlank(dbUrl)){
			description.append(this.getMessage("desktop.instancedbconnection.error.dbUrl"));
		}
		if(StringUtils.isBlank(dbcpValidationQuery)){
			description.append(this.getMessage("desktop.instancedbconnection.error.dbcpValidationQuery"));
		}
		if(StringUtils.isBlank(noTestDBConf)){
			description.append(this.getMessage("desktop.instancedbconnection.error.noTest"));
		}
		if (!StringUtils.isBlank(description.toString())) {
			error.setCode("ERROR");
			error.setDescription(description.toString());
		}
		return error;
	}
}