package com.fdt.sdl.core.ui.action.indexing.status;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.IndexSubscriber;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.U;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fdt.sdl.util.SecurityUtil;

public class DownloadIndexAction extends Action {
    public static final String DOWNLOAD_FILE = "downloadFile";
    public static final String LIST_FILES = "listFiles";
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        List<String> errs = new ArrayList<String>();
        
        String indexName = request.getParameter("indexName");
        String fileName = request.getParameter("fileName");
        String operation = request.getParameter("operation");
        String dir = request.getParameter("dir");

        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            if (!SecurityUtil.isAllowed(request,dc)) {
                errs.add("Security Error: User is not authenticated or not on the allowed IP list for index "+indexName);
                request.setAttribute("errs", errs);
                return (mapping.findForward("error"));
            }

            if(DOWNLOAD_FILE.equals(operation)&&!U.isEmpty(fileName)) {
                ServletOutputStream out = response.getOutputStream();
                File file = FileUtil.resolveFile(getServerDownloadBaseDirectory(dir,dc), fileName);
                response.setHeader("Content-Disposition","inline; filename="+file.getName());
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                try {
                    byte[] buffer = new byte[64*1024];
                    int count;
                    while ((count = bis.read(buffer)) != -1) {
                        out.write(buffer, 0, count);
                    }
                    out.flush();
                    out.close();
                }finally {
                    if(bis!=null)bis.close();
                }
            }else if(LIST_FILES.equals(operation)) {
                File directoryFile = getServerDownloadBaseDirectory(dir, dc);
                listFiles(dir,directoryFile,response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errs.add("Error during downloading index for "+indexName);
            return (mapping.findForward("error"));
        }
        return null;
    }
    private void listFiles(String dirName, File dir, HttpServletResponse response) throws IOException {
        ServletOutputStream out = response.getOutputStream();
        response.setContentType("text/html");
        try {
            File[] files = dir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isFile();
                }});
            for (int i = 0;i<files.length;i++){
                out.print(dirName+" "+files[i].getName()+" "+files[i].lastModified()+" "+files[i].length()+"\n");
            }
        }catch(Exception e) {
            out.println("No Such Index");
        }
        out.flush();
        out.close();
    }
    //used on server side only
    private static File getServerDownloadBaseDirectory(String dir, DatasetConfiguration dc) {
        File directoryFile = null;
        if(IndexSubscriber.SUBSCRIBED_MAIN_DIRECTORY.equals(dir)) {
            directoryFile = IndexStatus.findActiveMainDirectoryFile(dc);
        }else if(IndexSubscriber.SUBSCRIBED_TEMP_DIRECTORY.equals(dir)) {
            directoryFile = IndexStatus.findActiveTempDirectoryFile(dc);
        }else if(IndexSubscriber.SUBSCRIBED_DICTIONARY_DIRECTORY.equals(dir)) {
            directoryFile = dc.getDictionaryIndexDirectoryFile();
        }else if(IndexSubscriber.SUBSCRIBED_PHRASE_DIRECTORY.equals(dir)) {
            directoryFile = dc.getPhraseIndexDirectoryFile();
        }
        return directoryFile;
    }
    public static File getDownloadBaseDirectory(String dir, DatasetConfiguration dc) {
        File directoryFile = null;
        if(IndexSubscriber.SUBSCRIBED_MAIN_DIRECTORY.equals(dir)) {
            directoryFile = IndexStatus.findNonActiveMainDirectoryFile(dc);
        }else if(IndexSubscriber.SUBSCRIBED_TEMP_DIRECTORY.equals(dir)) {
            directoryFile = IndexStatus.findNonActiveTempDirectoryFile(dc);
        }else if(IndexSubscriber.SUBSCRIBED_DICTIONARY_DIRECTORY.equals(dir)) {
            directoryFile = dc.getDictionaryIndexDirectoryFile();
        }else if(IndexSubscriber.SUBSCRIBED_PHRASE_DIRECTORY.equals(dir)) {
            directoryFile = dc.getPhraseIndexDirectoryFile();
        }
        return directoryFile;
    }
}
