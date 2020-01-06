/**
 *  SIWECOS-Host-Validator - A Webservice for the Siwecos Infrastructure to validate user provided hosts
 *
 *  Copyright 2019 Ruhr University Bochum / Hackmanit GmbH
 *
 *  Licensed under Apache License 2.0
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package de.rub.nds.siwecos.validator.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.url.WebURL;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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

    public List<URI> crawl(int maxResults, int depth, String userAgent) {
        CrawlConfig config = new CrawlConfig();

        // Set the folder where intermediate crawl data is stored (e.g. list of urls that are extracted from previously
        // fetched pages and need to be crawled later).
        config.setCrawlStorageFolder("/tmp/crawler4j/");

        config.setPolitenessDelay(5);

        // You can set the maximum crawl depth here. The default value is -1 for unlimited depth.
        config.setMaxDepthOfCrawling(depth);

        // You can set the maximum number of pages to crawl. The default value is -1 for unlimited number of pages.
        config.setMaxPagesToFetch(maxResults);

        // Should binary data should also be crawled? example: the contents of pdf, or the metadata of images etc
        config.setIncludeBinaryContentInCrawling(false);
        config.setResumableCrawling(false);
        config.setFollowRedirects(true);
        config.setThreadShutdownDelaySeconds(0);
        config.setConnectionTimeout(10000);
        config.setThreadMonitoringDelaySeconds(1);
        config.setCleanupDelaySeconds(1);
        config.setShutdownOnEmptyQueue(true);
        config.setUserAgentString(userAgent);
        List<URI> uriList = new LinkedList<>();
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RegexPatchedRobotsTxtServer robotstxtServer = new RegexPatchedRobotsTxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller;
        try {
            controller = new CrawlController(config, pageFetcher, robotstxtServer);
            controller.addSeed(startUri);
            int numberOfCrawlers = 1;
            CrawlController.WebCrawlerFactory<WebCrawler> factory = () -> new UrlCrawler();
            controller.start(factory, numberOfCrawlers);
            controller.waitUntilFinish();
            List<Object> crawlersLocalData = controller.getCrawlersLocalData();
            for (Object localData : crawlersLocalData) {
                Set<WebURL> urlSet = (Set<WebURL>) localData;
                LOGGER.info("Crawled " + urlSet.size() + " urls");
                for (WebURL url : urlSet) {
                    uriList.add(new URI(url.getURL()));
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
        LOGGER.info("Returning urilist");
        return uriList;
    }
}
