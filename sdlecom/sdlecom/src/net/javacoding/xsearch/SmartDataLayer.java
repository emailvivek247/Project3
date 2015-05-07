package net.javacoding.xsearch;

import java.io.File;
import java.util.ArrayList;

import net.javacoding.xsearch.config.ConfigurationHistory;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.search.SpellCheckManager;
import net.javacoding.xsearch.search.searcher.SearcherManager;
import net.javacoding.xsearch.utility.SchedulerTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.search.BooleanQuery;

public class SmartDataLayer {

	private static Logger logger   = LoggerFactory.getLogger(SmartDataLayer.class.getName());
	
    /**
     * called in ApplicaitonInitServlet.init
     */
    public static void init(File dbsHome) {
        try {
            ArrayList<DatasetConfiguration> dcs = ServerConfiguration.getDatasetConfigurations();
            for (DatasetConfiguration dc : dcs){
                logger.info("starting Searcher Manager for " + dc.getName() + "...");
                SearcherManager.init(dc);
            }
            logger.info("starting Spell Check Manager...");
            for (DatasetConfiguration dc : dcs){
                SpellCheckManager.start(dc);
                SpellCheckManager.warmup(dc);
            }
            for (DatasetConfiguration dc : dcs){
                ConfigurationHistory.saveDuringStartup(dc);
            }

            //system wide Lucene settings
            BooleanQuery.setAllowDocsOutOfOrder(true);

        } catch (Exception e) {
            logger.info("There is some error while starting Smart Data Layer" + e.toString());
            e.printStackTrace();
        }
    }

    public static void destroy() {
        SearcherManager.destroy();
        SchedulerTool.destroy();
    }

}
