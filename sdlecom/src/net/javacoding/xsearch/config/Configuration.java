/*
 * Copyright 2004.
 */

package net.javacoding.xsearch.config;

import java.io.File;
import java.io.IOException;

import net.javacoding.xsearch.core.exception.ConfigurationException;
import net.javacoding.xsearch.utility.FileUtil;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Base class for configuration.
 */
public abstract class Configuration extends XMLSerializable implements ConfigConstants {

    private static final long serialVersionUID = 3942166619045761606L;

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    /** The digester used to pass the configuration file. */
    protected transient Digester digester = null;

    /** The dataset configuration file. */
    protected transient File configFile = null;

    /** The last modified time of the configuration file. */
    protected transient long lastModified = 0L;

    protected transient boolean isDirty = false;

    // <code>false</code> if the configuration file is corrupt.
    protected transient boolean isValid = false;

    /**
     * Initializes this object by parsing the configuration file.
     *
     * @throws ConfigurationException
     */
    protected void init() throws ConfigurationException {

        if (configFile == null) {
            setValid(false);
            throw new ConfigurationException("Configuration file must not be null");
        }

        Digester d = initDigester();
        d.push(this);
        try {
            d.parse(configFile);
            setValid(true);
        } catch (IOException e) {
            System.out.println(configFile);
            setValid(false);
            throw new ConfigurationException(e.getMessage());
        } catch (SAXException e) {
            setValid(false);
            throw new ConfigurationException(e.getMessage());
        } finally {
            try {
                if (d != null) {
                    d.clear();
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        lastModified = configFile.lastModified();
        isDirty = false;
    }

    /**
     * Create (if needed) and return a new Digester instance that has been
     * initialized to parse a configuraiton file (an instance of the
     * configuration object must be pushed on to the evaluation stack before
     * parsing begins).
     */
    protected Digester initDigester() {
        if (digester != null) {
            return digester;
        }

        // Create a new Digester instance with standard capabilities
        digester = new Digester();
        digester.setNamespaceAware(true);
        //digester.setValidating(true);
        //digester.setUseContextClassLoader(true);
        try {
            digester.addRuleSet((org.apache.commons.digester.RuleSet)
                    Class.forName(this.getClass().getName()+"RuleSet").newInstance());
        } catch (Exception e) {
            return null;
        }

        return digester;
    }

    /**
     * Sets the dataset configuration file.
     *
     * @param configFile the dataset configuration file
     */
    public void setConfigFile(File configFile) {
        if (this.configFile == null || this.configFile.compareTo(configFile) != 0) {
            this.configFile = configFile;
            lastModified = 0L;
        }
    }

    public File getConfigFile() {
        return this.configFile;
    }

    /**
     * Delete the dataset configuration file.
     */
    public boolean deleteConfigFile() {
        return configFile.delete();
    }

    /**
     * Writes the dataset configuration file.
     */
    public void writeConfigFile() throws IOException {
        FileUtil.writeFile(configFile, this.toString(), "UTF8");
        lastModified = configFile.lastModified();
        isDirty = false;
        setValid(true);
    }

    /**
     * Loads the object from configuration file.
     */
    public void load() throws ConfigurationException {
        init();
    }

    /**
     * Loads the object from configuration file.
     *
     * @param configFile the configuration file
     */
    public void load(File configFile) throws ConfigurationException {
        setConfigFile(configFile);
        init();
    }

    /**
     * Saves the object to configuration file.
     */
    public void save() throws IOException {
        writeConfigFile();
    }

    /**
     * Changes the state of the object.
     */
    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    /**
     * Returns <code>true</code> if the object contains changes that have not
     * been saved to the configuration file.
     */
    public boolean isDirty() {
        return isDirty;
    }

    /**
     * Returns <code>true</code> if the object have become out of sync with the
     * recently modified configuration file.
     */
    public boolean isStale() {
        if (configFile == null) {
            return false;
        }
        return (lastModified < configFile.lastModified());
    }

    /**
     * Changes the state of the object.
     */
    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * Returns <code>false</code> if the configuration file is corrupt.
     */
    public boolean isValid() {
        return isValid;
    }

    public boolean exists() {
        return (configFile != null && configFile.exists());
    }

    public boolean canRead() {
        return (configFile != null && configFile.canRead());
    }

    public boolean canWrite() {
        return (configFile != null && configFile.canWrite());
    }
}
