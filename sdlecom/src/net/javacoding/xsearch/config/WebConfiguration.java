/*
 * Copyright 2004.
 */

package net.javacoding.xsearch.config;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.javacoding.xsearch.connection.JdbcToolkit;
import net.javacoding.xsearch.core.exception.ConfigurationException;
import com.fdt.sdl.core.parser.FileParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class mapped to the <code>&lt;xsearch-web&gt;</code> root element of the web
 * configuration file.
 *
 */
public class WebConfiguration extends Configuration {

    // ----------------------------------------------------- Instance Variables

    private static Logger logger = LoggerFactory.getLogger(WebConfiguration.class);

    /** The LOV of analyzers. */
    private Map<String, AnalyzerChoice> analyzers = null;

    /** The LOV of similarities. */
    private List<SimilarityChoice> similarities = null;

    /** The LOV of database column's java class names. */
    private Map<String, String> columnClassNames = null;

    /** The LOV of index field types. */
    private Map<String, String> indexFieldTypes = null;

    /** The wizard with several steps. */
    private Wizard wizard = null;

    private List<String> dateFormats = null;
    private List<String> numberFormats = null;
    
    private Map<String, String> commands = null;

    
    // ----------------------------------------------------------- Constructors

    public WebConfiguration(File configFile) throws ConfigurationException {
        analyzers = new LinkedHashMap<String, AnalyzerChoice>();
        similarities = new ArrayList<SimilarityChoice>();
        columnClassNames = new LinkedHashMap<String, String>();
        indexFieldTypes = new LinkedHashMap<String, String>();
        dateFormats = new ArrayList<String>();
        numberFormats = new ArrayList<String>();
        commands = new LinkedHashMap<String, String>();
        this.configFile = configFile;
        init();
    }

    // --------------------------------------------------------- Public Methods

    public void init() throws ConfigurationException {
        analyzers.clear();
        similarities.clear();
        columnClassNames.clear();
        indexFieldTypes.clear();
        dateFormats.clear();
        numberFormats.clear();
        commands.clear();
        super.init();
    }

    /**
     * Adds an item to the analyzer LOV
     */
    public void addAnalyzerChoice(AnalyzerChoice analyzer) {
        if (analyzer != null) {
            analyzers.put(analyzer.getName(), analyzer);
        }
    }

    /**
     * Returns the analyzer LOV
     */
    public Map<String, AnalyzerChoice> getAnalyzers() {
        loadIfUpdated();
        return analyzers;
    }

    public void addSimilarityChoice(SimilarityChoice similarity) {
        if (similarity != null) {
        	similarities.add(similarity);
        }
    }
    public List<SimilarityChoice> getSimilarityChoices() {
        loadIfUpdated();
        return similarities;
    }

    /**
     * Adds an item to the column class name LOV
     */
    public void addColumnClassName(String name, String value) {
        columnClassNames.put(name, value);
    }

    /**
     * Returns the column class name LOV
     */
    public Map<String, String> getColumnClassNames() {
        loadIfUpdated();
        return columnClassNames;
    }

    /**
     * Adds an item to the index field type LOV
     */
    public void addIndexFieldType(String name, String value) {
        indexFieldTypes.put(name, value);
    }

    /**
     * Returns the index field type LOV
     */
    public Map<String, String> getIndexFieldTypes() {
        loadIfUpdated();
        return indexFieldTypes;
    }

    /**
     * Returns the jdbc driver LOV
     */
    public List<JdbcDriverInfo> getJdbcdrivers() {
        return JdbcToolkit.listAndTestDriver();
    }

    public JdbcDriverInfo getJdbcdriver(String className, String driverName) {
        JdbcDriverInfo driver = null;
        List<JdbcDriverInfo> drivers = getJdbcdrivers();
        for (JdbcDriverInfo d : drivers) {
            if (d != null && d.getClassName().equals(className)) {
                if (d.getName().equals(driverName)) {
                    return d;
                } else {
                    if (driver == null) driver = d;
                }
            }
        }
        // if no driver matches driverName, return the first driver that matches className
        return driver;
    }

    public Wizard getWizard() {
        loadIfUpdated();
        return wizard;
    }
    public void setWizard(Wizard wizard) {
        this.wizard = wizard;
    }

    public void addDateFormat(String dateFormat) {
        dateFormats.add(dateFormat);
    }

    public List<String> getDateFormats() {
        loadIfUpdated();
        return dateFormats;
    }

    public void addNumberFormat(String numberFormat) {
        numberFormats.add(numberFormat);
    }
    
    public List<String> getNumberFormats() {
        loadIfUpdated();
        return numberFormats;
    }

    private void loadIfUpdated(){
        if(isStale()){
            try {
                init();
            } catch (ConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    public void addCommand(String name, String cmd) {
        StringBuilder sb = new StringBuilder(cmd.length());
        for(String s : cmd.split(" ")){
            if(s!=null && s.length()>0){
                sb.append(s);
                sb.append(" ");
            }
        }
        cmd = sb.toString();
        commands.put(name, cmd.trim());
    }
    /**
     * @return Returns the indexingCommands.
     */
    public Map<String, String> getCommands() {
        loadIfUpdated();
        return commands;
    }
    public String getCommand(String name) {
        loadIfUpdated();
        return commands.get(name);
    }
    
}
