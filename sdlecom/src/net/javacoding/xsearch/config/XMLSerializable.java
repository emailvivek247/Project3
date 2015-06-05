package net.javacoding.xsearch.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.javacoding.xsearch.config.facet.FacetType;
import net.javacoding.xsearch.config.facet.FacetTypes;
import net.javacoding.xsearch.config.facet.NumberFacet;
import net.javacoding.xsearch.config.facet.NumberRange;

import com.fdt.sdl.license.License;
import com.fdt.sdl.styledesigner.htmlelement.HtmlElement;
import com.fdt.sdl.styledesigner.htmlelement.Img;
import com.fdt.sdl.styledesigner.htmlelement.Input;
import com.fdt.sdl.styledesigner.htmlelement.Label;
import com.fdt.sdl.styledesigner.htmlelement.Option;
import com.fdt.sdl.styledesigner.htmlelement.Select;
import com.fdt.sdl.styledesigner.htmlelement.Separator;
import com.fdt.sdl.styledesigner.htmlelement.Table;
import com.fdt.sdl.styledesigner.htmlelement.Td;
import com.fdt.sdl.styledesigner.htmlelement.TextArea;
import com.fdt.sdl.styledesigner.htmlelement.Tr;
import com.fdt.sdl.styledesigner.operation.IncludeOperation;
import com.fdt.sdl.styledesigner.operation.RequireOperation;
import com.fdt.sdl.styledesigner.operation.ScaffoldOperation;
import com.fdt.sdl.styledesigner.value.BooleanValue;
import com.fdt.sdl.styledesigner.value.ColumnValue;
import com.fdt.sdl.styledesigner.value.ColumnsValue;
import com.fdt.sdl.styledesigner.value.DateColumnValue;
import com.fdt.sdl.styledesigner.value.NumberColumnValue;
import com.fdt.sdl.styledesigner.value.ScaffoldValue;
import com.fdt.sdl.styledesigner.value.StringColumnValue;
import com.fdt.sdl.styledesigner.value.StringValue;
import com.fdt.sdl.styledesigner.variable.BooleanVariable;
import com.fdt.sdl.styledesigner.variable.ColumnVariable;
import com.fdt.sdl.styledesigner.variable.ColumnsVariable;
import com.fdt.sdl.styledesigner.variable.MultiSelectColumnVariable;
import com.fdt.sdl.styledesigner.variable.ScaffoldVariable;
import com.fdt.sdl.styledesigner.variable.StringVariable;
import net.javacoding.xsearch.core.PeriodTable;
import net.javacoding.xsearch.fetch.FetcherInfo;
import net.javacoding.xsearch.foundation.QuerySampleValues;
import net.javacoding.xsearch.foundation.WebserverHttpInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.styledesigner.Scaffold;
import com.fdt.sdl.styledesigner.Template;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public abstract class XMLSerializable implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(XMLSerializable.class);

    public XMLSerializable() {
        super();
    }

    public String toXML() {
        XStream xstream = createXStream();
        xstream.processAnnotations(processClasses());
        return xstream.toXML(this);
    }

    public static String toXML(List<XMLSerializable> list) {
        if (list == null || list.size() == 0)
            return "";
        XStream xstream = createXStream();
        xstream.processAnnotations(list.get(0).processClasses());
        return xstream.toXML(list);
    }

    public String toJson() {
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.processAnnotations(processClasses());
        return xstream.toXML(this);
    }

    public static String toJson(List<XMLSerializable> list) {
        if (list == null || list.size() == 0)
            return "";
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.processAnnotations(list.get(0).processClasses());
        return xstream.toXML(list);
    }

    public Class[] processClasses() {
        return getPakcageClasses();
    }

    public void toXML(File theFile) {
        try {
            PrintWriter out;
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(theFile),"UTF8")));
            out.println(toXML());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void toXML(List list, File theFile) {
        try {
            PrintWriter out;
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(theFile),"UTF8")));
            out.println(toXML(list));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(File theFile) {
        toXML(theFile);
    }

    public static Object fromXML(String input) {
        XStream xstream = createXStream();
        xstream.processAnnotations(getPakcageClasses());
        return xstream.fromXML(input);
    }

    public static Object fromXML(InputStream input) {
        XStream xstream = createXStream();
        xstream.processAnnotations(getPakcageClasses());
        return xstream.fromXML(input);
    }

    public static Object fromXML(File file) {
        FileInputStream input = null;
        Object obj = null;
        try {
            input = new FileInputStream(file);
            XStream xstream = createXStream();
            xstream.processAnnotations(getPakcageClasses());
            obj = xstream.fromXML(input);
            return obj;
        } catch (com.thoughtworks.xstream.io.StreamException s) {
        	System.out.println(s.getMessage());
            logger.warn("Failed to read file:" + file + s.getMessage());
            return null;
        } catch (Exception e) {
        } finally {
            if(input!=null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    private static XStream createXStream() {
        return new XStream() {

            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {

                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        return definedIn != Object.class ? super.shouldSerializeMember(definedIn, fieldName) : false;
                    }

                };
            }

        };
    }

    public static Class[] getPakcageClasses() {
        List<Class> ret = new ArrayList<Class>();

        ret.add(License.class);

        //not really in use now
        ret.add(DatasetConfiguration.class);
        ret.add(ServerConfiguration.class);
        ret.add(WebConfiguration.class);
        ret.add(Column.class);
        ret.add(Dataquery.class);
        ret.add(ContentDataquery.class);
        ret.add(DeletionDataquery.class);
        ret.add(IncrementalDataquery.class);
        ret.add(WorkingQueueDataquery.class);
        ret.add(DataSource.class);
        ret.add(FetcherInfo.class);
        
        //added for column history
        ret.add(ConfigurationHistory.class);

        ret.add(JdbcDriverInfo.class);
        ret.add(PeriodTable.class);
        ret.add(QuerySampleValues.class);
        
        ret.add(Scaffold.class);
        
        ret.add(ScaffoldValue.class);
        ret.add(BooleanValue.class);
        ret.add(ColumnsValue.class);
        ret.add(ColumnValue.class);
        ret.add(DateColumnValue.class);
        ret.add(NumberColumnValue.class);
        ret.add(StringColumnValue.class);
        ret.add(StringValue.class);

        ret.add(ScaffoldVariable.class);
        ret.add(BooleanVariable.class);
        ret.add(ColumnsVariable.class);
        ret.add(ColumnVariable.class);
        ret.add(StringVariable.class);
        ret.add(MultiSelectColumnVariable.class);
        
        //Html Classes
        ret.add(HtmlElement.class);
        ret.add(Select.class);
        ret.add(Option.class);
        ret.add(Label.class);
        ret.add(Input.class);
        ret.add(Table.class);
        ret.add(Tr.class);
        ret.add(Td.class);
        ret.add(TextArea.class);
        ret.add(Separator.class);
        ret.add(Img.class);
        
        ret.add(ScaffoldOperation.class);
        ret.add(IncludeOperation.class);
        ret.add(RequireOperation.class);

        //need to add classes like FilterResult, etc
        ret.add(net.javacoding.xsearch.search.result.SearchResult.class);

        ret.add(Template.class);
        ret.add(WebserverHttpInfo.class);
        
        ret.add(FacetTypes.class);
        ret.add(FacetType.class);
        ret.add(NumberFacet.class);
        ret.add(NumberRange.class);
        Class[] classesA = new Class[ret.size()];
        ret.toArray(classesA);
        return classesA;
    }
}
