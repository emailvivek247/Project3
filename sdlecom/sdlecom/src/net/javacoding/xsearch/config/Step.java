package net.javacoding.xsearch.config;

/**
 * 
 *
 * describe each step
 */
public class Step implements ConfigConstants {
    
    private String name = null;
    private boolean repeatable = true;
    private String action = null;
    private String description = null;
    private boolean optional = false;

    public String getAction() {
        return action;
    }
    public String getDefaultAction() {
        if(action!=null&&action.indexOf(",")>0) {
            return action.split(",")[0];
        }
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isOptional() {
        return optional;
    }
    public void setOptional(boolean optional) {
        this.optional = optional;
    }
    public boolean getRepeatable() {
        return repeatable;
    }
    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }
}
