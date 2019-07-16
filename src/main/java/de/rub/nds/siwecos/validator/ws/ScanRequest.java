/**
 *  SIWECOS-Host-Validator - A Webservice for the Siwecos Infrastructure to validate user provided hosts
 *
 *  Copyright 2019 Ruhr University Bochum / Hackmanit GmbH
 *
 *  Licensed under Apache License 2.0
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package de.rub.nds.siwecos.validator.ws;

/**
 *
 * @author Robert Merget <robert.merget@rub.de>
 */
public class ScanRequest {

    private String url;

    private String userAgent;

    private Boolean crawl;

    private Integer maxCount;

    private Integer maxDepth;

    public ScanRequest() {
    }

    public ScanRequest(String url, String userAgent, Boolean noCrawl, Integer maxCount, Integer maxDepth) {
        this.url = url;
        this.userAgent = userAgent;
        this.crawl = noCrawl;
        this.maxCount = maxCount;
        this.maxDepth = maxDepth;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Boolean getCrawl() {
        return crawl;
    }

    public void setCrawl(Boolean crawl) {
        this.crawl = crawl;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public Integer getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public String toString() {
        return "ScanRequest{" + "url=" + url + ", userAgent=" + userAgent + ", noCrawl=" + crawl + ", maxCount=" + maxCount + ", maxDepth=" + maxDepth + '}';
    }

}
