/*
 * Created on Feb 2, 2005
 *
 */
package net.javacoding.xsearch.status;

/**
 * 
 */
public  class KeyWord {
    String key;
    int hit = 0;

    public KeyWord(String key) {
        this.key = key;
        hit = 1;
    }

    public void addHitNumber() {
        hit++;
    }

    public int getHitNumber() {
        return hit;
    }

    public String getKey() {
        return key;
    }
}
