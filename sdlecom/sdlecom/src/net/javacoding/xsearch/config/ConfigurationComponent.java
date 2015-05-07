package net.javacoding.xsearch.config;


public abstract class ConfigurationComponent extends XMLSerializable {

	/** The configuration object associated with. */
	protected transient Configuration configObject = null;

	public ConfigurationComponent() {
		super();
	}

	public void setConfigObject(Configuration configObject) {
	    this.configObject = configObject;
	}

	public Configuration getConfigObject() {
	    return configObject;
	}
	
	public void setDirty(){
        if (configObject != null) configObject.setDirty(true);
	}
}
