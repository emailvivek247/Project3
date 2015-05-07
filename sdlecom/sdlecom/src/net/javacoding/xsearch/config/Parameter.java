/*
 * Copyright 2004.
 */

package net.javacoding.xsearch.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.javacoding.xsearch.utility.DBTool;

/**
 * Class mapped to the <code>&lt;parameter&gt;</code> element of a dataset
 * configuration file. It describes a parameter in an SQL statement.
 *
 */
@XStreamAlias("parameter")
public class Parameter implements ConfigConstants {

	public Parameter() {
		super();
	}

	/** The column name of the parameter. */
    private String name = null;
    public String getName() { return name; }
    public void setName(String name) {
        this.name = name ==null ? null : name.intern();
        if (configObject != null) configObject.setDirty(true);
    }

    /** The index of the parameter starting from 1.*/
    private int index;
    public int getIndex() { return index; }
    public void setIndex(int index) {
        this.index = index;
        if (configObject != null) configObject.setDirty(true);
    }

    /** The value of the parameter. */
    private Object value = null;
    public Object getValue() { return value; }
    public void setValue(Object value) {
        this.value = value;
        if (configObject != null) configObject.setDirty(true);
    }
    public void setValue(String type, String valueStr) {
        value = DBTool.createObject(type, valueStr);
        if (configObject != null) configObject.setDirty(true);
    }
    public String getType() { return value.getClass().getName(); }
    
    @XStreamAlias("is-variable-binding")
    private boolean isVariableBinding = true;
    public boolean getIsVariableBinding() {
        return isVariableBinding;
    }
    public void setIsVariableBinding(boolean isVariableBinding) {
        this.isVariableBinding = isVariableBinding;
        if (configObject != null) configObject.setDirty(true);
    }

    // --------------------------------------------------------- Public Methods

    /** The configuration object associated with. */
    protected Configuration configObject = null;
    public void setConfigObject(Configuration configObject) {
        this.configObject = configObject;
    }
    public Configuration getConfigObject() {
        return configObject;
    }

    /**
     * Returns an XML representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("    <parameter>\n");
        if (name != null) {
            sb.append("      <name>").append(name).append("</name>\n");
        }
        sb.append("      <index>").append(index).append("</index>\n");
        if (!isVariableBinding) {
            sb.append("      <is-variable-binding>").append(isVariableBinding).append("</is-variable-binding>\n");
        }
        if (value != null) {
            sb.append("      <type>").append(value.getClass().getName()).append("</type>\n");
            sb.append("      <value>").append(value.toString()).append("</value>\n");
        }
        sb.append("    </parameter>\n");
        return sb.toString();
    }

}
