package com.fdt.sdl.styledesigner.operation;

import java.io.File;
import java.io.IOException;

import com.fdt.sdl.styledesigner.Scaffold;
import com.fdt.sdl.styledesigner.Template;

import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.U;


public abstract class ScaffoldOperation {
    public abstract void operate(Scaffold s, Template t) throws IOException;
    public abstract boolean accept(Template t) throws IOException;

    protected boolean requireFile(Template t, String fileName) {
        if(t.directory==null) return false;
        if(U.isEmpty(fileName)) return true;
        File targetFile = FileUtil.resolveFile(t.directory, fileName);
        if(!targetFile.exists()) return false;
        if(!targetFile.canWrite()) return false;
        return true;
    }
}
