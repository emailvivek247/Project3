package com.fdt.common.util;

import java.io.File;
import java.util.List;
import java.util.Vector;

public class FileMap {

    private static final int INITIAL_DEPTH = 1;
    private static final String DEFAULT_SEP_WIDTH = "    ";
    private static final String FILE_PREFIX = "---";
    private static String treeXML = "";
    private static Integer counter = 0;

    public static String displayFiles(String filePath) {
        counter = 0;
        String fileXML = displayFiles(new File(filePath));
        return fileXML;
    }

    public static String displayFiles(File file) {
        treeXML = "<ul>";
        //System.out.print(file);
        List<Integer> depthList = new Vector<Integer>();
        depthList.add(new Integer(INITIAL_DEPTH));
        displayFiles(file.listFiles(), INITIAL_DEPTH, depthList, counter);
        treeXML = treeXML + "</ul>";
        return treeXML;
    }

    public static void displayFiles(File[] files, int depth, List<Integer> parentDepths, Integer parentId) {
        Integer maxDepth = (Integer) parentDepths.get(parentDepths.size() - 1);
        String hline = "";
        for (int i = INITIAL_DEPTH; i <= maxDepth; i++) {
            boolean found = false;
            for (int j = 0; j < parentDepths.size(); j++) {
                Integer curDepth = (Integer) parentDepths.get(j);
                if (i == curDepth.intValue()) {
                    found = true;
                }
            }
            if (found == true) {
                hline += DEFAULT_SEP_WIDTH + "|";
            } else {
                hline += DEFAULT_SEP_WIDTH;
            }
        }
        hline = "\n" + hline + "\n" + hline;
        hline += FILE_PREFIX;

        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                counter = counter + 1;
                treeXML = treeXML + "<li id='" + counter.toString() + "' rel='file'><a href='" + files[i].toString().replace("\\", "\\\\") + "'>" + files[i].getName() + "</a></li>";
            } else {
                counter = counter + 1;
                treeXML = treeXML + "<li id='" + counter.toString() + "' rel='folder' state='open'><a href='#'>" + files[i].getName() + "</a><ul>";
                List<Integer> depthList = new Vector<Integer>(parentDepths);
                Integer curDepth = new Integer(depth);
                Integer nxtLevelDepth = new Integer(depth + 1);
                if (i == files.length - 1) {
                    if (depthList.contains(curDepth)) {
                        depthList.remove(curDepth);
                    }
                }
                depthList.add(nxtLevelDepth);
                if (files[i].listFiles() != null) {
                    int newParentId = counter;
                    displayFiles(files[i].listFiles(), nxtLevelDepth, depthList, newParentId);
                }
                treeXML = treeXML + "</ul></li>";

            }
        }

    }

}
