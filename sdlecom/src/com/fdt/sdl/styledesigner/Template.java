package com.fdt.sdl.styledesigner;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.digester.RuleSetBase;
import org.xml.sax.SAXException;

import net.javacoding.xsearch.config.XMLSerializable;
import net.javacoding.xsearch.utility.FileUtil;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Describes a search template.
 *
 */
@XStreamAlias("template")
public class Template extends XMLSerializable{

    /** The short name of the template. */
    public transient String name = null;
    /** The long name of the template. */
    @XStreamAlias("longname")
    public String longname = null;
    /** The description of the template. */
    @XStreamAlias("description")
    public String description = null;
    /** The default results per page. */
    @XStreamAlias("defaultLength")
    public Integer defaultLength= null;
    /** The directory file object */
    public transient File directory;
    
    public Template() {
		super();
	}

	private static RuleSet digesterRuleSet = new RuleSetBase() {
        @Override
        public void addRuleInstances(Digester digester) {
            digester.addBeanPropertySetter("template/longname", "longname");
            digester.addBeanPropertySetter("template/description", "description");
            digester.addBeanPropertySetter("template/defaultLength", "defaultLength");
        }
    };

    public String getName() { return name; }
    public String getLongname() { return longname; }
    public String getDescription() { return description; }
    public Integer getDefaultLength() { return defaultLength; }

    public void setLongname(String longname) {
        this.longname = longname;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setDefaultLength(Integer defaultLength) {
        this.defaultLength = defaultLength;
    }

    public Directory findCurrentDirectory(String path){
        if(path!=null){
            File dir = FileUtil.resolveFile(directory, path.split("/|\\\\"));
            if(FileUtil.isIncluded(directory, dir)){
                Stack<String> paths = new Stack<String>();
                File p = dir;
                while (p != null) {
                    if (p.equals(directory)) break;
                    paths.push(p.getName());
                    p = p.getParentFile();
                }
                String[] directoryPath = new String[paths.size()];
                int i = 0;
                while(paths.size()>0){
                    directoryPath[i++] = paths.pop();
                }
                Directory d = new Directory();
                d.path = directoryPath;
                return d;
            }else{
                return null;
            }
        }else {
            Directory d = new Directory();
            d.path = new String[]{""};
            return d;
        }
    }

    public class Directory{
        public String[] path;
        public String[] getPath(){ return path;}
        public Directory[] getPathDirectories(){
            Directory[] dirs = new Directory[path.length];
            for(int i=0;i<dirs.length;i++){
                dirs[i] = new Directory();
                dirs[i].path = new String[i+1];
                for(int j=0;j<i+1;j++){
                    dirs[i].path[j] = path[j];
                }
            }
            return dirs;
        }
        public String toString(){ return FileUtil.joinBy(File.separator,path); }
    }
    
    public static Template digestFromXML(File file) {
        Digester digester = new Digester();
        digester.push(new Template());
        digester.addRuleSet(digesterRuleSet);
        try {
            return (Template) digester.parse(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null;
    }
}
