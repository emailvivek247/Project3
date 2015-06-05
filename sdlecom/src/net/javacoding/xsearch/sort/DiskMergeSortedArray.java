package net.javacoding.xsearch.sort;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskMergeSortedArray {
	
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	private Object[] array = null;
	private int arrayInsertIndex = 0;
	private int arrayPopIndex = 0;

	private int blockSize = 0;
	private File workingDirectory = null;
	private String fileNamePrefix = null;
	
	private int fileCount = 0;
	
	private DiskBasedSortedArray[] diskArrays = null;

	/**
	 * 1. Keep adding Comparables to this array dmsa.add(obj)
	 * 2. dmsa.flush()
	 * 3. Keep popping from this array dmsa.pop() until null
	 * @param blockSize
	 * @param workingDirectory
	 * @param fileNamePrefix
	 */
	public DiskMergeSortedArray(int blockSize, File workingDirectory, String fileNamePrefix) {
		this.blockSize = blockSize;
		this.workingDirectory = workingDirectory;
		this.fileNamePrefix = fileNamePrefix;
		this.array = new Object[blockSize]; 
	}
	
	public boolean add(Object o){
		array[arrayInsertIndex++] = o;
		if(arrayInsertIndex==blockSize){
			try{
				flush();
			}catch(IOException e){
				logger.warn("Can not write to "+workingDirectory);
			}
		}
		return true;
	}
	
	private void flush() throws IOException{
		File blockFile = new File(workingDirectory, fileNamePrefix+"."+(fileCount++));
		FileOutputStream fos = new FileOutputStream(blockFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		Arrays.sort(array, arrayPopIndex, arrayInsertIndex);
		for(int i=arrayPopIndex;i<blockSize&&i<arrayInsertIndex;i++){
			oos.writeObject(array[i]);
			array[i] = null;
		}
		oos.close();
		arrayInsertIndex = 0;
		arrayPopIndex = 0;
	}
	
	public Comparable pop() throws IOException, ClassNotFoundException{
		if(diskArrays==null){
			flush();
			diskArrays = new DiskBasedSortedArray[fileCount];
			for(int i=0;i<diskArrays.length;i++){
				diskArrays[i] = new DiskBasedSortedArray(i);
			}
		}
		int smallest = -1;
		Comparable<Object> o = null;
		for(int i=0;i<diskArrays.length;i++){
			if(o==null|| diskArrays[i].peek()!=null && o.compareTo(diskArrays[i].peek())>0){
				o = (Comparable<Object>) diskArrays[i].peek();
				smallest = i;
			}
		}
		if(smallest==-1){
			return null;
		}
		return (Comparable) diskArrays[smallest].pop();
	}
	
	public void close(){
		if(diskArrays!=null){
			for(int i=0;i<diskArrays.length;i++){
				diskArrays[i].close();
			}
		}
	}
	
	private class DiskBasedSortedArray{
        int readBlockSize = (int) Math.round(blockSize*1.0/fileCount+0.5);
		File storageFile = null;
		int popIndex = readBlockSize;
		int insertIndex = 0;
		Object[] objects = null;
		ObjectInputStream ois = null;
		private DiskBasedSortedArray(int ext){
			storageFile = new File(workingDirectory, fileNamePrefix+"."+ext);
			objects = new Object[readBlockSize];
		}
		
		protected Object pop() throws IOException, ClassNotFoundException{
			if(popIndex>=insertIndex){
				fillup();
				if(popIndex>=insertIndex){
					return null;
				}
			}
            Object obj = objects[popIndex];
            objects[popIndex++]=null;
			return obj;
		}
		protected Object peek() throws IOException, ClassNotFoundException{
			if(popIndex>=insertIndex){
				fillup();
				if(popIndex>=insertIndex){
					return null;
				}
			}
			return objects[popIndex];
		}
		protected void fillup() throws IOException, ClassNotFoundException{
			if(ois==null){
		        FileInputStream fis = new FileInputStream(storageFile);
		        ois = new ObjectInputStream(fis);
			}
			insertIndex = 0;
			Object o;
			try{
				while(insertIndex<readBlockSize){
                    o = ois.readObject();
                    if(o!=null) {
    					objects[insertIndex++] = o;
    					popIndex = 0;
                    }
				}
			}catch(EOFException e){}
		}
		protected void close(){
			if(ois!=null){
		        try {
					ois.close();
					storageFile.delete();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
