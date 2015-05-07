package com.fdt.sdl.admin.ui.action.template;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.U;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fdt.sdl.styledesigner.Scaffold;
import com.fdt.sdl.styledesigner.ScaffoldManager;
import com.fdt.sdl.util.SecurityUtil;

public class ListScaffoldsAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if (!SecurityUtil.isAdminUser(request)){
			return (mapping.findForward("welcome"));
		}
		
        String operation = request.getParameter("operation");
        String indexName = request.getParameter("indexName");
        request.setAttribute("indexName", indexName);
        DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
        request.setAttribute("dc", dc);
        
        File root = ScaffoldManager.getScaffoldRootDirectory();
        File d = root;
        String dir = request.getParameter("dir");
        if(!U.isEmpty(dir)) {
            if (dir.charAt(dir.length()-1) == '\\') {
                dir = dir.substring(0, dir.length()-1) + "/";
            } else if (dir.charAt(dir.length()-1) != '/') {
                dir += "/";
            }
            d = new File(root, dir);
        }

        if("ajaxListDirectories".equals(operation)) {
            File[] dirs = d.listFiles(new FileFilter() {
                public boolean accept(File f) {
                    if(!f.isDirectory()) return false;
                    if("CVS".equals(f.getName())||f.getName().startsWith(".")) return false;
                    return !FileUtil.resolveFile(f, ScaffoldManager.PAGE_STYLE_XML).exists();
                }});
            Arrays.sort(dirs, new Comparator<File>() {
                public int compare(File a, File b) {
                    if(a.getName().equals("default")) return -1;
                    if(b.getName().equals("default")) return 1;
                    return a.getName().compareTo(b.getName());
                }});
            request.setAttribute("dir", dir);
            request.setAttribute("dirs", dirs);
            return (mapping.findForward("ajaxListDirectories"));
        }else if("ajaxListScaffolds".equals(operation)) {
            listScaffolds(d,request);
            return (mapping.findForward("ajaxListScaffolds"));
        }else {
            listScaffolds(root, request);
            return (mapping.findForward("continue"));
        }
	}
	private void listScaffolds(File dir, HttpServletRequest request) {
        List<Scaffold> all = ScaffoldManager.listScaffolds(dir);
        List<Scaffold> full_scaffolds = new ArrayList<Scaffold>();
        List<Scaffold> partial_scaffolds = new ArrayList<Scaffold>();
        for(Scaffold s : all) {
            if(s.isPartial || s.isChart || s.isMap) {
            	partial_scaffolds.add(s);
            } else {
            	full_scaffolds.add(s);
            }
        }
        request.setAttribute("full_scaffolds", full_scaffolds);
        request.setAttribute("partial_scaffolds", partial_scaffolds);
	}

}
