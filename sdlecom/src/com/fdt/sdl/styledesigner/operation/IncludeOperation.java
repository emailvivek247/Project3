package com.fdt.sdl.styledesigner.operation;

import java.io.File;
import java.io.IOException;

import com.fdt.sdl.styledesigner.value.ColumnValue;
import com.fdt.sdl.styledesigner.value.ColumnsValue;
import com.fdt.sdl.styledesigner.value.ScaffoldValue;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.U;

import com.fdt.sdl.styledesigner.Scaffold;
import com.fdt.sdl.styledesigner.Template;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("include")
public class IncludeOperation extends ScaffoldOperation {
    public static final String PREPEND = "prepend";
    public static final String APPEND = "append";

    @XStreamAlias("type")
    @XStreamAsAttribute
    public String type;

    @XStreamAlias("file")
	@XStreamAsAttribute
	public String file;

    @XStreamAlias("toFile")
    @XStreamAsAttribute
	public String toFile;
    
    @XStreamAlias("withColumnName")
    @XStreamAsAttribute
    public boolean withColumnName;
    
    public IncludeOperation() {
		super();
	}

	@Override
    public void operate(Scaffold s, Template t) throws IOException {
        if(t.directory==null) return;
        if(U.isEmpty(toFile)) return;
        if(type==null) type = PREPEND;
        File targetFile = FileUtil.resolveFile(t.directory, toFile);
        if(!targetFile.exists()) return;
        if(withColumnName) {
            processFinalFileNameWithColumnName(s);
            if(finalFileName==null) {
                FileUtil.deleteAll(FileUtil.resolveFile(t.directory, file));
                return;
            }
        }
        String stringToInclude = "<#include \""+(finalFileName==null? file : finalFileName ) +"\">";
        String targetFileContent = FileUtil.readFile(targetFile);
        if(targetFileContent==null) targetFileContent = "";
        if(targetFileContent.indexOf(stringToInclude)<0) {
            if(PREPEND.equals(type)) {
                targetFileContent = stringToInclude+"\n"+targetFileContent;
            }else {
                targetFileContent = targetFileContent + stringToInclude+"\n";
            }
            FileUtil.writeFile(targetFile, targetFileContent);
        }
        if(finalFileName!=null) {
            FileUtil.rename(FileUtil.resolveFile(t.directory, file), FileUtil.resolveFile(t.directory, finalFileName));
        }
    }

    @Override
    public boolean accept(Template t) throws IOException {
        return requireFile(t, toFile);
    }

    private transient String finalFileName = null;
    private String processFinalFileNameWithColumnName(Scaffold s) {
        String columnName = null;
        for (ScaffoldValue sv : s.getValues()) {
            if (sv instanceof ColumnValue) {
                columnName = ((ColumnValue) sv).getColumnName();
            } else if (sv instanceof ColumnsValue) {
                if(((ColumnsValue) sv).getColumnNames().size()==1) {
                    columnName = ((ColumnsValue) sv).getColumnNames().get(0);
                }
            }
            if(columnName!=null) break;
        }
        if(!U.isEmpty(columnName)) {
            finalFileName = file.substring(0, file.lastIndexOf(".stl")) + "_" + columnName.toLowerCase() + ".stl";
        }
        return finalFileName;
    }

}
