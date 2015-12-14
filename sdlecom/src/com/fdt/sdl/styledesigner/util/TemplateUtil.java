package com.fdt.sdl.styledesigner.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.FileUtil;

import com.fdt.sdl.styledesigner.Template;

/**
 * Template utility methods.
 *
 */
public class TemplateUtil {

    private final static String TEMPLATE_DIRECTORY = "templates";
    private final static String WORKING_DIRECTORY = "_work";

    private final static String TEMPLATE_PROPERTIES_FILE = "template.xml";

    public final static String TEMPLATE_PREVIEW_IMAGE_REGEX = "preview\\.(?i)(jpg|gif|png)";
   // public final static String TEMPLATE_CONFIG_FILE = "template-config.xml";

    public final static String CACHE_TEMPLATE_FILE = "cache.stl";
    /**
     * Returns the context-relative path of a template, for example,
     * templates/indexName/templateName.
     *
     * @param indexName the name of the index; if <code>null</code>, an example
     *        template path is returned
     * @param templateName the name of the template
     */
    public static String getTemplatePath(String indexName, String templateName, String...fileNames) {
        StringBuffer path = new StringBuffer();
        if (indexName != null) {
            path.append(TEMPLATE_DIRECTORY);
            path.append("/").append(indexName.toLowerCase());
        }
        path.append("/").append(templateName);
        if(fileNames!=null&&fileNames.length>0) {
            path.append("/");
            path.append(FileUtil.joinBy("/", fileNames));
        }
        return path.toString();
    }
    public static String getTemplatePath(String indexName, String templateName) {
        return getTemplatePath(indexName, templateName, new String[] {});
    }

    /**
     * Returns the absolute path of a template file, for example,
     * /templates/indexName/templateName/fileName.
     *
     * @param indexName the name of the index
     * @param templateName the name of the template
     */
    public static String getTemplateFilePath(String indexName, String templateName, String... fileNames) {
        StringBuffer path = new StringBuffer();
        path.append("/").append(getTemplatePath(indexName, templateName));
        path.append("/").append(FileUtil.joinBy("/", fileNames));
        return path.toString();
    }

    /**
     * Returns the context-relative path of the template preview image file, for example,
     * templates/indexName/templateName/preview.gif
     *
     * @param indexName the name of the index
     * @param templateName the name of the template
     */
    public static String getTemplatePreviewPath(String indexName, String templateName) {
        StringBuffer path = new StringBuffer();
        path.append(getTemplatePath(indexName, templateName));

        File dir = getTemplateDirectory(indexName, templateName);
        File[] imgs = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().matches(TEMPLATE_PREVIEW_IMAGE_REGEX) && !pathname.isDirectory();
            }
        });
        if (imgs != null && imgs.length > 0) {
            path.append("/").append(imgs[0].getName());
            return path.toString();
        } else {
            return null;
        }
    }

    /**
     * Returns a template file by name.
     *
     * @param indexName the name of the index
     * @param templateName the name of the template
     * @param fileName the name of the template file
     */
    public static File getTemplateFile(String indexName, String templateName, String... fileNames) {
        return FileUtil.resolveFile(getTemplateDirectory(indexName, templateName), fileNames);
    }
    public static File getTemplateFile(String indexName, String templateName, String fileName) {
        return FileUtil.resolveFile(getTemplateDirectory(indexName, templateName), fileName);
    }
    public static File getTemplateMainFile(String indexName, String templateName) {
        File dir = getTemplateDirectory(indexName, templateName);
        File f;
        if((f=new File(dir,"main.vm")).exists()) return f;
        if((f=new File(dir,"main.jsp")).exists()) return f;
        return new File(dir,"main.stl") ;
    }

    /**
     * Returns all files for a template.
     * 
     * @param indexName the name of the index
     * @param templateName the name of the template
     */
    public static File[] getTemplateFiles(String indexName, String templateName) {
        return listFiles(getTemplateDirectory(indexName, templateName));
    }
    public static File[] getTemplateFiles(String indexName, String templateName, String dir) {
        File templateDirectory = getTemplateDirectory(indexName, templateName);
        File directory = (dir==null? null : FileUtil.resolveFile(templateDirectory, dir.split("/|\\||\\\\")));
        if(directory!=null && FileUtil.isIncluded(templateDirectory, directory)){
            return listFiles(directory);
        }else{
            return listFiles(templateDirectory);
        }
    }
    public static File[] listFiles(File directory){
        File[] files = directory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return !TEMPLATE_PROPERTIES_FILE.equalsIgnoreCase(pathname.getName());
            }
        });
        Arrays.sort(files, new Comparator<File>(){
            public int compare(File a, File b) {
                return (a.getName().compareTo(b.getName()));
            }});
        return files;
    }

    /**
     * Returns the template directory.
     *
     * @param indexName the name of the index
     * @param templateName the name of the template
     */
    public static File getTemplateDirectory(String indexName, String templateName) {
        StringBuffer path = new StringBuffer();
        path.append(WebserverStatic.getRootDirectory());
        path.append(getTemplatePath(indexName, templateName));
        return new File(path.toString());
    }

    /**
     * Returns the template object by name.
     *
     * @param indexName the name of the index
     * @param templateName the name of the template
     * @exception IOException if an input/output error occurs
     */
    public static Template getTemplate(String indexName, String templateName) 
            throws IOException {
        return getTemplate(getTemplateDirectory(indexName, templateName));
    }

    /**
     * Returns the template object in a given directory.
     *
     * @param dir the template directory
     * @exception IOException if an input/output error occurs
     */
    public static Template getTemplate(File dir) throws IOException {
        File f = FileUtil.resolveFile(dir, TEMPLATE_PROPERTIES_FILE);
        Template t = null;
        if(f.exists()) {
            t = Template.digestFromXML(f);
        }
        if(t==null) {
            t = new Template();
        }
        t.name = dir.getName();
        t.directory = dir;
        return t;
    }

    /**
     * Returns an array of all templates for an index.
     *
     * @param indexName the name of the index
     * @exception IOException if an input/output error occurs
     */
    public static Template[] getTemplates(String indexName) throws IOException {
        ArrayList templates = new ArrayList();
        StringBuffer path = new StringBuffer();
        path.append(WebserverStatic.getRootDirectory());
        if (indexName != null) {
            path.append(TEMPLATE_DIRECTORY);
            path.append("/").append(indexName.toLowerCase());
        }
        File dir = new File(path.toString());
        File[] subDirs = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if (subDirs != null) {
            for (int i = 0; i < subDirs.length; i++) {
                File[] files = subDirs[i].listFiles(new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.getName().toLowerCase().startsWith("main") && !pathname.isDirectory();
                    }
                });
                if (files != null && files.length > 0) {
                    templates.add(getTemplate(subDirs[i]));
                }
            }
        }

        Template results[] = new Template[templates.size()];
        return ((Template[])templates.toArray(results));
    }

    /**
     * Stores the template to a properties file.
     *
     * @param indexName the name of the index
     * @param t the template to be saved
     */
    public static void saveTemplate(String indexName, Template t) throws IOException {
        File f = FileUtil.resolveFile(getTemplateDirectory(indexName, t.getName()), TEMPLATE_PROPERTIES_FILE);
        t.toXML(f);
    }
    /*
     * templateDirectory is the template root directory
     * templateDirectory/indexName/templateName
     */
    public static void saveTemplate(File templateDirecotry, Template t) throws IOException {
        File f = FileUtil.resolveFile(templateDirecotry, TEMPLATE_PROPERTIES_FILE);
        t.toXML(f);
    }

    public static File getTemplateDirectory(String indexName) {
        return new File(WebserverStatic.getRootDirectory()+TemplateUtil.TEMPLATE_DIRECTORY+File.separator+indexName);
    }
}
