package net.javacoding.xsearch.foundation;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.config.WebConfiguration;
import net.javacoding.xsearch.config.XMLSerializable;
import net.javacoding.xsearch.config.facet.FacetTypes;
import net.javacoding.xsearch.utility.FileUtil;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

import com.fdt.sdl.core.ui.action.indexing.status.SDLJobListener;
import com.fdt.sdl.styledesigner.util.PageStyleUtil;


public class WebserverStatic {
    public WebserverStatic(){}

    public static String sep = System.getProperty("file.separator");
    public static String getSeparator(){
    	return sep;
    }
    public static Scheduler scheduler;
    public static boolean isServer;
    public static String rootDirectory = "."+ sep;
    public static WebConfiguration webConfiguration;
    public static transient FacetTypes facetTypes;
    private static final String NOT_YET_STARTED = "NOT YET STARTED";
   
    public static final String INCREMENTAL_INDEXING = "INCREMENTAL INDEXING";
    public static final String INCREMENTAL_INDEXING_WITH_DELETION = "INCREMENTAL INDEXING WITH THOROUGH DELETION DETECTION";
    public static final String RECREATE_INDEX = "RE-CREATE INDEX";
    public static final String FETCH_SUBSCRIBED_INDEX = "FETCH SUBSCRIBED INDEX"; 
    public static final String RE_ANALYZE = "RE-ANALYZE";
    public static final String DELETE_DUPLICATES = "DELETE DUPLICATES";
    public static final String REBUILD_SPELL_CHECK_DICTIONARY = "RE-CREATE SPELLING DICTIONARY";
    public static final String UNLOCK_INDEX = "UNLOCK INDEX";
    public static final String CLEAN_INDEX = "CLEAN INDEX";
    public static final String REFRESH_INDEX = "REFRESH INDEX";
    public static final String MAKE_OFFLINE = "MAKE OFFLINE";
   
    public static WebserverHttpInfo httpInfo = null;
    
    public static Map<String, String> indexingActionMap = new HashMap<String, String>() {{
		 put(INCREMENTAL_INDEXING, "stopIndexing unlockStoppedIndex createPeriodTable maybeBuildSynonyms incrementalIndexingWithFastDeletion mergeIndexesIfNeeded buildDictionaryIfNeeded ping-a-url");
		 put(INCREMENTAL_INDEXING_WITH_DELETION, "stopIndexing unlockStoppedIndex createPeriodTable maybeBuildSynonyms incrementalIndexingWithThoroughDeletion mergeIndexesIfNeeded buildDictionaryIfNeeded ping-a-url");
		 put(RECREATE_INDEX, "stopIndexing unlockStoppedIndex maybeBuildSynonyms reCreateIndex mergeIndexes reBuildDictionary buildDictionaryIfNeeded ping-a-url");  
    	 put(FETCH_SUBSCRIBED_INDEX, "unlockStoppedIndex retrieveSubscription ping-a-url");
    	 put(RE_ANALYZE, "unlockStoppedIndex buildDictionaryIfNeeded maybeBuildSynonyms reIndexing");
    	 put(DELETE_DUPLICATES, "stopIndexing unlockStoppedIndex createPeriodTable maybeBuildSynonyms incrementalIndexingWithFastDeletion mergeIndexesIfNeeded buildDictionaryIfNeeded ping-a-url");
    	 put(REBUILD_SPELL_CHECK_DICTIONARY, "unlockStoppedIndex reBuildDictionary");
    	 put(UNLOCK_INDEX, "unlockStoppedIndex");
    	 put(CLEAN_INDEX, "delete");
    	 put(REFRESH_INDEX, "refreshIndex");
    }};
    
    
    public static Map<String, String> getIndexingActionMap() {
		return indexingActionMap;
	}
	public static void setIndexingActionMap(Map<String, String> indexingActionMapLocal) {
		indexingActionMap = indexingActionMapLocal;
	}
	public static void setScheduler(Scheduler sched){
        scheduler = sched;
    }
    public static Scheduler getScheduler(){
        return scheduler ;
    }

    public static void setIsServer(boolean serverMode) {
        isServer = serverMode;
    }
    public static boolean getIsServer() {
        return isServer;
    }
    public static void setRootDirectory(String root){
        rootDirectory = root;
        if (!rootDirectory.endsWith(sep)){
            rootDirectory = rootDirectory + sep;
        }
    }
    public static String getRootDirectory(){
        return rootDirectory ;
    }
    public static File getRootDirectoryFile(){
        return new File(rootDirectory) ;
    }

    public static void setWebConfiguration(WebConfiguration wc) {
        webConfiguration = wc;
    }
    public static WebConfiguration getWebConfiguration() {
        return webConfiguration;
    }

    /**
    * Execute the java command for the index
    * @param indexName
    * @param commands like startIndexing, fullIndexing, etc
    * @param heapSize 64,256, etc
    */
    public static String getJavaCommand(String indexName, int heapSize){
        return //(isWindows?"":"nice -10 ")
            //+" -cp "+getClassPath()
            //+"-XXPermSize=256m"
            //+" -Xms" + heapSize + "m"
            "-Xmx" + heapSize + "m"
            +" -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
            +" net.javacoding.xsearch.IndexManager"
            +" -config \""+ ServerConfiguration.getServerConfiguration().getConfigFile() +"\""
            +" -index "+indexName+" ";
    }

    private static FileFilter fileFilter=new FileFilter(){
        public boolean accept(File pathname) {
            String tmp=pathname.getName().toLowerCase();
            if(tmp.endsWith(".jar") || tmp.endsWith(".zip")){
                return true;
            }
            return false;
        }
    };
    public static String getClassPath(){
        StringBuffer sb = new StringBuffer();
        sb.append("classes");
        File workingDirectory = new File(getWebInfDirectory());
        appendClassPath(sb,workingDirectory, "lib"+sep+"ext"+sep+"indexing");
        appendClassPath(sb,workingDirectory, "lib");
        sb.append(File.pathSeparatorChar);
        sb.append(System.getProperty("java.class.path"));

        return sb.toString();
    }
    
    private static void appendClassPath(StringBuffer sb, File workingDirectory, String dir) {
        File libDir  = new File(workingDirectory, dir);
        File[] files = libDir.listFiles(fileFilter);
        if(files!=null){
            for(int i=0;i<files.length;i++){
                sb.append(File.pathSeparatorChar);
                sb.append(dir);
                sb.append(sep);
                sb.append(files[i].getName());
            }
        }
    }

    static String m_antCommand = null;

    public static String getJobWorkingDirectory(){
        return rootDirectory+"WEB-INF";
    }
    public static String getWebInfDirectory(){
        return rootDirectory+"WEB-INF";
    }
    public static File getDictionaryDirectoryFile(){
        return new File(ServerConfiguration.getServerConfiguration().getBaseDirectory(),"dictionary");
    }
    public static void setURIFile(HttpServletRequest request){
    	try {
    		File f = getURIFile();
    		if(httpInfo==null&& !f.exists()){
    	    	httpInfo = new WebserverHttpInfo(request);
    			httpInfo.save(f);
    			return;
    		}
    		if(httpInfo==null&&f.exists()){
    			httpInfo.load(f);
    		}
        	WebserverHttpInfo newInfo = new WebserverHttpInfo(request);
        	//change the server uri file in 3 cases
        	//1. uri file doesn't exist
        	//2. server has been accessed only from 127.0.0.1
        	//3. local server changed port or directory
        	if( !f.exists()
        			|| httpInfo == null
        			|| ("127.0.0.1".equals(httpInfo.getServerIP()) && !"127.0.0.1".equals(newInfo.getServerIP()) )
        			|| !newInfo.localUrl.equalsIgnoreCase(httpInfo.localUrl)){
        		httpInfo = newInfo;
        		httpInfo.save(f);
        	}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public static void setURIFile(String localUrl, String serverUrl) {
        File f = getURIFile();
        if (httpInfo == null && !f.exists()) {
            httpInfo = new WebserverHttpInfo(localUrl, serverUrl);
            httpInfo.save(f);
        }
    }

    public static void resetURIFile(HttpServletRequest request){
        try {
            File f = getURIFile();
            httpInfo = new WebserverHttpInfo(request);

            if(!("127.0.0.1".equals(httpInfo.getServerIP()))){
                httpInfo.save(f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getURIFile(){
        Path baseDirPath = ServerConfiguration.getServerConfiguration().getBaseDirectory().toPath();
        Path logPath = baseDirPath.resolve("log");
        try {
            logPath = Files.createDirectories(logPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logPath.resolve("http.xml").toFile();
    }

    public static String getServerURL(){
        try{
        	if(httpInfo==null){
        		httpInfo = WebserverHttpInfo.load(getURIFile());
        	}
            if(httpInfo==null) {
                return "";
            }
            return httpInfo.serverUrl;
        }catch(IOException ioe){}
        return null;
    }
    public static String getLocalURL(){
        try{
        	if(httpInfo==null){
        		httpInfo = WebserverHttpInfo.load(getURIFile());
        	}
            if(httpInfo==null) {
                return "";
            }
            return httpInfo.localUrl;
        }catch(IOException ioe){}
        return null;
    }
    public static String getServerName(){
        try{
        	if(httpInfo==null){
        		httpInfo = WebserverHttpInfo.load(getURIFile());
        	}
        	if(httpInfo==null) {
        	    return "";
        	}
            return httpInfo.getServerName();
        }catch(IOException ioe){}
        return null;
    }
    public static int getServerPort(){
        try{
            if(httpInfo==null){
                httpInfo = WebserverHttpInfo.load(getURIFile());
            }
            if(httpInfo!=null) {
                return httpInfo.getServerPort();
            }
        }catch(IOException ioe){}
        return -1;
    }
    public static String getServerIP() throws UnknownHostException{
        try{
        	if(httpInfo==null){
        		httpInfo = WebserverHttpInfo.load(getURIFile());
        	}
            if(httpInfo==null) {
                return "";
            }
            return httpInfo.getServerIP();
        }catch(IOException ioe){}
        return null;
    }
    /**
     * @return the directory that stores jdbc drivers
     */
    public static String getDriverDirectory() {
        if(rootDirectory.equals("."+sep)){
            return "."+sep+"lib"+sep+"ext"+sep+"jdbc";
        }
        return getWebInfDirectory()+sep+"lib"+sep+"ext"+sep+"jdbc";
    }
    public static String getFetcherDirectory() {
        if(rootDirectory.equals("."+sep)){
            return "."+sep+"lib"+sep+"ext"+sep+"fetcher";
        }
        return getWebInfDirectory()+sep+"lib"+sep+"ext"+sep+"fetcher";
    }

    /**
     * @return the directory that holds the log
     */
    public static File getLogDirectoryFile() {
        return new File(ServerConfiguration.getServerConfiguration().getBaseDirectory(), "log");
    }
    private static String _encoding = "utf-8";
    public static String getEncoding() {
        return _encoding;
    }
    public static void setEncoding(String encoding) {
        if(encoding!=null && encoding.trim().length()>0) {
            _encoding = encoding.trim().intern();
        }
        //logger.debug("encoding is:"+_encoding);
    }
    public static Properties getSystemProperties() {
        return System.getProperties();
    }

    public static FacetTypes getFacetTypes() {
        if(facetTypes==null) {
            File facetTypesFile = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "WEB-INF", "conf", "facet", "facet-types.xml");
            facetTypes = (FacetTypes) XMLSerializable.fromXML(facetTypesFile);
        }
        return facetTypes;
    }
    
	public static List<String> getTimerDetails() {
		List<String> timerDetails = new LinkedList<String>();
		List<String> groups;
		long time = 0;
		String executionTime = null;
		String status = "";
		Period period = null;
		int hours = 0, minutes = 0, seconds = 0, milliseconds = 0;
		
		try {
			groups = scheduler.getJobGroupNames();
			for (String groupName : groups) {
				Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(groupName));
				for (JobKey jobKey : jobKeys) {
					List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
					for (Trigger trigger : triggers) {
						String jobName = jobKey.getName();
						String previousFireTime = trigger.getPreviousFireTime() == null ? ""
								: PageStyleUtil.format(trigger
										.getPreviousFireTime().toString(),
										"EEE MMM dd HH:mm:ss zzz yyyy",
										"EEE, MMM dd, yyyy, hh:mm:ss a");
						String nextFireTime = trigger.getNextFireTime() == null ? ""
								: PageStyleUtil.format(trigger
										.getNextFireTime().toString(),
										"EEE MMM dd HH:mm:ss zzz yyyy",
										"EEE, MMM dd, yyyy, hh:mm:ss a");
						
						SDLJobListener sdlJobListener = (SDLJobListener) scheduler.getListenerManager().getJobListener(groupName+jobName);	
						status = sdlJobListener.getStatus();
						time = sdlJobListener.getTime();
						String previousEndTime = sdlJobListener.getEndTime() == null ? ""
								: PageStyleUtil.format(sdlJobListener.getEndTime().toString(),
										"EEE MMM dd HH:mm:ss zzz yyyy",
										"EEE, MMM dd, yyyy, hh:mm:ss a");
						hours = 0;
						minutes = 0; 
						seconds = 0; 
						milliseconds = 0;
						if(previousFireTime.equals("")) {
							status = NOT_YET_STARTED;
							time = 0;							
						} else if(!previousEndTime.equals("")) {
							
							DateTime previousFireStartDateTime = new DateTime(trigger.getPreviousFireTime());
							DateTime previousFireEndDateTime = new DateTime(sdlJobListener.getEndTime());
							int isPreviousFireEndTimeGreaterThanPreviousFireDateTime = previousFireEndDateTime.compareTo(previousFireStartDateTime);
							
							if(isPreviousFireEndTimeGreaterThanPreviousFireDateTime == 1) {
								period = new Period(previousFireStartDateTime, previousFireEndDateTime , PeriodType.time());
								hours = period.getHours();
								minutes = period.getMinutes();
								seconds = period.getSeconds();
								milliseconds = period.getMillis();	
								
							} else {
								hours = (int) (time / (1000*60*60));
								minutes = (int) ((time % (1000*60*60)) / (1000*60));
								seconds = (int) (((time % (1000*60*60)) % (1000*60)) / 1000);
								milliseconds = (int) (((time % (1000*60*60)) % (1000*60)) % 1000);
								
							}
							
						}
						executionTime = StringUtils.leftPad(String.valueOf(hours), 2, "0").concat(":") + 
								StringUtils.leftPad(String.valueOf(minutes), 2, "0").concat(":") + 
								StringUtils.leftPad(String.valueOf(seconds), 2, "0").concat(":") + 
								StringUtils.leftPad(String.valueOf(milliseconds), 3, "0");
						
						String row = groupName.concat("|").concat(jobName)
									.concat("|").concat(previousFireTime)
									.concat("|").concat(previousEndTime)
									.concat("|").concat(nextFireTime)
									.concat("|").concat(status)
									.concat("|").concat(executionTime);
						timerDetails.add(row);
					}
				}
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return timerDetails;
	}

}
