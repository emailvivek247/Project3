package com.fdt.ecomadmin.ui.controller.administration;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_ADMIN_EDIT_EMAIL;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_ADMINISTRATION;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.EDIT_EMAIL_TEMPLATES;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Comparator;

import javax.servlet.http.HttpServletRequest;

import org.apache.tools.ant.util.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.breadcrumbs.Link;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.common.util.FileMap;

@Controller
public class EmailTemplatesController extends AbstractBaseController {

    @Value("${ecom.emailtemplates.path}")
    private String DEFAULT_PATH = null;

    public File[] getListOfEmailTemplates(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File a, File b) {
                return (a.getName().compareTo(b.getName()));
            }});
        return files;
    }

    @Link(label="Email Email Templates", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/readEmailTemplate.admin")
    public ModelAndView editEmailTemplatesTemp(HttpServletRequest request,
                                               @RequestParam(defaultValue = "") String filePathReq,
                                               @RequestParam(defaultValue = "") String fileIdReq) {
        ModelAndView modelAndView = this.getModelAndView(request, EDIT_EMAIL_TEMPLATES, TOP_ADMINISTRATION,
        		SUB_ADMIN_EDIT_EMAIL);
        String treeXML = FileMap.displayFiles(DEFAULT_PATH);
        modelAndView.addObject("treeXML", treeXML);
        modelAndView.addObject("filePathReq", filePathReq);
        modelAndView.addObject("fileIdReq", fileIdReq);
        modelAndView.addObject("filePath", filePathReq);
        modelAndView.addObject("fileId", fileIdReq);
        if (filePathReq.length() > 0) {
            try {
                String fileData = readEmailTemplate(filePathReq);
                if (fileData == null) {
                    fileData = "";
                }
                modelAndView.addObject("fileContent", fileData);
                modelAndView.addObject("isFileAccessed", "Y");
            } catch (IOException exception) {
                modelAndView.addObject("errormsg", "Error Reading file :" + filePathReq);
            }

        }
        return modelAndView;
    }

    @RequestMapping(value="/saveEmailTemplate.admin")
    public ModelAndView editEmailTemplateContents(HttpServletRequest request,
                                                  @RequestParam(defaultValue = "") String filePath,
                                                  @RequestParam(defaultValue = "") String fileContent,
                                                  @RequestParam(defaultValue = "") String fileId) {
        ModelAndView modelAndView = this.getModelAndView(request, EDIT_EMAIL_TEMPLATES, TOP_ADMINISTRATION,
        		SUB_ADMIN_EDIT_EMAIL);
        String treeXML = FileMap.displayFiles(DEFAULT_PATH);
        modelAndView.addObject("treeXML", treeXML);
        modelAndView.addObject("filePath", filePath);
        modelAndView.addObject("fileId", fileId);
        modelAndView.addObject("filePathReq", filePath);
        modelAndView.addObject("fileIdReq", fileId);
        File file =  new File(filePath);
        try {
            writeFile(file, fileContent, "UTF-8");
            String fileData = readEmailTemplate(filePath);
            if (fileData == null) {
                fileData = "";
            }
            modelAndView.addObject("fileContent", fileData);
            modelAndView.addObject("isFileAccessed", "Y");
            modelAndView.addObject("successmsg", "File Saved Successfully");
        } catch (IOException exception) {
            modelAndView.addObject("errormsg", "Error Saving file :" + filePath);
        }


        return modelAndView;
    }

    private static String readEmailTemplate(String emailTemplatePath) throws IOException {
        File emailTemplate = new File(emailTemplatePath);
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(emailTemplate), "UTF-8"));
            return FileUtils.readFully(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private static void writeFile(File f, String str, String encoding) throws IOException {
        BufferedWriter out = null;
        try {
            if (encoding == null) {
                out = new BufferedWriter(new FileWriter(f));
            } else {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), encoding));
            }
            out.write(str);
            out.flush();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

}
