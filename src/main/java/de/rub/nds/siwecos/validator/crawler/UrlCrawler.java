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

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author ic0ns
 */
public class UrlCrawler extends WebCrawler {

    private final static Pattern EXCLUSIONS = Pattern
            .compile(".*(\\.(css|js|xml|gif|jpg|png|mp3|mp4|zip|gz|pdf|svg|rdf))$");

    private Set<WebURL> links = new HashSet<>();

    @Override
    public Object getMyLocalData() {
        return links;
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            for (WebURL tempUrl : htmlParseData.getOutgoingUrls()) {
                if (shouldVisit(page, tempUrl)) {
                    links.add(tempUrl);
                }
            }
        }
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String urlString = url.getURL().toLowerCase();
        return !EXCLUSIONS.matcher(urlString).matches()
                && referringPage.getWebURL().getDomain().equals(url.getDomain());
    }

}
