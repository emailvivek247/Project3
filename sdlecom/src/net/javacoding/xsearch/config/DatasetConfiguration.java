package net.javacoding.xsearch.config;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.javacoding.xsearch.IndexManager;
import net.javacoding.xsearch.core.exception.ConfigurationException;
import net.javacoding.xsearch.indexer.ProxyAnalyzer;
import net.javacoding.xsearch.search.PerFieldAnalyzer;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.U;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Similarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.admin.ui.action.constants.IndexType;


/**
 * Class mapped to the <code>&lt;dataset&gt;</code> root element of a
 * dataset configuration file.
 */
public class DatasetConfiguration extends Configuration implements StorageConfiguration, Serializable {
	
	private static final long serialVersionUID = 8886152903607070122L;
	
	public static int DATASOURCE_TYPE_DATABASE = 0;
    public static int DATASOURCE_TYPE_FETCHER  = 1;
    

    // ----------------------------------------------------- Instance Variables

    private static Logger logger = LoggerFactory.getLogger(DatasetConfiguration.class);

    /** The base directory for dataset indices and configuration files. */
    private transient File baseDirectory = null;

    private transient long modifiedTime = 0L;

    /** The list of data sources */
    private int dataSourceType = DATASOURCE_TYPE_DATABASE;
    public int getDataSourceType() {return dataSourceType;}
    public void setDataSourceType(int type) {dataSourceType = type;}

    private FetcherConfiguration fetcherConfiguration = new FetcherConfiguration();
    public FetcherConfiguration getFetcherConfiguration() {return fetcherConfiguration;}
    public void setFetcherConfiguration(FetcherConfiguration fetcherConfiguration) {this.fetcherConfiguration = fetcherConfiguration; isDirty=true;}

    /** The list of data sources */
    private ArrayList<DataSource> dataSources;

    /** The dataquery that retrieves the working queue. */
    private WorkingQueueDataquery workingQueueDataquery = null;

    /** The list of dataqueries that retrieves content. */
    private ArrayList<ContentDataquery> contentDataqueries = null;

    private DeletionDataquery deletionQuery = null;
    
    private boolean prefixIndexRootDirectory = false;
    
    private boolean useServerDBConnection = false;
    
    private int numberOfHoursBeforeDeletion = 6;
    
    public int getNumberOfHoursBeforeDeletion() {
		return numberOfHoursBeforeDeletion;
	}
	public void setNumberOfHoursBeforeDeletion(int numberOfHoursBeforeDeletion) {
		this.numberOfHoursBeforeDeletion = numberOfHoursBeforeDeletion;
	}

	private IncrementalDataquery incrementalQuery = null;
    // ----------------------------------------------------------- Constructors

    public DatasetConfiguration(String name, File configFile, File baseDirectory){
        this.name = (name==null?null:name.intern());
        this.configFile = configFile;
        this.modifiedTime = configFile.lastModified();
        this.baseDirectory = baseDirectory;

        this.dataSources = new ArrayList<DataSource>();
        this.contentDataqueries = new ArrayList<ContentDataquery>();
        try {
            init();
        } catch (ConfigurationException e) {
            //when just creating the dc, there'll be exception since the file is not there
        }
    }
    
    public long lastModified(){
    	return this.modifiedTime;
    }

    // ------------------------------------------------------------- Properties

    /** Name of the dataset. */
    private transient String name = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.intern();
        isDirty = true;
    }

    private String displayName = null;

    /**
     * @return interned display name
     */
    public String getDisplayName() {
        return (this.displayName==null?this.name:this.displayName);
    }

    public void setDisplayName(String displayName) {
        if(displayName==null || displayName.trim().length()<=0) {
            this.displayName = null;
        }else {
            this.displayName = displayName.trim().intern();
        }
        isDirty = true;
    }
    
    private int displayOrder = 0;
    /**
     * @return Returns the displayOrder.
     */
    public int getDisplayOrder() {
        return displayOrder;
    }
    /**
     * @param displayOrder The displayOrder to set.
     */
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
        ServerConfiguration.resetDatasetConfigurationsOrder();
        isDirty = true;
    }
    

    /** The translatable description of the index (dataset). */
    private String description = null;

    /**
     * @return interned description
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description==null?null:description.intern());
        isDirty = true;
    }

    /** The directory where dataset indices are built. */
    private String indexdir = null;

    public String getIndexdir() {
    	ServerConfiguration serverConfiguration = ServerConfiguration.getServerConfiguration();
        if (indexdir == null) {
        	if (serverConfiguration.getIndexRootDirectory() == null && !prefixIndexRootDirectory) {
        		indexdir = baseDirectory + File.separator + "indexes" + File.separator + name;
        	} else {
        		indexdir = name;
        	}
        }
    	return indexdir;
    }
    public void setIndexdir(String indexdir) {
        this.indexdir = indexdir;
        isDirty = true;
    }

    /** The directory where dataset indices are merged, re-indexed. */
    private String workDirectory = "_work";
    public File getWorkDirectoryFile() {
    	return FileUtil.resolveFile(getIndexDirectoryFile(), workDirectory);
    }
    public String getWorkDirectory() {
    	return workDirectory;
    }
    public void setWorkDirectory(String workDirectory) {
        this.workDirectory = workDirectory;
        isDirty = true;
    }

    public void addDataSource(DataSource dataSource) {
        dataSources.add(dataSource);
        dataSource.setConfigObject(this);
        isDirty = true;
    }
    public DataSource getDataSource(int i) {
    	if (useServerDBConnection) {
    		return ServerConfiguration.getServerConfiguration().getServerDataSource();
    	} else {
	    	if(i<0||i>=dataSources.size()){
	    		return null;
	    	}
	    	return dataSources.get(i);
    	}
    }
    public DataSource getDataSource(String name) {
    	for(DataSource ds : dataSources){
    		if(name.equalsIgnoreCase(ds.getName())) {
    	    	if (useServerDBConnection) {
    	    		return ServerConfiguration.getServerConfiguration().getServerDataSource();
    	    	} else {	
    	    		return ds;
    	    	}
    		}
    	}
    	return null;
    }
    public int getDataSourcesSize() {
    	return dataSources.size();
    }
    public ArrayList<DataSource> getDataSources() {
    	return dataSources;
    }

    /*begin of backward database connection compatibility*/
    public void setJdbcdriver(String jdbcdriver) {
    	if(this.getDataSourcesSize()==0){
    		this.addDataSource(new DataSource());
    	}
        this.getDataSource(0).setJdbcdriver(jdbcdriver);
    }
    public void setDbUrl(String dbUrl) {
    	if(this.getDataSourcesSize()==0){
    		this.addDataSource(new DataSource());
    	}
        this.getDataSource(0).setDbUrl(dbUrl);
    }
    public void setDbUsername(String dbUsername) {
    	if(this.getDataSourcesSize()==0){
    		this.addDataSource(new DataSource());
    	}
        this.getDataSource(0).setDbUsername(dbUsername);
    }
    public void setDbPassword(String dbPassword) {
    	if(this.getDataSourcesSize()==0){
    		this.addDataSource(new DataSource());
    	}
        this.getDataSource(0).setDbPassword(dbPassword);
    }
    public void setToEncryptedDbPassword(String dbUrl,String dbPassword) {
    	if(this.getDataSourcesSize()==0){
    		this.addDataSource(new DataSource());
    	}
        this.getDataSource(0).setDbPassword(dbPassword);
    }
    public void setDbcpValidationQuery(String dbcpValidationQuery) {
    	if(this.getDataSourcesSize()==0){
    		this.addDataSource(new DataSource());
    	}
        this.getDataSource(0).setDbcpValidationQuery(dbcpValidationQuery);
    }
    /*end of backward database connection compatibility*/


    /** The number of threads retrieving content. */
    private int fetcherThreadsCount = 7;

    public int getFetcherThreadsCount() {
        return fetcherThreadsCount;
    }

    public void setFetcherThreadsCount(int fetcherThreadsCount) {
        this.fetcherThreadsCount = fetcherThreadsCount;
        isDirty = true;
    }

    /** The number of threads retrieving content. */
    private int batchFetcherThreadsCount = 2;

    public int getBatchFetcherThreadsCount() {
        return batchFetcherThreadsCount;
    }
    public void setBatchFetcherThreadsCount(int batchFetcherThreadsCount) {
        this.batchFetcherThreadsCount = batchFetcherThreadsCount;
        isDirty = true;
    }

    /** The number of threads building indices. */
    private int writerThreadsCount = 3;

    public int getWriterThreadsCount() {
        return writerThreadsCount;
    }

    public void setWriterThreadsCount(int writerThreadsCount) {
        this.writerThreadsCount = writerThreadsCount;
        isDirty = true;
    }

    private int listFetchSize = 1000;

    public int getListFetchSize() {
        return (listFetchSize>=0?listFetchSize:0);
    }

    public void setListFetchSize(int listFetchSize) {
        this.listFetchSize = listFetchSize;
        isDirty = true;
    }

    public int getDbcpMaxactive() {
        return getFetcherThreadsCount();
    }

    /** Action to take in case of an exhausted DBCP connection pool.
    *    default is 1 = GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK
    */
    public byte getDbcpWhenexhausted() {
        return 1;
    }

    /**
     * @return 3000L
     */
    public long getDbcpMaxwait() {
        return 3000L;
    }

    /**
     * @return getDbcpMaxactive
     */
    public int getDbcpMaxidle() {
        return getDbcpMaxactive();
    }

    /**
     * @return size of contentDataqueries + 1
     */
    public int getDbcpPsMaxactive() {
        if(contentDataqueries==null) return 1;
        return contentDataqueries.size()+1;
    }

    /**
     * @return 1, block if exhausted
     */
    public byte getDbcpPsWhenexhausted() {
        return 1;
    }

    /**
     * @return 1000L
     */
    public long getDbcpPsMaxwait() {
        return 1000L;
    }

    public int getDbcpPsMaxidle() {
        return getDbcpPsMaxactive();
    }

    /** Maximum size of indices measured in Megabytes. */
    private double indexMaxSize = ConfigConstants.FREE_SIZE_LIMIT;
    public double getIndexMaxSize() {
        return indexMaxSize;
    }
    public void setIndexMaxSize(double indexMaxSize) {
        this.indexMaxSize = indexMaxSize;
        isDirty = true;
    }

    private float mergePercentage = 1;
    public float getMergePercentage() {
        return mergePercentage;
    }
    public void setMergePercentage(float percent) {
        this.mergePercentage = percent;
        isDirty = true;
    }
    
    private NonPeakHours mergeHours = new NonPeakHours();
    public NonPeakHours getMergeHours() {
        return mergeHours;
    }
    public void setMergeHours(String enabled, String begin, String end) {;
        mergeHours.enabled = U.getBoolean(enabled, "true", false);
        mergeHours.begin = U.getInt(begin, 1);
        mergeHours.end = U.getInt(end, 3);
        isDirty = true;
    }

    /** JVM heap size measured in Megabytes. */
    private int prunePercentage = 90;
    public int getPrunePercentage() {
        return prunePercentage;
    }
    public void setPrunePercentage(int percent) {
        this.prunePercentage = percent;
        isDirty = true;
    }
    
    /** JVM heap size measured in Megabytes. */
    private int jvmMaxHeapSize = 256;

    public int getJvmMaxHeapSize() {
        return jvmMaxHeapSize;
    }

    public void setJvmMaxHeapSize(int maxSize) {
        this.jvmMaxHeapSize = maxSize;
        isDirty = true;
    }
    
    private int documentBufferSizeMB = 16;
    public int getDocumentBufferSizeMB() {
        return documentBufferSizeMB;
    }

    public void setDocumentBufferSizeMB(int documentBufferSizeMB) {
        this.documentBufferSizeMB = documentBufferSizeMB;
        isDirty = true;
    }

    /** The language for building indices. */
    private String language = "Any";

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        isDirty = true;
    }

    /** The class name of the analyzerName for building indices. */
    private String analyzerName = "com.fdt.sdl.core.analyzer.NumberLowerCaseAnalyzer";

    public String getAnalyzerName() {
        return analyzerName;
    }

    /**
     * set the analyzerName and trys to pre-load the class
     * Pre-loading can save 40 ms on first search
     * @param analyzerName analyzerName full class name
     */
    public void setAnalyzerName(String analyzerName) {
        this.analyzerName = analyzerName;
        isDirty = true;
        try {
            Class.forName(analyzerName).newInstance();
            //logger.debug(analyzerName+" loaded!");
        } catch (ClassNotFoundException e) {
            logger.debug("Class Not Found for:"+analyzerName+" "+e.getMessage());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    public transient static String DefaultSimilarity = "org.apache.lucene.search.DefaultSimilarity";
    private String similarityName = DefaultSimilarity;
    
	public String getSimilarityName() {
		return similarityName;
	}

	public void setSimilarityName(String similarityName) {
        this.similarityName = similarityName;
        isDirty = true;
        if(similarityName!=null) try {
            Class.forName(similarityName).newInstance();
        } catch (ClassNotFoundException e) {
			logger.warn("ClassNotFoundException", e);
            e.printStackTrace();
        } catch (InstantiationException e) {
			logger.warn("InstantiationException", e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
			logger.warn("IllegalAccessException", e);
            e.printStackTrace();
        }
	}

    transient Similarity _similarity = null;
    public Similarity getSimilarity(){
        if(_similarity!=null) return _similarity;
        try {
			_similarity = (Similarity) Class.forName(similarityName).newInstance();
		} catch (InstantiationException e) {
			logger.warn("InstantiationException", e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			logger.warn("IllegalAccessException", e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.warn("ClassNotFoundException", e);
			e.printStackTrace();
		}
        return _similarity;
    }

    /** The schedule for indexing. */
    private List<Schedule> schedules = new ArrayList<Schedule>();
    public List<Schedule> getSchedules() {
        return schedules;
    }
    /** add/update any schedule with the same indexing mode */
    public void addSchedule(Schedule schedule) {
        for(int i=0;i<this.schedules.size();i++) {
            if(schedules.get(i).getIndexingMode().equals(schedule.getIndexingMode())) {
                schedules.set(i, schedule);
                isDirty = true;
                return;
            }
        }
        this.schedules.add(schedule);
        isDirty = true;
    }
    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
        isDirty = true;
    }

    /** Name of the default template. */
    private String defaultTemplateName = null;

    /**
     * @return interned default template name
     */
    public String getDefaultTemplateName() {
        return defaultTemplateName;
    }

    public void setDefaultTemplateName(String defaultTemplateName) {
        this.defaultTemplateName = (defaultTemplateName==null?null:defaultTemplateName.intern());
        isDirty = true;
    }

    /** Name of the tablet template. */
    private String tabletTemplateName = null;

    /**
     * @return interned tablet template name
     */
    public String getTabletTemplateName() {
        return tabletTemplateName;
    }

    public void setTabletTemplateName(String tabletTemplateName) {
        this.tabletTemplateName = (tabletTemplateName==null?null:tabletTemplateName.intern());
        isDirty = true;
    }

    /** Name of the Mobile template. */
    private String mobileTemplateName = null;

    /**
     * @return interned Mobile template name
     */
    public String getMobileTemplateName() {
        return mobileTemplateName;
    }

    public void setMobileTemplateName(String mobileTemplateName) {
        this.mobileTemplateName = (mobileTemplateName==null?null:mobileTemplateName.intern());
        isDirty = true;
    }    
    
    private int searcherMaxactive = 8;

    public int getSearcherMaxactive() {
        return searcherMaxactive;
    }

    public void setSearcherMaxactive(int searcherMaxactive) {
        this.searcherMaxactive = searcherMaxactive;
        isDirty = true;
    }

    private byte searcherWhenexhausted = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;

    public byte getSearcherWhenexhausted() {
        return searcherWhenexhausted;
    }

    public void setSearcherWhenexhausted(byte searcherWhenexhausted) {
        this.searcherWhenexhausted = searcherWhenexhausted;
        isDirty = true;
    }

    private long searcherMaxwait = 10000L;

    public long getSearcherMaxwait() {
        return searcherMaxwait;
    }

    public void setSearcherMaxwait(long searcherMaxwait) {
        this.searcherMaxwait = searcherMaxwait;
        isDirty = true;
    }

    private int searcherMaxidle = 7;

    public int getSearcherMaxidle() {
        return searcherMaxidle;
    }

    public void setSearcherMaxidle(int searcherMaxidle) {
        this.searcherMaxidle = searcherMaxidle;
        isDirty = true;
    }

    private boolean isInMemorySearch = false;

    public boolean getIsInMemorySearch() {
        return isInMemorySearch;
    }

    public void setIsInMemorySearch(boolean isInMemory) {
        this.isInMemorySearch = isInMemory;
        isDirty = true;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Initializes this object by parsing the configuration file.
     *
     * @throws ConfigurationException
     */
    @Override
	public void init() throws ConfigurationException {
    	dataSources.clear();
        contentDataqueries.clear();
        _columns = null;
        _sortable_columns = null;
        _searchable_columns = null;
        _filterable_columns = null;
        _contentDataqueryArray = null;
        hasSearchedDateWeightColumn = false;
        dateWeightColumn = null;
        super.init();
    }

    /**
     * Called after saving the file to disk
     * Some derived values needs to be init here
     *
     * @throws ConfigurationException
     */
    @Override
	public void save() throws IOException {
        ConfigurationHistory.save(this);
        _columns = null;
        _sortable_columns = null;
        _searchable_columns = null;
        _filterable_columns = null;
        _contentDataqueryArray = null;
        hasSearchedDateWeightColumn = false;
        dateWeightColumn = null;
        super.save();
    }

    public void setBaseDirectory(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }
    public File getBaseDirectoryFile() {
        return this.baseDirectory;
    }
    public String getIndexDirectory() {
        return getIndexDirectoryFile().getAbsolutePath();
    }
    public File getIndexDirectoryFile() {
    	File indexDirectoryFile = null;
    	if (ServerConfiguration.getServerConfiguration().getIndexRootDirectory() != null && prefixIndexRootDirectory) {
    		indexDirectoryFile = FileUtil.resolveFile(new File(ServerConfiguration.getServerConfiguration()
    			.getIndexRootDirectory()), getIndexdir()); 
    	} else {	
    		indexDirectoryFile  = new File(getIndexdir());
    	}
    	return indexDirectoryFile;
    }    
    public File getMainIndexDirectoryFile() {
        return FileUtil.resolveFile(getIndexDirectoryFile(), "main");
    }
    public File getAltMainIndexDirectoryFile() {
        return FileUtil.resolveFile(getIndexDirectoryFile(), "main0");
    }
    public File getTempIndexDirectoryFile() {
        return FileUtil.resolveFile(getIndexDirectoryFile(), "temp");
    }
    public File getAltTempIndexDirectoryFile() {
        return FileUtil.resolveFile(getIndexDirectoryFile(), "temp1");
    }
    public File getDictionaryIndexDirectoryFile() {
        return FileUtil.resolveFile(getIndexDirectoryFile(), "dictionary");
    }
    public File getPhraseIndexDirectoryFile() {
        return FileUtil.resolveFile(getIndexDirectoryFile(), "phrase");
    }
    public File getSynonymsDirectoryFile() {
        return FileUtil.resolveFile(getDictionaryIndexDirectoryFile(), "synonyms");
    }
    public File getFullListIndexDirectoryFile() {
        return FileUtil.resolveFile(getIndexDirectoryFile(), "_full");
    }

    /**
     * Gets the dataquery that retrieves the working queue.
     *
     * @return the dataquery that retrieves the working queue
     */
    public WorkingQueueDataquery getWorkingQueueDataquery() {
        return workingQueueDataquery;
    }

    /**
     * Gets an array of dataqueries that retrieve content.
     *
     * @return an array of dataqueries that retrieve content
     */
    private transient ContentDataquery[] _contentDataqueryArray = null;
    public ContentDataquery[] getContentDataqueryArray() {
        if(_contentDataqueryArray!=null&&!isDirty) return _contentDataqueryArray;
        ContentDataquery results[] = new ContentDataquery[contentDataqueries.size()];
        _contentDataqueryArray = (contentDataqueries.toArray(results));
        return _contentDataqueryArray;
    }
    public ArrayList<ContentDataquery> getContentDataqueries() {
        return contentDataqueries;
    }

    /**
     * Deletes a content dataquery for the given id.
     *
     * @param id the id of the content dataquery to be deleted
     */
    public void deleteContentDataquery(int id) {
        // Use 1+index of the ArraryList as id
        contentDataqueries.remove(id-1);
        isDirty = true;
    }

    /**
     * Adds a dataquery.
     *
     * @param dataquery the dataquery that retrieves the working queue
     */
    public void addDataquery(WorkingQueueDataquery dataquery) {
        workingQueueDataquery = dataquery;
        dataquery.setConfigObject(this);
        isDirty = true;
    }

    /**
     * Adds a dataquery.
     *
     * @param dataquery the dataquery that retrieves content
     */
    public void addDataquery(ContentDataquery dataquery) {
        contentDataqueries.add(dataquery);
        dataquery.setConfigObject(this);
        isDirty = true;
    }

    public void addDataquery(DeletionDataquery dataquery) {
        deletionQuery=dataquery;
        dataquery.setConfigObject(this);
        isDirty = true;
    }

    public void addDataquery(IncrementalDataquery dataquery) {
        incrementalQuery=dataquery;
        dataquery.setConfigObject(this);
        isDirty = true;
    }

    /**
     * Adds a dataquery.
     *
     * @param dataquery a general dataquery
     */
    public void addDataquery(Dataquery dataquery) {
        String msg = "General dataquery is not supported as of now";
        logger.error(msg);
    }

    /**
     * Search a column by name within workingQueueDataquery and contentDataqueries
     */
    public Column findColumn(String name) {
        Column c = (workingQueueDataquery==null?null:workingQueueDataquery.getColumn(name));
        if (c != null) return c;
        for (int i = 0; contentDataqueries!=null&&i < contentDataqueries.size(); i++){
            c = (contentDataqueries.get(i)).getColumn(name);
            if (c != null) return c;
        }
        return null;
    }

    public ArrayList<Column> findColumnsByFieldType(String family) {
        if (family == null) return null;
        ArrayList<Column> columns = getColumns();
        ArrayList<Column> al = new ArrayList<Column>();
        for (Column c : columns) {
            if (c != null && IndexFieldType.belongsTo(c.getIndexFieldType(),family)) {
                al.add(c);
            }
        }
        return al;
    }

    public List<String> getColumnNames() {
        return getColumns().stream().map(c -> c.getColumnName()).collect(Collectors.toList());
    }

    /**
     * Similar to findColumn(), but uses an internal hash to avoid loop
     * The internal hash is cached, better not used it when still editting the sql queries
     */
    private transient Map<String,Column> mapStringToColumn = null;
    public Column getColumn(String name) {
        if(name==null) return null;
        if(mapStringToColumn!=null &&!isDirty) return mapStringToColumn.get(name.toLowerCase());
        synchronized(COLUMNS_LOCK) {
            mapStringToColumn = new HashMap<String,Column>();
            if(workingQueueDataquery!=null){
                for(Column c: workingQueueDataquery.getColumns()) {
                    mapStringToColumn.put(c.getName().toLowerCase(), c);
                }
            }
            if(contentDataqueries!=null){
                for(int i = 0; i < contentDataqueries.size(); i++){
                    for(Column c: (contentDataqueries.get(i)).getColumns()) {
                        mapStringToColumn.put(c.getName().toLowerCase(), c);
                    }
                }
            }
        }
        return mapStringToColumn.get(name.toLowerCase());
    }

    private transient ArrayList<Column> _columns = null;
    private transient Object COLUMNS_LOCK = new Object();
    public ArrayList<Column> getColumns(boolean force) {
        if(_columns!=null &&!isDirty && !force) return _columns;
        synchronized (COLUMNS_LOCK) {
            _columns = new ArrayList<Column>();
            if(workingQueueDataquery!=null){
                _columns.addAll(workingQueueDataquery.getColumns());
            }
            if(contentDataqueries!=null){
                for(int i = 0; i < contentDataqueries.size(); i++){
                    ArrayList<Column> cl = (contentDataqueries.get(i)).getColumns();
                    for(int j=0; j<cl.size(); j++){
                        boolean found = false;
                        Column c = cl.get(j);
                        for(int k=0; k<_columns.size(); k++){
                            Column a = (Column)_columns.get(k);
                            if(c.getColumnName().equals(a.getColumnName())){
                                found = true;
                                break;
                            }
                        }
                        if(!found){
                            _columns.add(c);
                        }
                    }
                }
            }
            return _columns;
		}
    }

    public ArrayList<Column> getColumns() {
    	return getColumns(false);
    }
    /**
     * 
     * @return all previous columns from main query and subsequent queries 
     */
    /**
     * @param currentQueryIndex 0 for main(not useful), 
     *     n for main columns + previous (n-1) subsequent queries 
     * @return
     */
    public ArrayList<Column> getFecthedColumns(int currentQueryIndex) {
        if( currentQueryIndex <= 0 ) return null;
        ArrayList<Column> fetchedColumns = new ArrayList<Column>();
        if(workingQueueDataquery!=null){
            fetchedColumns.addAll(workingQueueDataquery.getColumns());
        }
        if(contentDataqueries!=null){
            for(int i = 0; i < contentDataqueries.size()&&i<currentQueryIndex-1; i++){
                ArrayList<Column> cl = ((ContentDataquery)contentDataqueries.get(i)).getColumns();
                for(Column c : cl){
                    boolean found = false;
                    for(Column a : fetchedColumns){
                        if(c.getColumnName().equals(a.getColumnName())){
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        fetchedColumns.add(c);
                    }
                }
            }
        }
        return fetchedColumns;
    }

    
    private transient ArrayList<Column> _sortable_columns = null;
    public ArrayList<Column> getSortableColumns() {
        if(_sortable_columns!=null&&!isDirty) return _sortable_columns;
        _sortable_columns = new ArrayList<Column>();
        ArrayList<Column> cl = getColumns(true);
        for(Column c : cl ){
            if(c.getIsSortable()){
                _sortable_columns.add(c);
            }
        }
        Column[] cs = (Column[]) _sortable_columns.toArray(new Column[_sortable_columns.size()]);
        _sortable_columns.clear();                              // empty the list
        Arrays.sort(cs, new Comparator<Column>() {         // sort the array
            public int compare(Column o1, Column o2) {
              return ((Column)o1).getSortDisplayOrder() - ((Column)o2).getSortDisplayOrder();
            }
          });
        for (int i = 0; i < cs.length; i++) {
            _sortable_columns.add(cs[i]);                  // re-build list, now sorted
        }
        return _sortable_columns;
    }

    private transient ArrayList<Column> _searchable_columns = null;
    public ArrayList<Column> getSearchableColumns() {
        if(_searchable_columns!=null&&!isDirty) return _searchable_columns;
        _searchable_columns = new ArrayList<Column>();
        ArrayList<Column> cl = getColumns(true);
        for(Column c : cl){
            if(c.getIsSearchable()){
                _searchable_columns.add(c);
            }
        }
        return _searchable_columns;
    }

    private transient ArrayList<Column> _filterable_columns = null;
    public ArrayList<Column> getFilterableColumns() {
        if(_filterable_columns!=null&&!isDirty) return _filterable_columns;
        _filterable_columns = new ArrayList<Column>();
        ArrayList<Column> cl = getColumns(true);
        for(Column c : cl){
            if(c.getIsFilterable()){
                _filterable_columns.add(c);
            }
        }
        Column[] cs = (Column[]) _filterable_columns.toArray(new Column[_filterable_columns.size()]);
        _filterable_columns.clear();                              // empty the list
        Arrays.sort(cs, new Comparator<Column>() {         // sort the array
            public int compare(Column o1, Column o2) {
              return ((Column)o1).getFilterDisplayOrder() - ((Column)o2).getFilterDisplayOrder();
            }
          });
        for (int i = 0; i < cs.length; i++) {
            _filterable_columns.add(cs[i]);                  // re-build list, now sorted
        }
        return _filterable_columns;
    }

    private int maxOpenFiles = 800;
    private transient int maxBufferedDocs = -1;
    private transient int maxMergeDocs = 1000*1000;
    private transient int maxFieldLength = IndexWriter.DEFAULT_MAX_FIELD_LENGTH;
    /**
     * @return Returns the maxOpenFiles.
     */
    public int getMaxOpenFiles() {
        return maxOpenFiles;
    }
    /**
     * @param maxOpenFiles The maxOpenFiles to set.
     */
    public void setMaxOpenFiles(int maxOpenFiles) {
        this.maxOpenFiles = maxOpenFiles;
        isDirty = true;
    }
    /**
     * @return Returns the maxBufferedDocs.
     */
    public int getMaxBufferedDocs() {
        return maxBufferedDocs;
    }
    /**
     * @param maxBufferedDocs The maxBufferedDocs to set.
     */
    public void setMaxBufferedDocs(int maxBufferedDocs) {
        this.maxBufferedDocs = maxBufferedDocs;
        isDirty = true;
    }
    /**
     * @return Returns the maxFieldLength.
     */
    public int getMaxFieldLength() {
        return maxFieldLength;
    }
    /**
     * @param maxFieldLength The maxFieldLength to set.
     */
    public void setMaxFieldLength(int maxFieldLength) {
        this.maxFieldLength = maxFieldLength;
        isDirty = true;
    }
    /**
     * @return Returns the maxMergeDocs.
     */
    public int getMaxMergeDocs() {
        return maxMergeDocs;
    }
    /**
     * @param maxMergeDocs The maxMergeDocs to set.
     */
    public void setMaxMergeDocs(int maxMergeDocs) {
        this.maxMergeDocs = maxMergeDocs;
        isDirty = true;
    }

    boolean isOptimizeNeeded = true;
    public boolean getIsOptimizeNeeded() {
        return isOptimizeNeeded;
    }
    public void setIsOptimizeNeeded(boolean isOptimizeNeeded) {
        this.isOptimizeNeeded = isOptimizeNeeded;
        isDirty = true;
    }

    /**
     * Based on number of fields to calculate optimal merge factor
     * @return Returns the mergeFactor.
     */
    public int getMergeFactor() {
        int mergeFactor = maxOpenFiles/(7+getColumns().size());
        if(mergeFactor>50) {
            mergeFactor = 50;
        }else if(mergeFactor<2) {
            mergeFactor = 2;
        }
        return mergeFactor;
    }
    
    private boolean isWildcardAllowed = false;
    public boolean getIsWildcardAllowed() {
        return isWildcardAllowed;
    }
    public void setIsWildcardAllowed(boolean isWildcardAllowed) {
        this.isWildcardAllowed = isWildcardAllowed;
        isDirty = true;
    }
    private boolean isWildcardLowercaseNeeded = true;
    public boolean getIsWildcardLowercaseNeeded() {
        return isWildcardLowercaseNeeded;
    }
    public void setIsWildcardLowercaseNeeded(boolean isWildcardLowercaseNeeded) {
        this.isWildcardLowercaseNeeded = isWildcardLowercaseNeeded;
        isDirty = true;
    }
    
    private int minWildcardPrefixLength = 4;
    public int getMinWildcardPrefixLength() {
        return minWildcardPrefixLength;
    }
    public void setMinWildcardPrefixLength(int minWildcardPrefixLength) {
        this.minWildcardPrefixLength = minWildcardPrefixLength;
        isDirty = true;
    }

    private String subscriptionUrl = null;
    public String getSubscriptionUrl() {
        return subscriptionUrl;
    }
    public void setSubscriptionUrl(String url) {
        this.subscriptionUrl = url;
        isDirty = true;
    }

    private boolean isQueryDefaultAnd = true;
    public boolean getIsQueryDefaultAnd() {
        return isQueryDefaultAnd;
    }
    public void setIsQueryDefaultAnd(boolean rel) {
        isQueryDefaultAnd = rel;
        isDirty = true;
    }

    private boolean isSecure = false;
    public boolean getIsSecure() {
        return isSecure;
    }
    public void setIsSecure(boolean secure) {
        isSecure = secure;
        isDirty = true;
    }
    
    private String allowedIpList = "*";
    public String getAllowedIpList() {
        return allowedIpList;
    }
    public void setAllowedIpList(String allowedIpList) {
        this.allowedIpList = allowedIpList;
        isDirty = true;
    }

    private boolean isEmptyQueryMatchAll = true;
    public boolean getIsEmptyQueryMatchAll() {
        return isEmptyQueryMatchAll;
    }
    public void setIsEmptyQueryMatchAll(boolean isEmptyQueryMatchAll) {
        this.isEmptyQueryMatchAll = isEmptyQueryMatchAll;
        isDirty = true;
    }

    private String urlToPing = null;
    public String getUrlToPing() {
        return urlToPing;
    }
    public void setUrlToPing(String urlToPing) {
    	if(urlToPing!=null){
    		urlToPing=urlToPing.trim();
    	}
        this.urlToPing = urlToPing;
        isDirty = true;
    }

    private transient boolean hasSearchedDateWeightColumn = false;
    private Column dateWeightColumn = null;
    public String getDateWeightColumnName() {
    	if(!hasSearchedDateWeightColumn){
            ArrayList<Column> columns = this.getColumns();
            for(int i=0;i<columns.size();i++) {
                Column c = (Column)columns.get(i); 
                if(c.getIsDateWeight()) {
                	dateWeightColumn = c;
                	break;
                }
            }
        	hasSearchedDateWeightColumn = true;
    	}
        return (dateWeightColumn==null ? null : dateWeightColumn.getColumnName());
    }
    public void setDateWeightColumnName(String dateWeightColumnName) {
        ArrayList<Column> columns = this.getColumns();
        dateWeightColumn = null;
        for(int i=0;i<columns.size();i++) {
            Column c = (Column)columns.get(i); 
            if(c.canBeDateWeight() && c.getColumnName().equals(dateWeightColumnName)) {
            	c.setIsDateWeight(true);
            	dateWeightColumn = c;
            }else{
            	c.setIsDateWeight(false);
            }
        }
    }
    
    public DeletionDataquery getDeletionQuery() {
        return deletionQuery;
    }

    public void setDeletionQuery(DeletionDataquery deletionQuery) {
        this.deletionQuery = deletionQuery;
    }

    public IncrementalDataquery getIncrementalDataquery() {
        return incrementalQuery;
    }

    public void setIncrementalDataquery(IncrementalDataquery incrementalQuery) {
        this.incrementalQuery = incrementalQuery;
    }

    private boolean isSpellChecking = true;
    public boolean getIsSpellChecking() {
        return isSpellChecking;
    }
    public void setIsSpellChecking(boolean isSpellChecking) {
        this.isSpellChecking = isSpellChecking;
        isDirty = true;
    }

    /**
     * Returns an XML representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(XML_DECLARATION).append("\n");

        sb.append("<dataset");
        if (displayName != null) {
            sb.append(" name=\"").append(displayName).append("\"");
        }
        sb.append(" exportedBy=\"").append(IndexManager.loadProductProperties().getProperty("product.version")).append("\"");
        sb.append(" createdAt=\"").append(new Date()).append("\"");
        sb.append(">\n");

        if (displayOrder != 0) {
            sb.append("  <display-order>").append(displayOrder).append("</display-order>\n");
        }

        if (description != null) {
            sb.append("  <description><![CDATA[").append(description).append("]]></description>\n");
        }

        if (indexType != null) {
            sb.append("  <index-type>").append(indexType.toString()).append("</index-type>\n");
        }

        if (indexdir != null) {
            sb.append("  <index-dir>").append(indexdir).append("</index-dir>\n");
        }

        sb.append("  <is-prefix-root-index-directory>").append(prefixIndexRootDirectory).append("</is-prefix-root-index-directory>\n");
        
        sb.append("  <use-server-db-connection>").append(useServerDBConnection).append("</use-server-db-connection>\n");
        
        sb.append("  <number-of-hours-before-deletion>").append(numberOfHoursBeforeDeletion).append("</number-of-hours-before-deletion>\n");
        
        if (workDirectory != null) {
            sb.append("  <work-directory>").append(workDirectory).append("</work-directory>\n");
        }

        if (mergePercentage != 5) {
            sb.append("  <temporary-index-minimal-merge-percentage>").append(mergePercentage).append("</temporary-index-minimal-merge-percentage>\n");
        }

        if (prunePercentage != 90 ) {
            sb.append("  <prune-index-target-percentage>").append(prunePercentage).append("</prune-index-target-percentage>\n");
        }
        sb.append("  <merge-hours enabled=\"").append(mergeHours.enabled).append("\"")
        .append(" begin=\"").append(mergeHours.begin).append("\"")
        .append(" end=\"").append(mergeHours.end).append("\"")
        .append("/>\n");

        sb.append("  <fetcher-threads>").append(fetcherThreadsCount).append("</fetcher-threads>\n");
        if(batchFetcherThreadsCount!=2){
            sb.append("  <batch-fetcher-threads>").append(batchFetcherThreadsCount).append("</batch-fetcher-threads>\n");
        }
        sb.append("  <writer-threads>").append(writerThreadsCount).append("</writer-threads>\n");
        sb.append("  <index-max-size>").append(indexMaxSize).append("</index-max-size>\n");
        if(maxMergeDocs!=1000*1000) {
            sb.append("  <max-merge-docs>").append(maxMergeDocs).append("</max-merge-docs>\n");
        }
        if(isOptimizeNeeded != true) {
            sb.append("  <is-optimize-needed>").append(isOptimizeNeeded).append("</is-optimize-needed>\n");
        }
        if ( listFetchSize != 1000) {
            sb.append("  <list-fetch-size>").append(listFetchSize).append("</list-fetch-size>\n");
        }

        if (schedules != null && schedules.size()>0) {
            for(int i=0;i<this.schedules.size();i++) {
                schedules.get(i).setId(i);
            }
            sb.append("  <schedules>\n");
            for(Schedule s : schedules) {
                sb.append(s);
            }
            sb.append("  </schedules>\n");
        }

        if (language != null) {
            sb.append("  <language>").append(language).append("</language>\n");
        }

        if (analyzerName != null) {
            sb.append("  <analyzer-name>").append(analyzerName).append("</analyzer-name>\n");
        }
        
        if (!DatasetConfiguration.DefaultSimilarity.equals(similarityName)){
            sb.append("  <similarity-name>").append(similarityName).append("</similarity-name>\n");
        }

        sb.append("  <data-source-type>").append(dataSourceType).append("</data-source-type>\n");
        if(fetcherConfiguration!=null) {
            sb.append(fetcherConfiguration);
        }

        if (dataSources!= null && dataSources.size() > 0 && !this.useServerDBConnection) {
	        sb.append("  <data-sources>\n");
	        for (int i = 0; i < dataSources.size(); i++) {
	            sb.append((DataSource)dataSources.get(i));
	        }
	        sb.append("  </data-sources>\n");
        }

        if (workingQueueDataquery != null) {
            sb.append(workingQueueDataquery);
        }

        ContentDataquery[] cdqs = getContentDataqueryArray();
        for (int i = 0; i < cdqs.length; i++) {
            sb.append(cdqs[i]);
        }

        if (deletionQuery != null) {
            sb.append(deletionQuery);
        }

        if (incrementalQuery != null) {
            sb.append(incrementalQuery);
        }

        if (defaultTemplateName != null) {
            sb.append("  <default-template>").append(defaultTemplateName).append("</default-template>\n");
        }

        if (tabletTemplateName != null) {
            sb.append("  <tablet-template>").append(tabletTemplateName).append("</tablet-template>\n");
        }
        
        if (mobileTemplateName != null) {
            sb.append("  <mobile-template>").append(mobileTemplateName).append("</mobile-template>\n");
        }
        
        sb.append("  <searcher-max-active>").append(searcherMaxactive).append("</searcher-max-active>\n");
        sb.append("  <searcher-when-exhausted>").append(searcherWhenexhausted).append("</searcher-when-exhausted>\n");
        sb.append("  <searcher-max-wait>").append(searcherMaxwait).append("</searcher-max-wait>\n");
        sb.append("  <searcher-max-idle>").append(searcherMaxidle).append("</searcher-max-idle>\n");
        sb.append("  <is-in-memory-search>").append(isInMemorySearch).append("</is-in-memory-search>\n");
        if(maxOpenFiles!=800) {
            sb.append("  <max-open-files>").append(maxOpenFiles).append("</max-open-files>\n");
        }
        if(jvmMaxHeapSize!=256){
            sb.append("  <jvm-max-heap-size>").append(jvmMaxHeapSize).append("</jvm-max-heap-size>\n");
        }
        
        if(maxFieldLength != IndexWriter.DEFAULT_MAX_FIELD_LENGTH) {
            sb.append("  <max-field-length>").append(maxFieldLength).append("</max-field-length>\n");
        }
        if(documentBufferSizeMB!=16){
            sb.append("  <document-buffer-size-mb>").append(documentBufferSizeMB).append("</document-buffer-size-mb>\n");
        }

        if(isWildcardAllowed) {
            sb.append("  <is-wildcard-allowed>").append(isWildcardAllowed).append("</is-wildcard-allowed>\n");
        }
        if(!isWildcardLowercaseNeeded) {
            sb.append("  <is-wildcard-lowercase-needed>").append(isWildcardLowercaseNeeded).append("</is-wildcard-lowercase-needed>\n");
        }
        if (minWildcardPrefixLength!= 4) {
            sb.append("  <min-wildcard-prefix-length>").append(minWildcardPrefixLength).append("</min-wildcard-prefix-length>\n");
        }

        if (subscriptionUrl != null) {
            sb.append("  <subscription-url>").append(subscriptionUrl).append("</subscription-url>\n");
        }
        
        if (isQueryDefaultAnd == false) {
            sb.append("  <is-query-default-and>").append(isQueryDefaultAnd).append("</is-query-default-and>\n");
        }
        
        if (isSecure == true) {
            sb.append("  <is-secure>").append(isSecure).append("</is-secure>\n");
        }

        if (allowedIpList != null) {
            sb.append("  <allowed-ip-list>").append(allowedIpList).append("</allowed-ip-list>\n");
        }
        if (isEmptyQueryMatchAll == false ) {
            sb.append("  <is-empty-query-match-all>").append(isEmptyQueryMatchAll).append("</is-empty-query-match-all>\n");
        }
        if (!U.isEmpty(urlToPing)){
            sb.append("  <url-to-ping><![CDATA[").append(urlToPing).append("]]></url-to-ping>\n");
        }

        sb.append("  <date-weight-formula>\n");
        TimeWeight[] tws = getTimeWeights();
        for(int i=0; i<tws.length;i++){
        	sb.append("    ");
        	sb.append(tws[i].toXML());
        	sb.append("\n");
        }
        sb.append("  </date-weight-formula>\n");
        
        if (isSpellChecking == true) {
            sb.append("  <spell-checking enabled=\"").append(isSpellChecking).append("\">").append("</spell-checking>\n");
        }

        if (numberReplicas != 0) {
            sb.append("  <number-replicas>").append(numberReplicas).append("</number-replicas>\n");
        }

        if (numberShards != 1) {
            sb.append("  <number-shards>").append(numberShards).append("</number-shards>\n");
        }

        sb.append("</dataset>\n");

        return sb.toString();
    }

    // ----------------------------------------------------- Validation Methods

    public boolean isRawField(String field){
        Column column = findColumn(field);
        if (column == null ) return false;
        if (IndexFieldType.belongsTo(column.getIndexFieldType(),IndexFieldType.KEYWORD)) 
            return true;
        return false;
    }
    
    public Column getPrimaryKeyColumn() {
        return (workingQueueDataquery==null?null:workingQueueDataquery.getPrimaryKeyColumn());
    }
    public Column getModifiedDateColumn() {
        return (workingQueueDataquery==null?null:workingQueueDataquery.getModifiedDateColumn());
    }

    public String getSecureColumnName() {
        if(!this.isSecure) {
            return null;
        }
        ArrayList<Column> columns = this.getColumns();
        for(int i=0;i<columns.size();i++) {
            Column c = (Column)columns.get(i); 
            if(c.getIsSecure()) {
                return c.getColumnName();
            }
        }
        return null;
    }
    
    transient PerFieldAnalyzer _analyzer = null;
    public Analyzer getAnalyzer() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        if(_analyzer!=null) return _analyzer;
        _analyzer = new PerFieldAnalyzer((Analyzer) Class.forName(this.getAnalyzerName()).newInstance());
        ArrayList<Column> columns = this.getColumns(true);
        for(int i=0; i< columns.size(); i++) {
            Column c = (Column) columns.get(i);
            if(c.getNeedSynonymsAndStopwords()) {
                if(U.isEmpty(c.getAnalyzerName())) {
                    _analyzer.addAnalyzer(c.getColumnName(), new ProxyAnalyzer(this,(Analyzer) Class.forName(this.getAnalyzerName()).newInstance(),false, true));
                } else {
                    _analyzer.addAnalyzer(c.getColumnName(), new ProxyAnalyzer(this,(Analyzer) Class.forName(c.getAnalyzerName()).newInstance(),false, true));
                }
            } else if(!U.isEmpty(c.getAnalyzerName())) {
                _analyzer.addAnalyzer(c.getColumnName(), (Analyzer) Class.forName(c.getAnalyzerName()).newInstance());
            } else if(c.getIsKeyword()) {
                _analyzer.addAnalyzer(c.getColumnName(), new KeywordAnalyzer());
            }
        }
        return (Analyzer) _analyzer;
    }
    public Analyzer getIndexingAnalyzer() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        PerFieldAnalyzer ret = new PerFieldAnalyzer((Analyzer) Class.forName(this.getAnalyzerName()).newInstance());
        ArrayList<Column> columns = this.getColumns(true);
        for(int i=0; i< columns.size(); i++) {
            Column c = (Column) columns.get(i);
            if(c.getNeedSynonymsAndStopwords()) {
                if(U.isEmpty(c.getAnalyzerName())) {
                    ret.addAnalyzer(c.getColumnName(), new ProxyAnalyzer(this,(Analyzer) Class.forName(this.getAnalyzerName()).newInstance(),true, true));
                } else {
                    ret.addAnalyzer(c.getColumnName(), new ProxyAnalyzer(this,(Analyzer) Class.forName(c.getAnalyzerName()).newInstance(),true, true));
                }
            } else if(!U.isEmpty(c.getAnalyzerName())) {
                ret.addAnalyzer(c.getColumnName(), (Analyzer) Class.forName(c.getAnalyzerName()).newInstance());
            }
        }
        return (Analyzer) ret;
    }

    static private long MINUTE = 60;
    static private long HOUR = 60*MINUTE;
    static private long DAY = 24*HOUR;
    static private long MONTH = 31*DAY;
    static private long YEAR = 12*MONTH;
    private transient TimeWeight[] InitialTimeWeights = new TimeWeight[] {
            new TimeWeight(10*MINUTE,12),
            new TimeWeight(30*MINUTE,11),
            new TimeWeight(1*HOUR,10),
            new TimeWeight(6*HOUR,9),
            new TimeWeight(12*HOUR,8),
            new TimeWeight(1*DAY,7),
            new TimeWeight(15*DAY,6),
            new TimeWeight(1*MONTH,5),
            new TimeWeight(2*MONTH,4),
            new TimeWeight(3*MONTH,3),
            new TimeWeight(6*MONTH,2),
            new TimeWeight(1*YEAR,1.3f),
            new TimeWeight(2*YEAR,1.2f),
            new TimeWeight(3*YEAR,1.1f),
            new TimeWeight(4*YEAR,1.05f),
            new TimeWeight(5*YEAR,1.04f),
            new TimeWeight(6*YEAR,1.03f),
            new TimeWeight(7*YEAR,1.02f),
            new TimeWeight(10*YEAR,1.01f)
    };
    private ArrayList<TimeWeight> timeWeights = null;
    public TimeWeight[] getTimeWeights() {
    	if(timeWeights==null) return InitialTimeWeights;
    	TimeWeight[] ret = (TimeWeight[]) timeWeights.toArray(new TimeWeight[0]);
        Arrays.sort(ret, new Comparator<TimeWeight>() {         // sort the array
            public int compare(TimeWeight o1, TimeWeight o2) {
              return (int) (((TimeWeight)o1).getTime() - ((TimeWeight)o2).getTime());
            }
          });
        return ret;
    }
    public void addTimeWeight(TimeWeight tw) {
    	if(timeWeights==null){
    		timeWeights = new ArrayList<TimeWeight>();
    	}
    	timeWeights.add(tw);
    }
    
    /**
     * if the "old" dataset already has database connection info and scheduling, we merge the old value into current one. 
     */
    public void merge(DatasetConfiguration old) {
        if(old.getDataSources()!=null && old.getDataSourcesSize()>0) {
            this.dataSources = old.getDataSources();
            this.setDirty(true);
        }
        if(old.getSchedules()!=null && old.getSchedules().size()>0) {
            this.setSchedules(old.getSchedules());
        }
        if(old.getSubscriptionUrl()!=null) {
            this.setSubscriptionUrl(old.getSubscriptionUrl());
        }
    }
    
    public boolean getIsScheduled() {
        for(Schedule s : schedules) {
            if(s.getIsEnabled()) {
                return true;
            }
        }
        return false;
    }

    public Schedule findScheduleByMode(String indexingMode) {
        for(Schedule s : schedules) {
            if(s.getIndexingMode().equals(indexingMode)) {
                return s;
            }
        }
        return null;
    }
    public Schedule findScheduleById(int id) {
        for(Schedule s : schedules) {
            if(s.getId()==id) {
                return s;
            }
        }
        return null;
    }
    
	public boolean isPrefixIndexRootDirectory() {
		return prefixIndexRootDirectory;
	}
	
	public void setPrefixIndexRootDirectory(boolean prefixIndexRootDirectory) {
		this.prefixIndexRootDirectory = prefixIndexRootDirectory;
		isDirty = true;
	}
	
//	public boolean isUseServerDBConnection() {
//		return useServerDBConnection;
//	}
//	
//	public void setUseServerDBConnection(boolean useServerDBConnection) {
//		this.useServerDBConnection = useServerDBConnection;
//		isDirty = true;
//	}
	
	public boolean isUseServerDBConnection() {
		return useServerDBConnection;
	}
	
	public void setUseServerDBConnection(boolean useServerDBConnection) {
		this.useServerDBConnection = useServerDBConnection;
		isDirty = true;
	}
	
	public void clearDataSource() {
		if (this.dataSources != null) {
			this.dataSources.clear();
		}
	}

    private IndexType indexType;

    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
        isDirty = true;
    }

    public void setIndexType(String indexType) {
        this.indexType = IndexType.valueOf(indexType);
        isDirty = true;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    private int numberReplicas = 0;

    public int getNumberReplicas() {
        return numberReplicas;
    }

    public void setNumberReplicas(int numberReplicas) {
        this.numberReplicas = numberReplicas;
        isDirty = true;
    }

    private int numberShards = 1;

    public int getNumberShards() {
        return numberShards;
    }

    public void setNumberShards(int numberShards) {
        this.numberShards = numberShards;
        isDirty = true;
    }

}
