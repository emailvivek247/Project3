package net.javacoding.xsearch;

import java.io.File;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.AffectedDirectoryGroup;
import net.javacoding.xsearch.status.IndexStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.admin.ui.action.constants.IndexType;

public abstract class AbstractReIndexing {

    private static final Logger logger = LoggerFactory.getLogger(AbstractReIndexing.class);

    protected final DatasetConfiguration dc;
    protected final AffectedDirectoryGroup adg;
    
    protected final File workingDir;
    protected boolean running = true;

    protected int branchCount = 1;

    public AbstractReIndexing(DatasetConfiguration dc, AffectedDirectoryGroup adg) {

        this.dc = dc;
        this.adg = adg;

        workingDir = IndexStatus.findNonActiveMainDirectoryFile(dc);

        adg.setNewDirectory(workingDir);
        adg.addOldDirectory(dc.getTempIndexDirectoryFile());
        adg.addOldDirectory(dc.getAltTempIndexDirectoryFile());
        adg.addOldDirectory(IndexStatus.findActiveMainDirectoryFile(dc));

        logger.info("Opened index " + dc.getName());
        logger.info("Working in " + workingDir);
    }

    public static AffectedDirectoryGroup start(DatasetConfiguration dc) {
        AffectedDirectoryGroup adg = new AffectedDirectoryGroup();
        AbstractReIndexing reIndexing = null;
        if (dc.getIndexType() == null || dc.getIndexType() == IndexType.LUCENE) {
            reIndexing = new ReIndexing(dc, adg);
        } else if (dc.getIndexType() == IndexType.ELASTICSEARCH) {
            reIndexing = new ESReIndexing(dc, adg);
        }
        reIndexing.reIndexing();
        reIndexing.close();
        return adg;
    }

    public int getBranchCount() {
        return branchCount > 0 ? branchCount : 1;
    }

    public abstract void close();

    public abstract void reIndexing();

    public abstract Thread addShutdownHook();

    public void removeShutdownHook(Thread hook) {
        Runtime.getRuntime().removeShutdownHook(hook);
    }

}