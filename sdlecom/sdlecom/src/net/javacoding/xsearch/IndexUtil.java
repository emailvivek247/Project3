package net.javacoding.xsearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.exception.ConfigurationException;
import net.javacoding.xsearch.foundation.LoggerPrintStream;
import net.javacoding.xsearch.utility.FileUtil;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.common.util.TIFFToPDFConverter;

public class IndexUtil {
	
	private static Logger logger = LoggerFactory.getLogger(IndexUtil.class);

    private String[]             commands = null;
    
    private void init() throws IOException, ConfigurationException {
        ServerConfiguration.setServerConfigFile("data/xsearch-config.xml");
    }

    public static void main(String args[]) {
        IndexUtil iu = new IndexUtil();
        try {
            iu.parseArgs(args);
            iu.start();
        } catch (Exception e) {
            System.err.println(e);
            usage();
        } finally {
            iu.finish();
        }
    }

    /*
     * Parse command line arguments Code inspired by http://www.ecs.umass.edu/ece/wireless/people/emmanuel/java/java/cmdLineArgs/parsing.html
     */
    private void parseArgs(String[] args) {
        String arg = null;
        int i = 0;
        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];
            if (i == args.length) usage();
            if (arg.equals("-config")) {
                //config = args[i++];
            } else if (arg.equals("-index")) {
                //index = args[i++];
            }
        }
        if (i == args.length)
            usage();
        else {
            commands = new String[args.length - i];
            System.arraycopy(args, i, commands, 0, args.length - i);
        }

        try {
            init();
        } catch (IOException ioe) {
            logger.error("Failed to access log file:" + ioe);
            usage();
        } catch (ConfigurationException ce) {
            logger.error("Failed to access the configuration or index you specified:" + ce);
            usage();
        }
    }

    public static void usage() {
        //System.out.println("Usage: java -jar fdt.jar net.javacoding.xsearch.IndexUtil split <index-name> <number_of_splits>");
        System.out.println("Usage: java net.javacoding.xsearch.IndexUtil merge -O <destination_directory> [-optimizeTo <number_of_segments>]<index_name_1> <index_name_2> [...]");
        System.exit(1);
    }

    public void finish() {
    }
    public void start() {
        long start = System.currentTimeMillis();
        int i = 0;
        String command = commands[i++];
        try {
            if ("merge".equals(command)) {
                File destDir = null;
                DatasetConfiguration dc = null;
                int optimizeTo = 0;
                while (i < commands.length && commands[i].startsWith("-")) {
                    String option = commands[i++];
                    if (i == commands.length) usage();
                    if (option.equals("-O")) {
                        destDir = new File(commands[i++]);
                    } else if (option.equals("-optimizeTo")) {
                        optimizeTo = Integer.parseInt(commands[i++]);
                    }
                }
                List<Directory> dirs = new ArrayList<Directory>();
                for (;i < commands.length;i++) {
                    dc = ServerConfiguration.getDatasetConfiguration(commands[i]);
                    if(dc!=null&&IndexReader.indexExists(dc.getMainIndexDirectoryFile())) {
                        dirs.add(FSDirectory.getDirectory(dc.getMainIndexDirectoryFile()));
                    }else {
                        logger.warn("No index found for index:"+commands[i]);
                    }
                }
                IndexWriter indexWriter = null;
                logger.info("Working in " + destDir);
                FileUtil.deleteAllFiles(destDir);
                destDir.mkdirs();
                //this.indexWriter = new IndexWriter(FSDirectory.getDirectory(destDir), false, dc.getAnalyzer(), true);
                indexWriter = new IndexWriter(destDir, dc.getAnalyzer(), true, MaxFieldLength.UNLIMITED); //false);
                indexWriter.setMergeFactor( dc.getMergeFactor() );
                // this.indexWriter.minMergeDocs = 1000;
                indexWriter.setRAMBufferSizeMB(dc.getDocumentBufferSizeMB()*dc.getWriterThreadsCount());
                indexWriter.setMaxFieldLength(dc.getMaxFieldLength());
                indexWriter.setUseCompoundFile(true);
                indexWriter.setMaxMergeDocs(dc.getMaxMergeDocs());
                indexWriter.setInfoStream(new LoggerPrintStream(logger));
                Directory[] directories = new Directory[dirs.size()];
                indexWriter.addIndexesNoOptimize((Directory[]) (dirs.toArray(directories)));
                if(optimizeTo>0) {
                    indexWriter.optimize(optimizeTo);
                }
                indexWriter.getInfoStream().close();
                indexWriter.close();
            }else {
                logger.info("Unknown " + command);
                usage();
            }
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        logger.info("------- " + command + ":completed, Time used: " + (System.currentTimeMillis() - start)/1000 +" seconds");
    }
}
