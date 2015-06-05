package com.fdt.sdl.styledesigner.operation;

import java.io.IOException;


import com.fdt.sdl.styledesigner.Scaffold;
import com.fdt.sdl.styledesigner.Template;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("require")
public class RequireOperation extends ScaffoldOperation {
    @XStreamAlias("file")
	@XStreamAsAttribute
	public String file;
    
    public RequireOperation() {
		super();
	}

	@Override
    public void operate(Scaffold s, Template t) throws IOException {
    }

    @Override
    public boolean accept(Template t) throws IOException {
        return requireFile(t, file);
    }
}
