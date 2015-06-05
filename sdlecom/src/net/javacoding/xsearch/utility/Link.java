/*
 * Created on Feb 8, 2005
 */
package net.javacoding.xsearch.utility;

/**
 * 
 *
 * to hold an html link information
 */
public class Link {

    protected String href = null;
    protected String text = null;

    public Link(String href, String text) {
        this.href = href;
        this.text = text;
    }
    public String getHref() {
        return href;
    }
    public void setHref(String href) {
        this.href = href;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
}
