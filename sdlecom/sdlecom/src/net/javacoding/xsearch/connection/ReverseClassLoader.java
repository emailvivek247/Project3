package net.javacoding.xsearch.connection;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class ReverseClassLoader extends URLClassLoader {
    private ClassLoader parent;

    public ReverseClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, null);
        this.parent = parent;
    }

    public ReverseClassLoader(URL[] urls) {
        super(urls, null);
        this.parent = getSystemClassLoader();
    }

    public ReverseClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
        this.parent = parent;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException ex) {
            if (parent != null) {
                return parent.loadClass(name);
            } else {
                throw ex;
            }
        }
    }

    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException ex) {
            if (parent != null) {
                return parent.loadClass(name);
            } else {
                throw ex;
            }
        }
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException ex) {
            if (parent != null) {
                return parent.loadClass(name);
            } else {
                throw ex;
            }
        }
    }

    public URL getResource(String name) {
        URL url = super.getResource(name);
        if (url == null && parent != null) {
            url = parent.getResource(name);
        }
        return url;
    }

    public URL findResource(String name) {
        URL url = super.findResource(name);
        if (url == null && parent != null) {
            url = parent.getResource(name);
        }
        return url;
    }
}
