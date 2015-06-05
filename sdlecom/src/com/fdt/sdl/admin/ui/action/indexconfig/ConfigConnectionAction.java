package com.fdt.sdl.admin.ui.action.indexconfig;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DataSource;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.exception.ConfigurationException;
import net.javacoding.xsearch.foundation.UserPreference;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.util.SecurityUtil;

/**
 * An action that handles the configuration of database connection.
 *
 * <p>The action support an <i>action</i> URL parameter. This URL parameter
 * controls what this action class does. The following values are supported:</p>
 * <ul>
 *   <li>list - list, this is the default if no action parameter is specified
 *   <li>test - test
 *   <li>save - save
 * </ul>
 */
public class ConfigConnectionAction extends Action {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.ConfigConnectionAction");

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request)) return (mapping.findForward("welcome"));
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        HttpSession session = request.getSession();
        String indexName = request.getParameter("indexName");
        String dataSourceName = U.getText(request.getParameter("dataSourceName"), DataSource.DEFAULT_DATASOURCE_NAME);
        session.setAttribute("indexName", indexName);
        String action = request.getParameter("operation");
        DataSource dataSource = null;
        boolean isNewDataSourceSetup = false;
        String saveMode = null;
        try {
            DatasetConfiguration indexConfig = ServerConfiguration.getDatasetConfiguration(indexName);
            session.setAttribute("dc", indexConfig);
            dataSource = indexConfig.getDataSource(dataSourceName);
            String useServerDBConnection = request.getParameter("useServerDBConnection");
            if (useServerDBConnection != null) {
            	indexConfig.setUseServerDBConnection(true);
            	action = "useServerDBConnAction";
            	indexConfig.clearDataSource();
            	indexConfig.save();
            } 
            List<DatasetConfiguration> indexConfigurations = ServerConfiguration.getDatasetConfigurations();
           	request.setAttribute("indexConfigs", indexConfigurations);
           	if (action == null) {
           		if (dataSource == null) {
           			dataSource = ServerConfiguration.getServerConfiguration().getServerDataSource();
           		}
           		request.setAttribute("ds", dataSource);
           		return (mapping.findForward("continue"));
           	}
            if ("test".equals(action) || "save".equals(action)) {
            	if(useServerDBConnection == null) {
            		indexConfig.setUseServerDBConnection(false);
            	}
                if (dataSource == null) {
                    dataSource = new DataSource();
                	isNewDataSourceSetup = true;
                }
                // Cache the changes made by user
                dataSource.setJdbcdriver(request.getParameter("jdbcdriver"));
                dataSource.setDriverDirectoryName(request.getParameter("driverDirectoryName"));
                UserPreference.setString("jdbcdriverName."+indexName, request.getParameter("jdbcdriverName"));
                String dbUrl = request.getParameter("dbUrl");
                dataSource.setDbUrl(dbUrl);                
                dataSource.setDbUsername(request.getParameter("dbUsername"));
                dataSource.setDbPassword(request.getParameter("dbPassword"));
                dataSource.setDbcpValidationQuery(request.getParameter("dbcpValidationQuery"));
    
                if ("test".equals(action)) {
                    StringBuffer sb = new StringBuffer();
                    if (dataSource.testConnection(sb)) {
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("action.configConnection.test.success"));
                        UserPreference.setBoolean("configDataSource."+indexConfig.getName(), true);
                    } else {
                        errors.add("error", new ActionMessage("action.configConnection.test.error", sb));
                        UserPreference.setBoolean("configDataSource."+indexConfig.getName(), false);
                    }
                } else {  // save
                	ActionMessage actionMessage = new ActionMessage("configuration.save.success");
                    StringBuffer sb = new StringBuffer();
                    if ("1".equals(request.getParameter("notest")) || dataSource.testConnection(sb)) {
                    	saveMode = request.getParameter("saveMode");
                    	if ("C".equals(saveMode)) {
                    		if (isNewDataSourceSetup) {
                    			indexConfig.addDataSource(dataSource);
                    		}
                    		indexConfig.save();
                    		messages.add(ActionMessages.GLOBAL_MESSAGE, actionMessage);
                    	} else if ("A".equals(saveMode)) {
                        	this.saveJDBCDetailsForAllIndexes(dataSourceName, request);
                        	messages.add(ActionMessages.GLOBAL_MESSAGE, actionMessage);
                    	} else if ("S".equals(saveMode)) {
                    		String[] indexes = request.getParameterValues("indexes");
                    		if (indexes != null) {
	                    		StringBuilder savedIndexName =  new StringBuilder();
	                        	for(String selectIndexName : indexes) {
	                        		savedIndexName.append(selectIndexName);
	                        		savedIndexName.append(" ");
	                        	}		
	                    		actionMessage = new ActionMessage("jdbcconfiguration.save.success", savedIndexName);
	                    		this.saveJDBCDetailsForSelectedIndex(dataSourceName, request);
	                    		messages.add(ActionMessages.GLOBAL_MESSAGE, actionMessage);	                    		
                    		} else {
                    			errors.add("error", new ActionMessage("jdbcconfiguration.save.error"));
                    		}
                    	}
                        UserPreference.setBoolean("configDataSource."+indexConfig.getName(), true);
                    } else {
                        errors.add("error", new ActionMessage("action.configConnection.test.error", sb));
                        UserPreference.setBoolean("configDataSource."+indexConfig.getName(), false);
                    }
                }
			} else if ("useServerDBConnAction".equals(action)) {
				ActionMessage actionMessage = new ActionMessage("action.configConnection.test.success");
				messages.add(ActionMessages.GLOBAL_MESSAGE,	actionMessage);
				saveMode = request.getParameter("saveMode");
				dataSource = ServerConfiguration.getServerConfiguration().getServerDataSource();
				if ("A".equals(saveMode)) {
					this.saveJDBCDetailsForAllIndexes(dataSourceName, request);
				} else if ("S".equals(saveMode)) {
					String[] indexes = request.getParameterValues("indexes");
					if (indexes != null) {
						StringBuilder savedIndexName = new StringBuilder();
						for (String selectIndexName : indexes) {
							savedIndexName.append(selectIndexName);
							savedIndexName.append(" ");
						}
						actionMessage = new ActionMessage("jdbcconfiguration.save.success", savedIndexName);
						this.saveJDBCDetailsForSelectedIndex(dataSourceName, request);
						messages.add(ActionMessages.GLOBAL_MESSAGE,	actionMessage);
					} else {
						errors.add("error", new ActionMessage(
								"jdbcconfiguration.save.error"));
					}
				}
			}
            request.setAttribute("ds", dataSource);
        } catch (NullPointerException e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
        } catch (ConfigurationException cfExp) {
            errors.add("error", new ActionMessage("configuration.load.error", cfExp));
        } catch (IOException e) {
            errors.add("error", new ActionMessage("configuration.changes.error", e));
        } finally{
            saveErrors(request, errors);
            saveMessages(request, messages);
            UserPreference.save();
            session.setAttribute("jdbcdriverName", UserPreference.getString("jdbcdriverName."+ indexName, ""));
        }
        // Forward to the velocity page
        return (mapping.findForward("continue"));
    }
    
    private void saveJDBCDetailsForAllIndexes(String dataSourceName, HttpServletRequest request) throws ConfigurationException, IOException {
    	List<DatasetConfiguration> indexConfigurations = ServerConfiguration.getDatasetConfigurations();
    	for(DatasetConfiguration indexConfig : indexConfigurations) {
            DataSource dataSource = indexConfig.getDataSource(dataSourceName);
            if(dataSource == null) {
                dataSource = new DataSource();
                indexConfig.addDataSource(dataSource);
            }
            dataSource.setJdbcdriver(request.getParameter("jdbcdriver"));
            dataSource.setDriverDirectoryName(request.getParameter("driverDirectoryName"));
            String dbUrl = request.getParameter("dbUrl");
            dataSource.setDbUrl(dbUrl);                
            dataSource.setDbUsername(request.getParameter("dbUsername"));
            dataSource.setDbPassword(request.getParameter("dbPassword"));
            dataSource.setDbcpValidationQuery(request.getParameter("dbcpValidationQuery"));
            String useServerDBConnection = request.getParameter("useServerDBConnection");
            if(useServerDBConnection != null) {
            	indexConfig.setUseServerDBConnection(true);
            } else {
            	indexConfig.setUseServerDBConnection(false);
            }
            indexConfig.save();
    	}
    }
    
    private void saveJDBCDetailsForSelectedIndex(String dataSourceName, HttpServletRequest request) throws ConfigurationException, IOException {
    	String[] indexes = request.getParameterValues("indexes");
    	for(String indexName : indexes) {
    		DatasetConfiguration indexConfig =  ServerConfiguration.getDatasetConfiguration(indexName);
            DataSource dataSource = indexConfig.getDataSource(dataSourceName);
            if(dataSource == null) {
                dataSource = new DataSource();
                indexConfig.addDataSource(dataSource);
            }
            dataSource.setJdbcdriver(request.getParameter("jdbcdriver"));
            dataSource.setDriverDirectoryName(request.getParameter("driverDirectoryName"));
            String dbUrl = request.getParameter("dbUrl");
            dataSource.setDbUrl(dbUrl);                
            dataSource.setDbUsername(request.getParameter("dbUsername"));
            dataSource.setDbPassword(request.getParameter("dbPassword"));
            dataSource.setDbcpValidationQuery(request.getParameter("dbcpValidationQuery"));
            String useServerDBConnection = request.getParameter("useServerDBConnection");
            if(useServerDBConnection != null) {
            	indexConfig.setUseServerDBConnection(true);
            } else {
            	indexConfig.setUseServerDBConnection(false);
            }
            indexConfig.save();
    	}
    }

}
