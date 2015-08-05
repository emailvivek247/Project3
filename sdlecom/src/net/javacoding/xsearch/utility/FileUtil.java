package net.javacoding.xsearch.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tools.ant.util.FileUtils;

/**
 * created for file operation, like deleteAll
 */

public final class FileUtil {
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.utility.FileUtil");

    /**
     * delete a whole directory
     * @throws IOException 
     */
    public static boolean deleteAll(File f) throws IOException {
    	boolean ret = true;
    	 if (f == null) {
             return true;
    	 }
         if (!f.exists()) {
             return true;
         }
         if (!f.canWrite()) {
             fail("Delete: write protected: " + f);
         }
    	org.apache.commons.io.FileUtils.forceDelete(f);		
    	return ret;
    }

    public static boolean deleteAll(File[] files) throws IOException {
        boolean ret = true;
        if (files == null)
            return true;
        for (int i = 0; i < files.length; i++) {
            ret &= deleteAll(files[i]);
        }
        return ret;
    }

    public static void deleteAll(String filename) throws IOException {
        if (filename == null)
            return;
        File f = new File(filename);
        deleteAll(f);
    }

    /** A convenience method to throw an exception */
    protected static void fail(String msg) throws IOException {
        throw new IOException(msg);
    }

    public static boolean deleteAllFiles(String filename) throws IOException {
        File f = new File(filename);
        return deleteAllFiles(f, null);
    }

    public static boolean deleteAllFiles(File dir) throws IOException {
        if (dir == null)
            return true;
        return deleteAllFiles(dir, null);
    }

    public static boolean deleteAllFiles(String filename, String except) throws IOException {
        File f = new File(filename);
        return deleteAllFiles(f, except);
    }

    /**
     * delete all files under one directory
     */
    public static boolean deleteAllFiles(File dir, String except) throws IOException {
        if (dir == null)
            return true;
        // delete every File under dir
        File[] files = dir.listFiles();
        if (files == null)
            return true;
        boolean ret = true;
        for (int i = 0; i < files.length; i++) {
            if (!files[i].isDirectory() && files[i].canWrite()) {
                if (!files[i].getName().equals(except)) {
                    int k = 0;
                    double triedLimit = files[i].length() * 1.0f / 1024 / 1024;
                    triedLimit = (triedLimit > 10 ? triedLimit : 10);
                    while (!files[i].delete() && k++ < triedLimit) {
                        logger.warn("deleting " + files[i] + " the " + k + " time");
                        try {
                            Thread.sleep(100L);
                            /* Commented By Vivek.
                             * System.gc(); // this is added to prevent some
                                           // window file deletion error
                            */
                        } catch (InterruptedException e) {
                            logger.warn("InterruptedException", e);
                        }
                    }
                    if (files[i].exists()) {
                        logger.warn("Skip deleting " + files[i]);
                        ret = false;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * move all files from one directory to another
     */
    public static boolean moveAllFiles(File fromDir, File toDir) throws IOException {
        String[] fileNames = fromDir.list();
        for (int i = 0; i < fileNames.length; i++) {
            File afile = new File(fromDir, fileNames[i]);
            fileUtils.rename(afile, new File(toDir, fileNames[i]));
        }
        return true;
    }

    /**
     * Copy all files, including directories, from one directory to another.
     */
    public static void copyAll(File fromDir, File toDir, String[] excepts) throws IOException {
        copyAll(fromDir, toDir, excepts, 32);
    }

    /*
     * deep copy directories, with exception files copy ready file last
     */
    public static void copyAll(File fromDir, File toDir, String[] excepts, int bufferKB) throws IOException {
        if (!toDir.exists()) {
            if (!toDir.mkdirs()) {
                throw new IOException();
            }
        }
        File[] fromFiles = fromDir.listFiles();
        if (fromFiles != null) {
            reorderFileArray(fromFiles);
            for (int i = 0; i < fromFiles.length; i++) {
                File ff = fromFiles[i];
                boolean allowed = true;
                if (excepts != null && excepts.length > 0) {
                    for (int j = 0; j < excepts.length && allowed; j++) {
                        if (ff.getName().matches(excepts[j])) {
                            allowed = false;
                        }
                    }
                }
                if (allowed) {
                    File tf = resolveFile(toDir, ff.getName());
                    if (ff.isDirectory()) {
                        // Avoid infinite self-copies
                        if (!ff.equals(toDir)) {
                            copyAll(ff, tf, excepts, bufferKB);
                        }
                    } else {
                        if (ff.getName().endsWith("cfs")) {
                            copyFile(ff, tf, bufferKB * 16);
                        } else {
                            copyFile(ff, tf, bufferKB);
                        }
                    }
                }
            }
        }
    }

    private static int reorderFileArray(File x[]) {
        if (x == null)
            return -1;
        int ready = -1;
        for (int i = 0; i < x.length; i++) {
            if (x[i].getName().equals("ready")) {
                ready = i;
                break;
            }
        }
        if (ready >= 0) {
            File tmp = x[ready];
            x[ready] = x[x.length - 1];
            x[x.length - 1] = tmp;
        }
        return ready;
    }

    /**
     * Copy all files, including directories, from one directory to another.
     */
    public static void copyAll(File fromDir, File toDir) throws IOException {
        copyAll(fromDir, toDir, new String[] { "CVS", ".svn" });
    }

    public static void copyAll(File fromDir, File toDir, int bufferKB) throws IOException {
        copyAll(fromDir, toDir, new String[] { "CVS", ".svn" }, bufferKB);
    }

    /**
     * Copy all files, not including directories, from one directory to another.
     */
    public static void copyAllFiles(File fromDir, File toDir) throws IOException {
        copyAll(fromDir, toDir, 32);
    }

    /*
     * shallow copy files copy ready file last
     */
    public static void copyAllFiles(File fromDir, File toDir, int bufferKB) throws IOException {
        if (!toDir.exists()) {
            if (!toDir.mkdirs()) {
                throw new IOException();
            }
        }
        File[] fromFiles = fromDir.listFiles();
        reorderFileArray(fromFiles);
        if (fromFiles != null) {
            for (int i = 0; i < fromFiles.length; i++) {
                File ff = fromFiles[i];
                File tf = resolveFile(toDir, ff.getName());
                if (!ff.isDirectory()) {
                    copyFile(ff, tf, bufferKB);
                }
            }
        }
    }

    /**
     * Writes a string to the file.
     * 
     * @param f
     *            the file to write
     * @param str
     *            string to be written
     * @throws IOException
     *             if the file writing fails
     */
    public static void writeFile(File f, String str) throws IOException {
        writeFile(f, str, null);
    }

    /**
     * Writes a string to the file.
     * 
     * @param f
     *            the file to write
     * @param str
     *            string to be written
     * @param encoding
     *            the encoding used to write the file
     * @throws IOException
     *             if the file writing fails
     */
    public static void writeFile(File f, String str, String encoding) throws IOException {
        BufferedWriter out = null;
        try {
            if (encoding == null) {
                out = new BufferedWriter(new FileWriter(f));
            } else {
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), encoding));
            }
            out.write(str);
            out.flush();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Read a file.
     * 
     * @param f
     *            the file to read
     * @return the string contents read from the file
     * @throws IOException
     *             if the file reading fails
     */
    public static String readFile(File f) throws IOException {
        return readFile(f, null);
    }

    /**
     * Replaces every subsequence of the source file that matches a pattern with
     * the pattern's mapped replacement string, and writes the result to the
     * destination file
     * 
     * @param sourceFile
     *            the file to read
     * @param destFile
     *            the file to write
     * @param replacements
     *            the pattern to replacement string map
     * @param inputEncoding
     *            the encoding used to read the file
     * @param outputEncoding
     *            the encoding used to write the file
     */
    public static void replaceFile(File sourceFile, File destFile, Map replacements, String inputEncoding, String outputEncoding) throws IOException {
        if (replacements == null) {
            copyFile(sourceFile, destFile);
            return;
        }
        String s = null;
        if (inputEncoding == null) {
            s = readFile(sourceFile);
        } else {
            s = readFile(sourceFile, inputEncoding);
        }
        if (s == null)
            s = "";
        Iterator it = replacements.keySet().iterator();
        for (int size = replacements.size(); size > 0; size--) {
            String pattern = (String) it.next();
            String replacement = EscapeChars.forRegex((String) replacements.get(pattern));
            s = s.replaceAll(pattern, replacement);
        }
        if (outputEncoding == null) {
            writeFile(destFile, s);
        } else {
            writeFile(destFile, s, outputEncoding);
        }
    }

    // The following methods are wrappers of the file utility functions in
    // org.apache.tools.ant.util.FileUtils

    private static FileUtils fileUtils = FileUtils.newFileUtils();

    /**
     * Read a file.
     * 
     * @param f
     *            the file to read
     * @param encoding
     *            the encoding used to read the file
     * @return the string contents read from the file
     * @throws IOException
     *             if the file reading fails
     */
    public static String readFile(File f, String encoding) throws IOException {
        BufferedReader in = null;
        try {
            if (encoding == null) {
                in = new BufferedReader(new FileReader(f));
            } else {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding));
            }
            return FileUtils.readFully(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Convienence method to copy a file from a source to a destination. No
     * filtering is performed.
     * 
     * @param sourceFile
     *            Name of file to copy from. Must not be <code>null</code>.
     * @param destFile
     *            Name of file to copy to. Must not be <code>null</code>.
     * @throws IOException
     *             if the copying fails
     */
    public static void copyFile(String sourceFile, String destFile) throws IOException {
        fileUtils.copyFile(sourceFile, destFile);
    }

    /**
     * Renames a file, even if that involves crossing file system boundaries.
     */
    public static void rename(File from, File to) throws IOException {
        fileUtils.rename(from, to);
    }

    /**
     * Convienence method to copy a file from a source to a destination. No
     * filtering is performed.
     * 
     * @param sourceFile
     *            the file to copy from. Must not be <code>null</code>.
     * @param destFile
     *            the file to copy to. Must not be <code>null</code>.
     * @throws IOException
     *             if the copying fails
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        InputStream is = new FileInputStream(sourceFile);
        copyFile(is, destFile);
        destFile.setLastModified(sourceFile.lastModified());
    }

    public static void copyFile(File sourceFile, File destFile, int bufferKB) throws IOException {
        logger.debug("copy "+sourceFile + " to " + destFile);
        InputStream is = new FileInputStream(sourceFile);
        copyFile(is, destFile, bufferKB);
        destFile.setLastModified(sourceFile.lastModified());
    }

    public static void copyFile(InputStream is, File destFile) throws IOException {
        copyFile(is, destFile, 32);
    }

    public static void copyFile(InputStream is, File destFile, int bufferKB) throws IOException {
        byte[] buffer = new byte[bufferKB * 1024 / 8];
        if (destFile.exists() && destFile.isFile()) {
            destFile.delete();
        }
        File parent = destFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(destFile);

            int count;
            while ((count = is.read(buffer)) != -1) {
                fos.write(buffer, 0, count);
            }

        } catch (IOException ioe) {
            logger.error("Downloader.IOException: " + ioe.getMessage());
            ioe.printStackTrace();
        } catch (Exception e) {
            logger.error("Downloader.Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (fos != null)
                fos.close();
            if (is != null)
                is.close();
        }
    }

    /**
     * Interpret the filename as a file relative to the given file - unless the
     * filename already represents an absolute filename.
     * 
     * @param file
     *            the "reference" file for relative paths. This instance must be
     *            an absolute file and must not contain &quot;./&quot; or
     *            &quot;../&quot; sequences (same for \ instead of /). If it is
     *            null, this call is equivalent to
     *            <code>new java.io.File(filename)</code>.
     * @param filename
     *            a file name
     * @return an absolute file that doesn't contain &quot;./&quot; or
     *         &quot;../&quot; sequences and uses the correct separator for the
     *         current platform.
     */
    public static File resolveFile(File file, String filename) {
        return fileUtils.resolveFile(file, filename);
    }

    /**
     * get a file object from path name
     */
    public static File getFile(String path) {
        if (path == null)
            return null;
        File f = new File(path);
        return f;
    }

    public FileUtil() {
    }

    /**
     * Copy the src file to the destination.
     * 
     * @param src
     * @param dest
     * @throws FileNotFoundException
     * @throws IOException
     *             public static boolean copyFile(File src, File dest) throws
     *             FileNotFoundException, IOException { return copyFile(src,
     *             dest, -1); }
     */
    public static boolean copyFileByExtent(File src, File dest, long extent) throws FileNotFoundException, IOException {

        if (dest.exists()) {
            dest.delete();
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel fcin = null;
        FileChannel fcout = null;
        try {
            // get channels
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dest);
            fcin = fis.getChannel();
            fcout = fos.getChannel();

            if (extent < 0) {
                extent = fcin.size();
            }

            // do the file copy
            long trans = fcin.transferTo(0, extent, fcout);
            if (trans < extent) {
                return false;
            }
            return true;

        } finally {
            // finish up
            if (fcin != null) {
                fcin.close();
            }
            if (fcout != null) {
                fcout.close();
            }
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * Get a list of all files in directory that have passed prefix.
     * 
     * @param dir
     *            Dir to look in.
     * @param prefix
     *            Basename of files to look for. Compare is case insensitive.
     * @return List of files in dir that start w/ passed basename.
     */
    public static File[] getFilesWithPrefix(File dir, final String prefix) {
        FileFilter prefixFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().startsWith(prefix.toLowerCase());
            }
        };
        return dir.listFiles(prefixFilter);
    }

    /**
     * @param parent
     *            The directory that may be parent or ancestors
     * @param child
     *            The directory that may be child
     * @return true if the child directory is included by the parent directory
     */
    public static boolean isIncluded(File parent, File child) {
        File p = child;
        while (p != null) {
            if (p.equals(parent))
                return true;
            p = p.getParentFile();
        }
        return false;
    }

    /**
     * @param fromDir
     * @param toDir
     * @return true if both directories have the same files with the same size
     */
    public static boolean isDirectoryFilesCopied(File fromDir, File toDir) {
        if (fromDir == null || toDir == null)
            return false;
        if (fromDir.isFile())
            return false;
        if (toDir.isFile())
            return false;

        File[] fs = fromDir.listFiles();
        for (int i = 0; fs != null && i < fs.length; i++) {
            if (fs[i].isDirectory())
                continue;
            File b = new File(toDir, fs[i].getName());
            if (!b.exists())
                return false;
            if (b.length() != fs[i].length())
                return false;
        }
        return true;
    }

    public static void createNewFile(File f) throws IOException {
        fileUtils.createNewFile(f);
    }

    public static void main0(String[] args) throws IOException {
        long _start = System.currentTimeMillis();
        // make sure there are exactly two arguments
        if (args.length != 2) {
            System.err.println("Usage: FileUtil SRC-FILE-NAME DEST-DIR-NAME");
            System.exit(1);
        }
        // make sure the source file is indeed a readable file
        File srcFile = new File(args[0]);
        if (!srcFile.isFile() || !srcFile.canRead()) {
            System.err.println("Not a readable file: " + srcFile.getName());
            System.exit(1);
        }
        // make sure the second argument is a directory
        File destDir = new File(args[1]);
        if (!destDir.isDirectory()) {
            System.err.println("Not a directory: " + destDir.getName());
            System.exit(1);
        }
        // create File object for destination file
        File destFile = new File(destDir, srcFile.getName());

        // copy file, optionally creating a checksum
        copyFile(srcFile, destFile);
        long time = System.currentTimeMillis() - _start;
        System.err.println("Total: " + time + "seconds ");
        System.err.println("Speed: " + destFile.length() / time + "KB/s");
    }

    public static String joinBy(String sep, String... files) {
        StringBuilder sb = new StringBuilder();
        if (files != null) {
            boolean needSep = false;
            for (int i = 0; i < files.length; i++) {
                if(U.isEmpty(files[i])) continue;
                if (needSep) {
                    sb.append(sep);
                }
                sb.append(files[i]);
                needSep = true;
            }
        }
        return sb.toString();
    }

    public static String join(String... files) {
        return joinBy(File.separator, files);
    }

    public static File resolveFile(File baseFile, String... files) {
        return resolveFile(baseFile, join(files));
    }

    public static void main(String[] args) throws IOException {
        long _start = System.currentTimeMillis();
        // make sure the source file is indeed a readable file
        URL fileURL = new URL(args[0]);

        // make sure the second argument is a directory
        File destDir = new File("./tmp");
        if (!destDir.isDirectory()) {
            System.err.println("Not a directory: " + destDir.getName());
            System.exit(1);
        }
        // create File object for destination file
        File destFile = new File(destDir, "_1.cfs");

        // copy file, optionally creating a checksum
        copyFile(fileURL.openStream(), destFile, 1024);
        long time = System.currentTimeMillis() - _start;
        System.err.println("Total: " + time + "seconds ");
        System.err.println("Speed: " + destFile.length() / time + "KB/s");
    }
    
    /* This Method is used to write a property in Properties File. */
    public static void writeProperty(String key, String value, String fileName) throws IOException, FileNotFoundException {
		Properties properties = new Properties();
		OutputStream out = null;
		InputStream in = null;
		in = new FileInputStream(fileName);
		properties.load(in);
		properties.setProperty(key, value);
		out = new FileOutputStream(fileName);
	    properties.setProperty(key, value);
	    properties.store(out, null);
		out.close();
	}
	
	/* This Method is used to read a property from the Properties File. */
	public static String readProperty(String key, String fileName) throws IOException, FileNotFoundException {
		String value = null;
		Properties properties = new Properties();
		InputStream in = null;
		in = new FileInputStream(fileName);
		properties.load(in);
		value = (String) properties.get(key);
		in.close();
		return value;
	}
}
