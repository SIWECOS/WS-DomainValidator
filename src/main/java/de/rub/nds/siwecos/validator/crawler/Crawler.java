/**
 *  SIWECOS-Host-Validator - A Webservice for the Siwecos Infrastructure to validate user provided hosts
 *
 *  Copyright 2019 Ruhr University Bochum / Hackmanit GmbH
 *
 *  Licensed under Apache License 2.0
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.rub.nds.siwecos.validator.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import java.net.URI;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author ic0ns
 */
public class Crawler extends WebCrawler {

    private Logger LOGGER = LogManager.getLogger();

    private String startUri;

    public Crawler(String startUri) {
        this.startUri = startUri;
    }

    public List<URI> crawl(int maxResults, int depth) {
        CrawlConfig config = new CrawlConfig();

        // Set the folder where intermediate crawl data is stored (e.g. list of urls that are extracted from previously
        // fetched pages and need to be crawled later).
        config.setCrawlStorageFolder("/tmp/crawler4j/");

        // Be polite: Make sure that we don't send more than 1 request per second (1000 milliseconds between requests).
        // Otherwise it may overload the target servers.
        config.setPolitenessDelay(5);

        // You can set the maximum crawl depth here. The default value is -1 for unlimited depth.
        config.setMaxDepthOfCrawling(2);

        // You can set the maximum number of pages to crawl. The default value is -1 for unlimited number of pages.
        config.setMaxPagesToFetch(10);

        // Should binary data should also be crawled? example: the contents of pdf, or the metadata of images etc
        config.setIncludeBinaryContentInCrawling(false);
        config.setResumableCrawling(false);
        config.setFollowRedirects(true);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller;
        try {
            controller = new CrawlController(config, pageFetcher, robotstxtServer);
            controller.addSeed(startUri);
            int numberOfCrawlers = 1;
            CrawlController.WebCrawlerFactory<WebCrawler> factory = () -> new WebCrawler();
            controller.start(factory, numberOfCrawlers);
            controller.waitUntilFinish();
            List<Object> crawlersLocalData = controller.getCrawlersLocalData();
            long totalLinks = 0;
            long totalTextSize = 0;
            int totalProcessedPages = 0;
            for (Object localData : crawlersLocalData) {
                CrawlStat stat = (CrawlStat) localData;
                totalLinks += stat.getTotalLinks();
                totalTextSize += stat.getTotalTextSize();
                totalProcessedPages += stat.getTotalProcessedPages();
            }

            logger.info("Aggregated Statistics:");
            logger.info("\tProcessed Pages: {}", totalProcessedPages);
            logger.info("\tTotal Links found: {}", totalLinks);
            logger.info("\tTotal Text Size: {}", totalTextSize);
        } catch (Exception ex) {
            LOGGER.error(ex);
        }

        return null;
    }
}
