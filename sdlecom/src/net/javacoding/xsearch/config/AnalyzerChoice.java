/*
 * Copyright 2004.
 */

package net.javacoding.xsearch.config;

/**
 * Class mapped to the <code>&lt;analyzer&gt;</code> element of a web
 * configuration file.
 *
 */
public class AnalyzerChoice implements ConfigConstants {

    // ------------------------------------------------------------- Properties

    private String name = null;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    private String className = null;
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    private String description = null;
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    private String language = null;
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

}
