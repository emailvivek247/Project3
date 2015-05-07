/*
 * Copyright 2004.
 */

package net.javacoding.xsearch.config;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.javacoding.xsearch.core.exception.ConfigurationException;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.U;

import org.apache.commons.lang.StringUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.license.License;
import com.fdt.sdl.styledesigner.util.PageStyleUtil;

/**
 * Class mapped to the <code>&lt;xsearch&gt;</code> root element of the server configuration file.
 * 
 */
public final class ServerConfiguration extends Configuration {

    // ----------------------------------------------------- Instance Variables

    private static Logger logger = LoggerFactory.getLogger(ServerConfiguration.class);

    /** The absolute pathname for the base directory. */
    private File baseDirectory = null;

    /** The singleton instance. */
    private static ServerConfiguration serverConfig = new ServerConfiguration();

    /** DatasetConfiguration objects pool. */
    private static Map<String, DatasetConfiguration> datasetConfigs = new HashMap<String, DatasetConfiguration>();
    
    /**Stores the Instance Index Root Directory **/
    private String indexRootDirectory = null;
    
    /**Stores the DataSource at the Server Instance Level **/
    private static DataSource serverDataSource = null;
    
    private static Map<String, InstanceJobSchedule> instanceJobSchedules = new HashMap<String, InstanceJobSchedule>();
    
    private transient final static int serverUniqieId = new Random().nextInt();
    
   	public static int getServerUniqueId() {
        return serverUniqieId;
    }
   	  	
    public ServerConfiguration() {
    }

    public static Map<String, InstanceJobSchedule> getInstanceJobSchedules() {
		return instanceJobSchedules;
	}

	public static void setInstanceJobSchedules(Map<String, InstanceJobSchedule> schedules) {
		instanceJobSchedules = schedules;
	}
	
	public static void setInstanceJobSchedule(InstanceJobSchedule instanceJobSchedule) {
		if(instanceJobSchedules!=null) {
			instanceJobSchedules.put(instanceJobSchedule.getScheduleName(), instanceJobSchedule);
		}
	}
	
	public static void removeInstanceJobSchedule(InstanceJobSchedule instanceJobSchedule) {
		if(instanceJobSchedules != null) {
			instanceJobSchedules.remove(instanceJobSchedule.getScheduleName());
		}
	}

	/** The base directory for dataset indices and configuration files. */
    private String basedir = ".";

    /**
     * @return interned base dir
     */
    public String getBasedir() {
        return basedir;
    }

    /**
     * Sets the base directory.
     * 
     * @param basedir the base directory
     * @throws java.lang.NullPointerException if configFile is null.
     */
    public void setBasedir(String basedir) {
        this.basedir = (basedir==null?null:basedir.intern());
        isDirty = true;

        // If basedir is relative, then it is resolved against the directory
        // for the server configuration file
        baseDirectory = FileUtil.resolveFile(configFile.getParentFile(), basedir);
    }
    
    private String adminUser;

    public String getAdminUser() {
        return PageStyleUtil.decrypt(adminUser);
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
        isDirty = true;
    }

    private String password;

    public String getPassword() {
        return PageStyleUtil.decrypt(password);
    }

    public void setPassword(String password) {
        this.password = password;
        isDirty = true;
    }

    private boolean isMergingOldDatasetValues = true;
    public void setIsMergingOldDatasetValues(boolean isMergingOldDatasetValues) {
        this.isMergingOldDatasetValues = isMergingOldDatasetValues;
        isDirty = true;
    }
    public boolean getIsMergingOldDatasetValues() {
        return isMergingOldDatasetValues;
    }

    private int searchLogSize = 1;
    public void setSearchLogSizeInMB(int logSize) {
        this.searchLogSize = logSize;
        isDirty = true;
    }
    public int getSearchLogSizeInMB() {
        return searchLogSize;
    }

    private int indexingLogSize = 3;
    public void setIndexingLogSizeInMB(int logSize) {
        this.indexingLogSize = logSize;
        isDirty = true;
    }
    public int getIndexingLogSizeInMB() {
        return indexingLogSize;
    }
    private boolean isShortIndexingLogEnabled = true;
    public void setIsShortIndexingLogEnabled(boolean isShortIndexingLogEnabled) {
        this.isShortIndexingLogEnabled = isShortIndexingLogEnabled;
        isDirty = true;
    }
    public boolean getIsShortIndexingLogEnabled() {
        return isShortIndexingLogEnabled;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Initializes this object by parsing the server configuration file.
     * 
     * @throws ConfigurationException
     */
    public void init() throws ConfigurationException {
        // server configFile is set at the start of the servlet container.
        if (configFile == null) {
            String path = System.getProperty("CONFIG_FILE");
            if (path != null) {
                configFile = new File(path);
            } else {
                configFile = new File("data/xsearch-config.xml");
            }
        }

        dcs=null;
        datasetConfigs.clear();

        super.init();
    }

    public void setConfigFile(File configFile) {
        super.setConfigFile(configFile);
        try {
            init();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        baseDirectory = FileUtil.resolveFile(configFile.getParentFile(), basedir);
    }

    /**
     * Sets the path of the server configuration file.
     * 
     * @path the path of the server configuration file
     */
    public static void setServerConfigFile(String path) {
        if (path == null) {
            logger.error("Server configuration file is null");
            return;
        }
        serverConfig.setConfigFile(new File(path));
    }

    /**
     * Returns the server configuration.
     * 
     * @throws ConfigurationException
     */
    public static ServerConfiguration getServerConfiguration() {
        return serverConfig;
    }
    
    /**
     * A static wrapper of <code>getDatasetConfig</code>.
     */
    public static DatasetConfiguration getDatasetConfiguration(String name){
        if(name == null ) return null;
        name = name.toLowerCase().intern();
        if(datasetConfigs.size()==0) {
            ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
            sc.syncDatasets();
        }
        DatasetConfiguration dc = (DatasetConfiguration) datasetConfigs.get(name);
        return dc;
    }

    private static ArrayList<DatasetConfiguration> dcs = null;
    /**
     * Gets the list of dataset configurations.
     * 
     * @return the list of dataset configurations
     * @throws ConfigurationException
     */
    public static ArrayList<DatasetConfiguration> getDatasetConfigurations(boolean refresh, final boolean isSortbyName) throws ConfigurationException {
        ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
        if(refresh) {
            sc.syncDatasets();
        }
        if(dcs!=null) return dcs;
        dcs = new ArrayList<DatasetConfiguration>();
        Iterator<String> it = datasetConfigs.keySet().iterator();
        while(it.hasNext()){
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(it.next());
            if (dc != null) dcs.add(dc);
        }
        DatasetConfiguration[] cs = (DatasetConfiguration[]) dcs.toArray(new DatasetConfiguration[dcs.size()]);
        Arrays.sort(cs, new Comparator<DatasetConfiguration>() {         // sort the array
            public int compare(DatasetConfiguration o1, DatasetConfiguration o2) {
            	if (isSortbyName) {
            		return o1.getName().compareTo(o2.getName());
            	} else {
            		return o1.getDisplayOrder() - o2.getDisplayOrder();
            	}
            }
          });
        dcs.clear();
        for (DatasetConfiguration dc : cs) {
            dcs.add(dc);                  // re-build list, now sorted
        }
        return dcs;
    }
    public static ArrayList<DatasetConfiguration> getDatasetConfigurations() throws ConfigurationException {
        return getDatasetConfigurations(true, false);
    }

    public static ArrayList<DatasetConfiguration> getDatasetConfigurations(boolean isSortbyName) throws ConfigurationException {
        return getDatasetConfigurations(true, isSortbyName);
    }
    
    public static DatasetConfiguration[] getDatasetConfigurations(String[] indexNames) throws ConfigurationException {
        if(indexNames==null||indexNames.length<=0){
            ArrayList<DatasetConfiguration> al = getDatasetConfigurations();
            for(int i=0;i<al.size();) {
                DatasetConfiguration dc = (DatasetConfiguration)al.get(i);
                if(dc.getDisplayOrder()>=0) {
                    i++;
                }else {
                    al.remove(i);
                }
            }
            return (DatasetConfiguration[]) al.toArray(new DatasetConfiguration[al.size()]);
        }
        ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
        sc.syncDatasets();
        ArrayList<DatasetConfiguration> dcs = new ArrayList<DatasetConfiguration>();
        for (int i = 0; i<indexNames.length; i++) {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexNames[i]);
            if (dc != null) dcs.add(dc);
        }
        DatasetConfiguration[] cs = (DatasetConfiguration[]) dcs.toArray(new DatasetConfiguration[dcs.size()]);
        Arrays.sort(cs, new Comparator<DatasetConfiguration>() {         // sort the array
            public int compare(DatasetConfiguration o1, DatasetConfiguration o2) {
              return o1.getDisplayOrder() - o2.getDisplayOrder();
            }
          });
        return cs;
    }
    protected static void resetDatasetConfigurationsOrder() {
        dcs = null;
    }

    /**
     * Returns the absolute pathname for the base directory.
     */
    public File getBaseDirectory() {
        return baseDirectory;
    }

    /**
     * Deletes the dataset configuration file entry.
     * 
     * @param name the dataset name
     */
    public void deleteDatasetConfiguratoinFile(String name) {
        datasetConfigs.remove(name);
        isDirty = true;
    }

    /**
     * Returns <code>true</code> if the dataset for the given name exists; <code>false</code> otherwise.
     * 
     * @param name the dataset name
     */
    public boolean existsDataset(String name) {
        if (name == null) { return false; }
        return getDatasetConfigurationFile(name).exists();
    }
    
    public File getDatasetConfigurationFile(String name) {
        return new File(baseDirectory, name.toLowerCase()+"-dataset-config.xml");
    }

    /**
     * Deletes the dataset for the given name.
     * 
     * @param name the dataset name
     */
    public void deleteDataset(String name) {
        if (name == null) { return; }

        // Delete dataset configuration files
        File f = getDatasetConfigurationFile(name);
        if (!f.delete()) {
            logger.error("couldn't delete " + f);
        }

        // Remove the entry from ServerConfiguration
        deleteDatasetConfiguratoinFile(name);

    }

    /**
     * Synchronizes dataset configuration file entries in the server configuration file with the file system.
     */
    public void syncDatasets(boolean forceRefresh) {
        // scan the base directory for new dataset configuration files
        File[] _files = getBaseDirectory().listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("-dataset-config.xml") && !pathname.isDirectory();
            }
        });
        if (_files != null) {
            for (int i = 0; i < _files.length; i++) {
                String fileName = _files[i].getName();
                addDatasetConfigurationFile(fileName.substring(0,fileName.length()-19), forceRefresh);
            }
        }
        dcs = null;
    }
    public void syncDatasets() {
        syncDatasets(false);
    }

    /**
     * @param fileName
     */
    public void addDatasetConfigurationFile(String name, boolean forceRefresh) {
        if(name==null) return;
        name = name.toLowerCase().intern();
        DatasetConfiguration old = (DatasetConfiguration) datasetConfigs.get(name);
        File f = getDatasetConfigurationFile(name);
        if(old==null || old.lastModified()<f.lastModified() || forceRefresh){
        	datasetConfigs.put(name, new DatasetConfiguration(name, getDatasetConfigurationFile(name), getBaseDirectory()));
//        	try {
//				datasetConfigs.put(name, (DatasetConfiguration)XMLSerializable.fromXML(new FileInputStream(getDatasetConfigurationFile(name)), DatasetConfiguration.class));
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
        }
    }

    public int validateIndexName(String name) {
        if (name == null || "".equals(name)) {
            return ERROR_INDEXNAME_REQUIRED;
        }
        if (!name.matches("[\\w]+")) {
            return ERROR_INDEXNAME_PATTERN;
        }
        if (name.length() > 30) {
            return ERROR_INDEXNAME_LENGTH;
        }
        if (existsDataset(name)) {
            return ERROR_INDEXNAME_DUPLICATE;
        }

        return 0;
    }

    RegistrationInformation registrationInformation = null;

    public RegistrationInformation getRegistrationInformation() {
        return registrationInformation;
    }

    public void setRegistrationInformation(RegistrationInformation registrationInformation) {
        this.registrationInformation = registrationInformation;
        if(!registrationInformation.isValidFormat()) {
            registrationInformation.setLicenseLevel(0);
        }
    }

    /**
     * Returns an XML representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(XML_DECLARATION).append("\n");

        sb.append("<xsearch");
        if (basedir != null) {
            sb.append(" basedir=\"").append(basedir).append("\"");
        }
        sb.append(" password=\"").append(password).append("\"");
        
        sb.append(" adminuser=\"").append(adminUser).append("\"");
        
        sb.append(">\n");

        if(isMergingOldDatasetValues==false) {
            sb.append("  <is-merging-old-dataset-values>").append(this.isMergingOldDatasetValues).append("</is-merging-old-dataset-values>\n");
        }
        if(searchLogSize!=1){
            sb.append("  <search-log-size-in-MB>").append(getSearchLogSizeInMB()).append("</search-log-size-in-MB>\n");
        }
        if(indexingLogSize!=3){
            sb.append("  <indexing-log-size-in-MB>").append(getIndexingLogSizeInMB()).append("</indexing-log-size-in-MB>\n");
        }
        if(isShortIndexingLogEnabled==false) {
            sb.append("  <is-short-indexing-log-enabled>").append(this.isShortIndexingLogEnabled).append("</is-short-indexing-log-enabled>\n");
        }
        if(indexRootDirectory != null && indexRootDirectory.length() > 0) {
            sb.append("  <index-root-directory><![CDATA[").append(this.indexRootDirectory.trim()).append("]]></index-root-directory>\n");
        }
        
        if(registrationInformation!=null) {
            sb.append(registrationInformation);
        }
        
        if (serverDataSource != null) {
        	sb.append(serverDataSource.toString());
        }
        
        sb.append("  <instance-job-schedules>\n");
        for (Map.Entry<String, InstanceJobSchedule> entry : instanceJobSchedules.entrySet()) {
            sb.append(entry.getValue());
        }
        sb.append("  </instance-job-schedules>\n");
        
        if(logging!=null) {
            sb.append(logging);
        }
        sb.append("</xsearch>");

        return sb.toString();
    }
    
    public void setLicense(License license) {
        if(registrationInformation==null)registrationInformation=new RegistrationInformation(); 
        registrationInformation.setLicense(license);
    }
    public License getLicense() {
        if(registrationInformation==null)registrationInformation=new RegistrationInformation(); 
        return registrationInformation.getLicense();
    }
    public boolean canUpgradeLicense() {
        if(registrationInformation==null)registrationInformation=new RegistrationInformation(); 
        return registrationInformation.canLicenseAppliedToThisRelease();
    }

    public int getLicenseVersion() {
        if(registrationInformation==null)return 1;
        return registrationInformation.getVersion();
    }
    public String getUser() {
        if(registrationInformation==null)return "Trial User";
        return registrationInformation.getUser();
    }
    public int getAllowedLicenseLevel() {
    	return 2;
    	/*
        if(registrationInformation==null)return 0;
        return registrationInformation.getAllowedLicenseLevel();
        */
    }
    public int getAllowedMaxIndexSize() {
        if(registrationInformation==null)return ConfigConstants.FREE_SIZE_LIMIT;
        return registrationInformation.getAllowedMaxIndexSize();
    }
    public int getLicenseLevel() {
        if(registrationInformation==null)return 0;
        return registrationInformation.getLicenseLevel();
    }
    public boolean isRegistrationValidFormat() {
        if(registrationInformation==null)return false;
        return registrationInformation.isValidFormat();
    }
    public String getRegistrationNumber() {
        if(registrationInformation==null)return "000";
        return registrationInformation.getRegistrationNumber();
    }
    public String getAllowedIpsString() {
        if(registrationInformation==null)return ""; 
        return U.join(registrationInformation.getAllowedIpList(),",");
    }
    public String getEndDate() {
        if(registrationInformation==null)return "2005-08-01";
        return registrationInformation.getEndDate();
    }

    public String getRegistrationCode() {
        if(registrationInformation==null)return null;
        return registrationInformation.getRegistrationCode();
    }

    public int getMaxIndexSize() {
        if(registrationInformation==null)return FREE_SIZE_LIMIT;
        return registrationInformation.getMaxIndexSize();
    }

    // the following are for reading by ruleset.java
    public void setMaxIndexSize(int indexSize) {
        if(registrationInformation==null)registrationInformation=new RegistrationInformation(); 
        registrationInformation.setMaxIndexSize(indexSize);
    }
    public void setRegistrationCode(String registrationCode) {
        if(registrationInformation==null)registrationInformation=new RegistrationInformation(); 
        registrationInformation.setRegistrationCode(registrationCode);
    }
    public void setRegistrationNumber(String registrationNumber) {
        if(registrationInformation==null)registrationInformation=new RegistrationInformation(); 
        registrationInformation.setRegistrationNumber(registrationNumber);
    }
    public void setAllowedIpsString(String allowedIpsString) {
        if(registrationInformation==null)registrationInformation=new RegistrationInformation(); 
        registrationInformation.setAllowedIpsString(allowedIpsString);
    }
    public void setUser(String user) {
        if(registrationInformation==null)registrationInformation=new RegistrationInformation(); 
        registrationInformation.setUser(user);
    }
    public void setLicenseVersion(int version) {
        if(registrationInformation==null)registrationInformation=new RegistrationInformation(); 
        registrationInformation.setVersion(version);
    }
    public void setLicenseLevel(int licenseLevel) {
        if(registrationInformation==null)registrationInformation=new RegistrationInformation(); 
        registrationInformation.setLicenseLevel(licenseLevel);
    }
    public void setStartDate(String startDate) {
        if(registrationInformation==null)registrationInformation=new RegistrationInformation(); 
        registrationInformation.setStartDate(startDate);
    }
    public void setUpgradeEndDate(String upgradeEndDate) {
        if(registrationInformation==null)registrationInformation=new RegistrationInformation(); 
        registrationInformation.setUpgradeEndDate(upgradeEndDate);
    }
    public void setEndDate(String endDate) {
        if(registrationInformation==null)registrationInformation=new RegistrationInformation(); 
        registrationInformation.setEndDate(endDate);
    }

    public void resetLicense() {
        if(registrationInformation!=null) {
            registrationInformation.resetLicenseCache();
        }
    }

    LoggingConfiguration logging;
    public void setLoggingConfiguration(LoggingConfiguration logging) {
        this.logging = logging;
        this.isDirty = true;
    }
    public LoggingConfiguration getLoggingConfiguration() {
        return this.logging==null? new LoggingConfiguration() : this.logging;
    }

	public String getIndexRootDirectory() {
		return indexRootDirectory;
	}

	public void setIndexRootDirectory(String indexRootDirectory) {
		this.indexRootDirectory = indexRootDirectory;
	}

	public DataSource getServerDataSource() {
		return serverDataSource;
	}

	public void setServerDataSource(DataSource dataSource) {
		serverDataSource = dataSource;
	}

	public void deleteSchedulesAssociatedWithIndex(String indexName) throws ConfigurationException, SchedulerException {
		List<DatasetConfiguration> datasetConfigurations = ServerConfiguration.getDatasetConfigurations();
		Scheduler scheduler = WebserverStatic.getScheduler();
		if(scheduler == null) return;
		if(datasetConfigurations !=null && datasetConfigurations.size() > 0 ) {
			for(DatasetConfiguration datasetConfiguration: datasetConfigurations) {
				if(indexName.equalsIgnoreCase(datasetConfiguration.getName())) {
					List<Schedule> schedules =  datasetConfiguration.getSchedules();
					if(schedules !=null && schedules.size() > 0 ) {
						for(Schedule schedule: schedules) {
							String jobName = schedule.getIndexingMode();
						    String groupName = datasetConfiguration.getName();
						    scheduler.deleteJob(new JobKey(jobName, groupName));
				        }
					}
					break;
				}
			}
		}		
	}

	public void updateInstanceTimerJobs(String indexName) throws SchedulerException, IOException {
		Map<String, InstanceJobSchedule> instanceJobSchedules = ServerConfiguration.getInstanceJobSchedules();
		InstanceJobSchedule instanceJobSchedule = null;
		String jobDataString = null;
		String subString = null;
		int endIndex;
		String groupName = "Instance Job";
		ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
		Scheduler scheduler = WebserverStatic.getScheduler();
		for(Map.Entry<String, InstanceJobSchedule>  entry : instanceJobSchedules.entrySet()) {
			instanceJobSchedule = entry.getValue();
			jobDataString = instanceJobSchedule.getJobDataString();
			int index = jobDataString.indexOf(indexName);
			if(index > -1) {
				endIndex = jobDataString.indexOf("|", index);
				subString = jobDataString.substring(index, endIndex + 1);
				jobDataString = jobDataString.replace(subString, "");
				if(StringUtils.countMatches(jobDataString, ",") >= 1) {
					instanceJobSchedule.setJobDataString(jobDataString);
					ServerConfiguration.setInstanceJobSchedule(instanceJobSchedule);						
				} else {
					// Remove the Instance Job Schedule itself..
					scheduler.deleteJob(new JobKey(instanceJobSchedule.getScheduleName(), groupName));
					ServerConfiguration.removeInstanceJobSchedule(instanceJobSchedule);				
				}
				sc.save();
			}
		}
	}
}
