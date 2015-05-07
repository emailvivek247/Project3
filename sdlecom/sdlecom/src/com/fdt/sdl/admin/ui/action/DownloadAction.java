package com.fdt.sdl.admin.ui.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.foundation.WebserverStatic;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.styledesigner.util.TemplateUtil;
import com.fdt.sdl.util.SecurityUtil;

public class DownloadAction extends Action {
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request)) return (mapping.findForward("welcome"));
        String[] indexNames = request.getParameterValues("indexesToDownload");
        if(indexNames!=null&&indexNames.length==1) {
            indexNames = indexNames[0].split(",");
        }
        DatasetConfiguration[] dcs = null;
        ActionMessages errors = new ActionMessages();
        String target = "continue";
        
        ZipOutputStream outStream = new ZipOutputStream(response.getOutputStream());
        outStream.setLevel(Deflater.BEST_SPEED);
        try {
            dcs = ServerConfiguration.getDatasetConfigurations(indexNames);
            if (dcs == null) return mapping.findForward("error");

            String fileName = "";
            for(int i=0;i<indexNames.length;i++) {
                fileName+="-"+indexNames[i];
            }

            response.setContentType("multipart/x-zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"index" + fileName+".zip\"");
            response.addHeader("Content-description", "Index Definitions And Templates");

            for (int i = 0; i < dcs.length; i++) {
                try {
                    addToZip(dcs[i].getConfigFile(), outStream);
                    addToZip(TemplateUtil.getTemplateDirectory(dcs[i].getName()),outStream);
                } catch (ZipException e) {
                    target = "error";
                }
            }
            outStream.close();
            outStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            errors.add("error", new ActionMessage("action.showIndexStatus.index.error", indexNames));
            saveErrors(request, errors);
            return (mapping.findForward("error"));
        }
        return null;
    }


    /**
     * Helper method which recursively zips and adds files and sub-directories to a
     * zip output stream.
     *
     * @param   file File representing a file or directory to add
     * @param   out open zip output stream to add to
     */
    private void addToZip(File file, ZipOutputStream out) throws FileNotFoundException, java.io.IOException {
        // if file is a file, add to zip
        if (file.exists() && file.isFile()) {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            try {
                ZipEntry ze = new ZipEntry(getFileZipName(file.getPath()));
                out.putNextEntry(ze);

                // read/write file to zip
                byte[] data = new byte[1024];
                int byteCount;

                while ((byteCount = bis.read(data, 0, 1024)) > -1) {
                    out.write(data, 0, byteCount);
                }
            }finally {
                if(bis!=null)bis.close();
            }
        } else if (file.exists() && file.isDirectory()&&!file.getName().startsWith(".")) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                addToZip(files[i], out);
            }
        }
    }
    private String getFileZipName(String filename) {
        if(filename.length()>WebserverStatic.getRootDirectory().length()) {
            return filename.substring(WebserverStatic.getRootDirectory().length());
        }
        return filename;
    }
}
