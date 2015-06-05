package net.javacoding.xsearch.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.javacoding.xsearch.status.IndexStatus;

public class AffectedDirectoryGroup {
    public File newDirectory;
    public List<File> newDirectories = new ArrayList<File>();
    public List<File> oldDirectories = new ArrayList<File>();
    
    public AffectedDirectoryGroup addOldDirectory(File old){
        if(old!=null){
            oldDirectories.add(old);
        }
        return this;
    }
    public AffectedDirectoryGroup addNewDirectory(File newDir){
        if(newDir!=null){
            oldDirectories.add(newDir);
        }
        return this;
    }

    public File getNewDirectory() {
        return newDirectory;
    }

    public void setNewDirectory(File newDirectory) {
        this.newDirectory = newDirectory;
    }

    public List<File> getOldDirectories() {
        return oldDirectories;
    }

    public void setOldDirectories(List<File> oldDirectories) {
        this.oldDirectories = oldDirectories;
    }
    
    public void setFinalReadyStatus(){
        try {
            if(newDirectories!=null){
                IndexStatus.setIndexReady(newDirectory);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        for(File dir : newDirectories){
            try {
                IndexStatus.setIndexNotReady(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for(File dir : oldDirectories){
            try {
                IndexStatus.setIndexNotReady(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
