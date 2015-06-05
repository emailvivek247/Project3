package com.fdt.sdl.styledesigner;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.config.XMLSerializable;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.styledesigner.htmlelement.HtmlElement;
import com.fdt.sdl.styledesigner.htmlelement.Input;
import com.fdt.sdl.styledesigner.htmlelement.Table;
import com.fdt.sdl.styledesigner.operation.ScaffoldOperation;
import com.fdt.sdl.styledesigner.util.TemplateUtil;
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

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ScaffoldManager {
	
    public static final String PAGE_STYLE_XML = "pagestyle.xml";
    
    public static final String[] avoidedDirectories = new String[] { ".svn", "CVS", ".*\\.pagestyle$" };
    
    private static Logger logger = LoggerFactory.getLogger(ScaffoldManager.class);

    public String getPreviewImageSrc(Scaffold s) {
        for (String ext : new String[] { "jpg", "png", "gif", "bmp" }) {
            if (FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "resources", "images", "stylepreviews", s.longName , "preview." + ext).exists()) {
                return FileUtil.joinBy("/", "resources", "images", "stylepreviews", s.longName, "preview." + ext);
            }
        }
        return "";
    }
    
    public static File getScaffoldRootDirectory() {
        return FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "WEB-INF", "styles");
    }

    public static List<Scaffold> listScaffolds(File aDirectory) {
        File[] subDirs = aDirectory.listFiles(new FileFilter() {
            public boolean accept(File folder) {
                return folder.isDirectory() && FileUtil.resolveFile(folder, PAGE_STYLE_XML).exists();
            }
        });
        File root = getScaffoldRootDirectory();
        List<Scaffold> scaffolds = new ArrayList<Scaffold>(subDirs.length);
        for (File dir : subDirs) {
            Scaffold s = (Scaffold) Scaffold.fromXML(FileUtil.resolveFile(dir, PAGE_STYLE_XML));
            s.longName = getRelativePath(root, dir);
            scaffolds.add(s);
        }
        Scaffold[] tempArray = scaffolds.toArray(new Scaffold[scaffolds.size()]);
        scaffolds.clear();                              // empty the list
        Arrays.sort(tempArray, new Comparator<Scaffold>() { public int compare(Scaffold o1, Scaffold o2) { return o1.order - o2.order; } });
        for(Scaffold s : tempArray) { scaffolds.add(s);}
        return scaffolds;
    }
    private static String getRelativePath(File parent, File child) {
        List<String> list = new ArrayList<String>();
        File p = child;
        while (p != null) {
            if (p.equals(parent)) break;
            list.add(0, p.getName());
            p = p.getParentFile();
        }
        return U.join(list, "/");
    }

    private static File getCachedValuesFile(String indexName, String scaffoldName) {
        return FileUtil.resolveFile(ServerConfiguration.getServerConfiguration().getBaseDirectory(), "log", indexName + "-" + scaffoldName.replace("/", "_") + "-variables.xml");
    }

    private static File getScaffoldDirectory(Scaffold s) {
        return getScaffoldDirectory(s.longName);
    }
    private static File getScaffoldDirectory(String longName) {
    	File filePath = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "WEB-INF", "styles", longName);
        return filePath;
    }

    /*
     * Used when creating the template, showing the scaffold's variables with default values
     */
    public static Scaffold loadScaffold(String dir, DatasetConfiguration dc) {
        Scaffold s = (Scaffold) Scaffold.fromXML(FileUtil.resolveFile(getScaffoldDirectory(dir), PAGE_STYLE_XML));
        s.longName = dir;

        //load previous values from last time, cached in WEB-INF/data/log/<indexName>-<scaffoldName>-variables.xml
        try {
            s.values = (List<ScaffoldValue>) ScaffoldValue.fromXML(getCachedValuesFile(dc.getName(), s.longName));
        } catch (Exception e) {}

        //load columns-variable according to columnCategory, need to load it from the dataset
        s.loadColumnsFromDataset(dc);

        //merge values into variables, to set the default values when creating templates
        if (s.variables != null) {
            for (ScaffoldVariable sVariable : s.getAllVariables()) {
                ScaffoldValue sValue = findScaffoldValue(s.values, sVariable.name);
                if (sValue != null) {
                	loadCachedPageStyleValues(sVariable, sValue);
                } 
                if (sValue != null && sVariable.getType().equals("htmlelement")) {
                	loadHtmlPageStyle(sVariable, sValue);
                }
            }
        }
        return s;
    }
    
    private static void loadHtmlPageStyle(ScaffoldVariable containerVariable,  ScaffoldValue sValue) {
    	if (((HtmlElement)containerVariable).getHtmlElementType().equals("table")) {
    		Table table = (Table)containerVariable;
	    	for(HtmlElement tr: table.getChildElements()) {
	    		for(HtmlElement td: tr.getChildElements()) {
	    			for(ScaffoldVariable scaffoldVariable : td.getChildElements()) {
	                	if (!scaffoldVariable.getType().equals("htmlelement")) {
	                		loadCachedPageStyleValues(scaffoldVariable, sValue);
	                	}
	    			}
	    		}
	    	}
    	}
    }
    
    private static void loadCachedPageStyleValues(ScaffoldVariable sVariable,  ScaffoldValue sValue) {
        if (sVariable instanceof BooleanVariable && sValue instanceof BooleanValue) {
            ((BooleanVariable) sVariable).defaultValue = (BooleanValue) sValue;
        } else if (sVariable instanceof StringVariable && sValue instanceof StringValue) {
            ((StringVariable) sVariable).defaultValue = (StringValue) sValue;
        } else if (sVariable instanceof ColumnVariable && sValue instanceof ColumnValue) {
            ((ColumnVariable) sVariable).defaultValue = (ColumnValue) sValue;
        } else if (sVariable instanceof MultiSelectColumnVariable && sValue instanceof ColumnValue) {
            for(ColumnValue cv : ((MultiSelectColumnVariable) sVariable).defaultValue.columns ) {
                if(cv.columnName.equals(((ColumnValue) sValue).columnName)) {
                    cv.isSelected = ((ColumnValue) sValue).isSelected;
                }
            }
        } else if (sVariable instanceof ColumnsVariable && sValue instanceof ColumnsValue) {
            for(ColumnValue selectedValue : ((ColumnsValue) sValue).columns) {
                for(ColumnValue optionValue : ((ColumnsVariable) sVariable).defaultValue.columns) {
                    if(selectedValue.columnName.equals(optionValue.columnName)) {
                        optionValue.isSelected = true;
                    }
                }
            }
        }
    }
    
    /*
     * Saving scaffold values from what user configured.
     * values is just a flattened list of ScaffoldValue
     */
    public static void saveScaffoldValues(Scaffold s, HttpServletRequest request) {
        if(s.variables==null) return;
        int tableCounterElements = 0;
        List<ScaffoldVariable> variables = s.getAllVariables();
        s.values = new ArrayList<ScaffoldValue>();

        for (int i = 1; i <= variables.size(); i++) {
        	int variableCounter = i + tableCounterElements;
            ScaffoldVariable sVariable = variables.get(i - 1);
            if (sVariable instanceof StringVariable) {
                StringValue stringValue = new StringValue(request.getParameter("variable" + variableCounter));
                stringValue.name = sVariable.name;
                s.values.add(stringValue);
            } else if (sVariable instanceof BooleanVariable) {
                BooleanValue booleanValue = new BooleanValue(U.getBoolean(request.getParameter("variable" + variableCounter), "1", false));
                booleanValue.name = sVariable.name;
                s.values.add(booleanValue);
            } else if (sVariable instanceof ColumnVariable) {
                if( request.getParameter("variable" + variableCounter)==null) continue;
                String[] columnName_columnType = request.getParameter("variable" + variableCounter).split("\\|");
                if (columnName_columnType == null || columnName_columnType.length < 2 || columnName_columnType[1] == null)
                    continue;
                String columnType = columnName_columnType[1].intern();
                //this is when the user select one column
                if (columnType == "Number") {
                    NumberColumnValue numberColumnValue = new NumberColumnValue();
                    numberColumnValue.name = sVariable.name;
                    numberColumnValue.columnName = columnName_columnType[0];
                    numberColumnValue.numberFormat = request.getParameter("numberFormat" + variableCounter);
                    s.values.add(numberColumnValue);
                } else if (columnType == "Timestamp"||columnType=="Date") {
                    DateColumnValue dateColumnValue = new DateColumnValue();
                    dateColumnValue.name = sVariable.name;
                    dateColumnValue.columnName = columnName_columnType[0];
                    dateColumnValue.dateFormat = request.getParameter("dateFormat" + variableCounter);
                    s.values.add(dateColumnValue);
                } else {// if(columnType == "String") {
                    StringColumnValue stringColumnValue = new StringColumnValue();
                    stringColumnValue.name = sVariable.name;
                    stringColumnValue.columnName = columnName_columnType[0];
                    stringColumnValue.highlighted = U.getBoolean(request.getParameter("isHTMLEscaped" + variableCounter), "1", false);
                    stringColumnValue.HTMLEscaped = U.getBoolean(request.getParameter("isHighlighted" + variableCounter), "1", false);
                    stringColumnValue.summarized = U.getBoolean(request.getParameter("isSummarized" + variableCounter), "1", false);
                    s.values.add(stringColumnValue);
                }
            } else if (sVariable instanceof MultiSelectColumnVariable) {
                ColumnValue columnValue = new ColumnValue();
                columnValue.name = sVariable.name;
                columnValue.columnName = request.getParameter("variable" + variableCounter);
                columnValue.isSelected = true;
                s.values.add(columnValue);
            } else if (sVariable instanceof ColumnsVariable) {
                String[] values = request.getParameterValues("variable" + variableCounter);
                if(values==null) continue;
                ColumnsValue columnsValue = new ColumnsValue();
                columnsValue.name = sVariable.name;
                columnsValue.columns = new ArrayList<ColumnValue>();
                for(String v : values) {
                    ColumnValue cv = new ColumnValue();
                    cv.columnName = v;
                    cv.isSelected = true;
                    columnsValue.columns.add(cv);
                }
                s.values.add(columnsValue);
            } else if (sVariable.getType().equals("htmlelement")) {
            	if (((HtmlElement)sVariable).getHtmlElementType().equals("table")) {
            		List tableElements = getTableElementValues((Table)sVariable, request, variableCounter);
            		s.values.addAll(tableElements);
            		tableCounterElements = tableCounterElements + tableElements.size() -1;
            	} else {
	            	String htmlElementValue = request.getParameter("variable" + variableCounter);
	            	if (htmlElementValue == null && ((HtmlElement)sVariable).getHtmlElementType().equalsIgnoreCase("input") && ((Input)sVariable).getInputType().equalsIgnoreCase("checkbox")) {
	            		htmlElementValue = "0";
	            	}
	                StringValue stringValue = new StringValue(htmlElementValue);
	                stringValue.name = sVariable.name;
	                s.values.add(stringValue);
            	}
            }
        }
        XMLSerializable.toXML(s.values, getCachedValuesFile(request.getParameter("indexName"), s.longName));
    }
    
    private static List<StringValue> getTableElementValues(Table table, HttpServletRequest request, int elementCount) {
    	List<StringValue> stringValues = new ArrayList<StringValue>();
    	for(HtmlElement tr: table.getChildElements()) {
    		for(HtmlElement td: tr.getChildElements()) {
    			for(ScaffoldVariable scaffoldVariable : td.getChildElements()) {
                	String requestValue = request.getParameter("variable" + elementCount);
                	if (requestValue == null && scaffoldVariable.getType().equalsIgnoreCase("htmlelement")  && ((HtmlElement)scaffoldVariable).getHtmlElementType().equalsIgnoreCase("input") && ((Input)scaffoldVariable).getInputType().equalsIgnoreCase("checkbox")) {
                		requestValue = "0";
                	}
                    StringValue stringValue = new StringValue(requestValue);
                    stringValue.name = scaffoldVariable.getName();
                    stringValues.add(stringValue);
                    elementCount++;
	
    			}
    		}
    	}
    	return stringValues;
    }

    public static Scaffold loadScaffoldValues(Scaffold s, HttpServletRequest request) {
    	return null;
    }
    private static ScaffoldValue findScaffoldValue(List<ScaffoldValue> values, String name) {
        if (values != null) {
            for (ScaffoldValue sValue : values) {
                if (name != null && sValue.name != null && name.equals(sValue.name)) {
                    return sValue;
                }
            }
        }
        return null;
    }

    private static ScaffoldVariable findScaffoldVariable(List<ScaffoldVariable> variables, String name) {
        if (variables != null) {
            for (ScaffoldVariable sVariable : variables) {
                if (sVariable.name.equals(name)) {
                    return sVariable;
                }
            }
        }
        return null;
    }

    /*
     * Really creating the template based on the scaffold values
     * Since variables has hierarchy, Need to getAllVariables()
     */
    public static void process(Scaffold s, File templateDirectory, DatasetConfiguration dc, String templateName) throws IOException {
        File scaffoldDirectory = new File(getScaffoldDirectory(s), "content");
        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(scaffoldDirectory);
        cfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);

        //set variables in the context
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("dc", dc);
        root.put("templateName", templateName);
        for (ScaffoldVariable sv : s.getAllVariables()) {
            if (sv instanceof BooleanVariable) {
                root.put(sv.name, ((BooleanVariable) sv).getDefaultValue());
            }
        }
        for (ScaffoldValue sv : s.getValues()) {
            if (sv instanceof BooleanValue) {
                root.put(sv.name, ((BooleanValue) sv).value);
            } else if (sv instanceof StringValue) {
                root.put(sv.name, ((StringValue) sv).value);
            } else if (sv instanceof StringColumnValue) {
                root.put(sv.name, docGet((StringColumnValue) sv));
            } else if (sv instanceof NumberColumnValue) {
                root.put(sv.name, docGet((NumberColumnValue) sv));
            } else if (sv instanceof DateColumnValue) {
                root.put(sv.name, docGet((DateColumnValue) sv));
            } else if (sv instanceof ColumnValue) {
                root.put(sv.name, ((ColumnValue) sv).columnName);
            } else if (sv instanceof ColumnsValue) {
                root.put(sv.name, ((ColumnsValue) sv).getColumnNames());
            }
        }

        //copy over stuff except .scaffold
        FileUtil.copyAll(scaffoldDirectory, templateDirectory, avoidedDirectories);
        List<File> files = new ArrayList<File>();
        addScaffoldFilesRecursively(scaffoldDirectory, files);

        //transform .scaffold files
        File baseDirectory = FileUtil.resolveFile(getScaffoldRootDirectory(), s.getLongName());
        for (File f : files) {
            String fileName = f.getAbsolutePath().substring((int) baseDirectory.getAbsolutePath().length()+"/content/".length());
            if(fileName.indexOf("\\")>0) {
                fileName = fileName.replaceAll("\\\\", "/");
            }
            Template template = cfg.getTemplate(fileName);
            String toName = fileName.substring(0, fileName.lastIndexOf(".pagestyle"));
            Writer out = new FileWriter(new File(templateDirectory, toName));
            try {
                template.process(root, out);
            } catch (TemplateException e) {
                logger.warn("Exception when processing:" + f);
                e.printStackTrace();
            }
            out.close();
        }

        //saving template information
        com.fdt.sdl.styledesigner.Template t = TemplateUtil.getTemplate(dc.getName(), templateName);
        t.longname = s.name;
        t.description = s.description;

        for (ScaffoldOperation o : s.getOperations()) {
            o.operate(s,t);
        }

        TemplateUtil.saveTemplate(templateDirectory,t);

    }
    private static void addScaffoldFilesRecursively(File dir, List<File> ret) {
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".pagestyle");
            }
        });
        for(File f:files) {ret.add(f);}
        File[] dirs = dir.listFiles(new FilenameFilter() {
            public boolean accept(File f, String name) {
                if(new File(f,name).isDirectory()) {
                    for(String n : avoidedDirectories) {
                        if(name.equals(n)) return false;
                    }
                    return true;
                }else {
                    return false;
                }
            }
        });
        if(dirs!=null) {
            for(File d : dirs) {
                addScaffoldFilesRecursively(d, ret);
            }
        }
    }
    
    private static String docGet(StringColumnValue scv) {
        String ret = null;
        if(scv.HTMLEscaped) {
            if(scv.summarized) {
                ret = "searchResult.summarize(doc,\""+scv.columnName+"\")";
            }else if(scv.highlighted) {
                ret = "searchResult.highlight(doc,\""+scv.columnName+"\")";
            }else {
                ret = "doc.get(\""+scv.columnName+"\")?html";
            }
        }else {
            if(scv.summarized) {
                ret = "searchResult.directSummarize(doc,\""+scv.columnName+"\")";
            }else if(scv.highlighted) {
                ret = "searchResult.directHighlight(doc,\""+scv.columnName+"\")";
            }else {
                ret = "doc.get(\""+scv.columnName+"\")";
            }
        }
        return "${"+ret+"}";
    }
    private static String docGet(NumberColumnValue ncv) {
        if(!U.isEmpty(ncv.numberFormat)) {
            return "${number.format(\""+ncv.numberFormat+"\",doc.get(\""+ncv.columnName+"\"))}";
        }else {
            return "${doc.get(\""+ncv.columnName+"\")}";
        }
    }
    private static String docGet(DateColumnValue dcv) {
        if(!U.isEmpty(dcv.dateFormat)) {
            if(dcv.dateFormat.equals("relative")){
                return "${date.format(\""+dcv.dateFormat+"\",doc.get(\""+dcv.columnName+"\"))}";
            }else{
                return "${date.format(\""+dcv.dateFormat+"\",doc.getObject(\""+dcv.columnName+"\"))}";
            }
        }else {
            return "${doc.get(\""+dcv.columnName+"\")}";
        }
    }
}
