package net.javacoding.xsearch.config.facet;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public abstract class FacetType {
    @XStreamAlias("name")
    @XStreamAsAttribute
    String name;
    @XStreamAlias("applicable-data-type")
    @XStreamAsAttribute
    String applicableDataType;

    public String getName() {
        return name;
    }
    public String getApplicableDataType() {
        return applicableDataType;
    }

}
