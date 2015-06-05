package net.javacoding.xsearch;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.javacoding.xsearch.core.AffectedDirectoryGroup;
import net.javacoding.xsearch.core.BeforeAfterOperation;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.HttpUtil;
import net.javacoding.xsearch.utility.LogUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.core.ui.action.indexing.status.DownloadIndexAction;

public class IndexSubscriber {
	
	private static final Logger logger  = LoggerFactory.getLogger(IndexManager.class.getName());

    public static final String SUBSCRIBED_MAIN_DIRECTORY = "main";
    public static final String SUBSCRIBED_TEMP_DIRECTORY = "temp";
    public static final String SUBSCRIBED_DICTIONARY_DIRECTORY = "dictionary";
    public static final String SUBSCRIBED_PHRASE_DIRECTORY = "phrase";

    protected IndexerContext ic;

    public IndexSubscriber() {
    }
    public IndexSubscriber(IndexerContext ic) {
        this.ic = ic;
    }

    private void startLogging() {
    	LogUtil.setLog(ic.getDatasetConfiguration());
    }

    private void stopLogging(){
        LogUtil.unsetLog(ic.getDatasetConfiguration());
    }

    public void retrieveMainTempIndex(String mainDir, String tempDir, BeforeAfterOperation beforeAfterOperation) throws Throwable {
        try {
            startLogging();
            FileList mainFileList = readFileList(mainDir);
            FileList tempFileList = readFileList(tempDir);
            boolean mainUpdated = false;
            boolean tempUpdated = false;
            AffectedDirectoryGroup adg = new AffectedDirectoryGroup();
            File currentMainFile = IndexStatus.findActiveMainDirectoryFile(ic.getDatasetConfiguration());
            if(mainFileList!=null && mainFileList.modifiedTime > IndexStatus.getIndexTimestamp(currentMainFile)){
                mainUpdated = downloadFileListTo(mainFileList,IndexStatus.findNonActiveMainDirectoryFile(ic.getDatasetConfiguration()));
                adg.addOldDirectory(currentMainFile);
            }
            File currentTempFile = IndexStatus.findActiveTempDirectoryFile(ic.getDatasetConfiguration());
            if(tempFileList!=null && tempFileList.modifiedTime > IndexStatus.getIndexTimestamp(currentTempFile)){
                tempUpdated = downloadFileListTo(tempFileList,IndexStatus.findNonActiveTempDirectoryFile(ic.getDatasetConfiguration()));
                adg.addOldDirectory(currentTempFile);
            }
            if(mainUpdated||tempUpdated){
                IndexManager.updateIndex(ic.getDatasetConfiguration(), adg);
            }
        } catch (MalformedURLException mue) {
            logger.error(NOTIFY_ADMIN, "Invalid URL", mue);
        } catch (IOException ioe) {
            logger.error(NOTIFY_ADMIN, "IOException", ioe);
        } finally {
            stopLogging();
        }
    }

    public void retrieveIndex(String dirName, BeforeAfterOperation beforeAfterOperation) throws Throwable {
        try {
            startLogging();
            FileList fileList = readFileList(dirName);
            if(fileList!=null){
                File localDir = DownloadIndexAction.getDownloadBaseDirectory(dirName, ic.getDatasetConfiguration());
                if (!localDir.exists()) localDir.mkdirs();
                logger.info("local index dir="+localDir);
                if(fileList.modifiedTime>IndexStatus.getIndexTimestamp(localDir)) {
                    beforeAfterOperation.before();
                    downloadFileListTo(fileList,localDir);
                    beforeAfterOperation.after();
                    logger.info("New index retrieved in " + localDir);
                }
            }
        } catch (MalformedURLException mue) {
            logger.error(NOTIFY_ADMIN, "Invalid URL", mue);
        } catch (IOException ioe) {
        	logger.error(NOTIFY_ADMIN, "IOException", ioe);
        } finally {
            stopLogging();
        }
    }
    
    private boolean downloadFileListTo(FileList fileList, File localDir) throws IOException{
        FileUtil.deleteAllFiles(localDir);
        localDir.mkdirs();
        for (FileEntry fe:fileList.files) {
            downloadFile(fe,localDir);
        }
        return true;
    }
    private FileList readFileList(String dirName){
        //get list from server
        String content = null;
        try{
            content = HttpUtil.open(getListURL(dirName));
        }catch(IOException e) {
            logger.warn("Failed to get the list. Please check subscribed server's license level, and make sure current server is on the Allowed IP or Host Name List of the index in Advanced Settings", e);
        }
        if(content!=null&&content.indexOf("<errors>")>=0) {
            logger.warn("Errors to get the list from "+dirName+":" + content);
            return null;
        }else if (content != null && !"".equals(content.trim())) {
            String[] list = content.split("\n");
            FileList fileList = new FileList();
            fileList.modifiedTime = reorderStringArray(list);
            for(String s : list){
                try{
                    String commands[] = s.split(" ");
                    FileEntry fe = new FileEntry();
                    fe.dir = commands[0];
                    fe.fileName = commands[1];
                    fe.modifiedTime = new Long(commands[2]).longValue();
                    fe.size = new Long(commands[3]).longValue();
                    fileList.files.add(fe);
                }catch(NumberFormatException nfe){
                    return null;
                }
            }
            return fileList;
        }
        return null;
    }
    
    private static class FileList{
        long modifiedTime;
        List<FileEntry> files = new ArrayList<FileEntry>();
    }
    private static class FileEntry{
        long modifiedTime;
        long size;
        String dir;
        String fileName;
    }
    
    private void downloadFile(FileEntry fe, File localTargetDirectory) throws IOException {
        URL fileURL = getDownloadFileURL(fe.dir, fe.fileName);
        File workingFile= new File(localTargetDirectory,fe.fileName);
        boolean created =workingFile.createNewFile();
        logger.info("creating file "+workingFile.getPath() +" = "+created);
        if(fe.fileName.endsWith("cfs")) {
            FileUtil.copyFile(fileURL.openStream(), workingFile,32);
        }else {
            FileUtil.copyFile(fileURL.openStream(), workingFile,4);
        }
        workingFile.setLastModified(fe.modifiedTime);
    }
    
    private URL getListURL(String dir) throws MalformedURLException {
        String listAddr = ic.getDatasetConfiguration().getSubscriptionUrl() + "downloadIndex.do?operation="+DownloadIndexAction.LIST_FILES+"&dir="+dir+"&indexName="+ic.getDatasetConfiguration().getName();
        logger.info("Get list from "+listAddr);
        return new URL(listAddr);
    }
    private URL getDownloadFileURL(String dir, String fileName) throws MalformedURLException {
        String fileUrl = ic.getDatasetConfiguration().getSubscriptionUrl() + "downloadIndex.do?indexName="+ic.getDatasetConfiguration().getName()+"&operation="+DownloadIndexAction.DOWNLOAD_FILE+"&dir="+dir+"&fileName=" + fileName;
        return new URL(fileUrl);
    }
    
    private static long reorderStringArray(String x[]) {
        if(x==null) return -1;
        long ret = -1;
        int ready = -1;
        for(int i=0;i<x.length;i++) {
            if(x[i].indexOf(" ready ")>0) {
                ready = i;
                ret = Long.parseLong(x[i].split(" ")[2]);
                break;
            }
        }
        if(ready>=0) {
            String tmp = x[ready];
            x[ready] = x[x.length-1];
            x[x.length-1] = tmp;
        }
        return ret;
    }

}
