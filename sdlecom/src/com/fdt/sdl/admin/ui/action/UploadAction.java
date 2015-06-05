package com.fdt.sdl.admin.ui.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.SchedulerTool;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.util.SecurityUtil;

public class UploadAction extends Action {

    private static Logger logger = LoggerFactory.getLogger(UploadAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request)) return (mapping.findForward("welcome"));

        ActionMessages messages = new ActionMessages();
        ActionMessages errors = new ActionMessages();

        if (FileUpload.isMultipartContent(request)) {
            try {
                //read old dataset values
                ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
                ArrayList<DatasetConfiguration> old_dcs = ServerConfiguration.getDatasetConfigurations();
                Map<String,Long> oldModifiedTimes = new HashMap<String,Long>(old_dcs.size());
                for (DatasetConfiguration old_dc : old_dcs) {
                    oldModifiedTimes.put(old_dc.getName(), old_dc.getConfigFile().lastModified());
                }

                DiskFileUpload upload = new DiskFileUpload();
                upload.setSizeMax(10 * 1024 * 1024);
                List items = upload.parseRequest(request);
                Iterator iter = items.iterator();
                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();
                    if (item.isFormField()) {
                    } else {
                        String fileName = (new File(item.getName())).getName();
                        if(fileName.endsWith(".zip")) {
                            try {
                            extractZip(item.getInputStream());
                            }
                            catch(Exception ee) {
                                logger.error("exception for zip:"+ee);
                                ee.printStackTrace();
                            }
                        }
                    }
                }
                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("action.uploadFiles.success"));
                saveMessages(request, messages);

                //merge old values into the new data set
                if(sc.getIsMergingOldDatasetValues()) {
                    ArrayList<DatasetConfiguration> new_dcs = ServerConfiguration.getDatasetConfigurations();
                    for (DatasetConfiguration new_dc : new_dcs) {
                        if(oldModifiedTimes.get(new_dc.getName())!=null && new_dc.getConfigFile().lastModified() > oldModifiedTimes.get(new_dc.getName())) {
                            for (DatasetConfiguration old_dc : old_dcs) {
                                if(new_dc.getName()==old_dc.getName()) {
                                    new_dc.merge(old_dc);
                                    new_dc.save();
                                    break;
                                }
                            }
                        }
                    }
                }

                if(sc.getAllowedLicenseLevel()>0) {
                    //reload from the disk
                    ArrayList<DatasetConfiguration> dcs = ServerConfiguration.getDatasetConfigurations();
                    for (DatasetConfiguration dc : dcs) {
                        try {
                            SchedulerTool.scheduleIndexingJob(dc);
                        }catch(Throwable t) {
                            logger.info("Failed to schedule for "+dc.getName()+": " + t.toString());
                        }
                    }
                }

                return mapping.findForward("continue");
            } catch (Exception e) {
                errors.add("error", new ActionMessage("action.uploadFiles.error"));
                saveErrors(request, errors);
                return mapping.findForward("continue");
            }
        } else { // from other page
            //
        }

        return mapping.findForward("continue");
    }
    public void extractZip(InputStream is) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        int BUFFER = 4096;
        BufferedOutputStream dest = null;
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            // System.out.println("Extracting: " +entry);
            // out.println(entry.getName());
        	String entryPath = entry.getName();
        	if (entryPath.indexOf('\\')>=0){
        		entryPath = entryPath.replace('\\', File.separatorChar);
        	}
            String destFilePath = WebserverStatic.getRootDirectory() + entryPath;
            int count;
            byte data[] = new byte[BUFFER];
            // write the files to the disk
            File f = new File(destFilePath);
            
            if(!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            if(!f.exists()) {
                FileUtil.createNewFile(f);
                logger.info("creating file: "+f);
            }else {
                logger.info("already existing file: "+f);
                if(f.isDirectory()) {
                    continue;
                }
            }

            FileOutputStream fos = new FileOutputStream(f);
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = zis.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
        }
        zis.close();
    }
}
