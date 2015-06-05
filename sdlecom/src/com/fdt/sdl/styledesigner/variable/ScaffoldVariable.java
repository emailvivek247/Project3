package com.fdt.sdl.styledesigner.variable;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public abstract class ScaffoldVariable {
    @XStreamAlias("name")
	@XStreamAsAttribute
	public String name;
    @XStreamAlias("prompt")
	public String prompt;
    @XStreamAlias("description")
	public String description;
    @XStreamAsAttribute
	public String disabled;
    @XStreamAsAttribute
	public String className;    
    @XStreamAsAttribute
	public String size; 
    
    @XStreamAlias("children")
    public List<ScaffoldVariable> children;

    public String getName() {
		return name;
	}
	public String getClassName() {
		return className;
	}
    public String getPrompt() {
		return prompt;
	}
	public String getDescription() {
		return description;
	}
    public List<ScaffoldVariable> getChildren() {
        return children;
    }
	public String getDisabled() {
		return disabled;
	}    
	public String getType() {
		String className = this.getClass().getName();
		String shortName = className.substring(className.lastIndexOf(".")+1);
		return shortName.substring(0, shortName.length()-"Variable".length()).toLowerCase();
	}

}
