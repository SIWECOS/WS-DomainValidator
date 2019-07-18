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

    private String domain;

    private String userAgent;

    private Boolean crawl;

    private Integer maxCount;

    private Integer maxDepth;

    private String[] callbackurls;

    private Boolean allowSubdomains;

    public ScanRequest() {
    }

    public ScanRequest(String domain, String userAgent, Boolean crawl, Integer maxCount, Integer maxDepth, String[] callbackurls, Boolean allowSubdomains) {
        this.domain = domain;
        this.userAgent = userAgent;
        this.crawl = crawl;
        this.maxCount = maxCount;
        this.maxDepth = maxDepth;
        this.callbackurls = callbackurls;
        this.allowSubdomains = allowSubdomains;
    }

    public Boolean getAllowSubdomains() {
        return allowSubdomains;
    }

    public void setAllowSubdomains(Boolean allowSubdomains) {
        this.allowSubdomains = allowSubdomains;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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

    public String[] getCallbackurls() {
        return callbackurls;
    }

    public void setCallbackurls(String[] callbackurls) {
        this.callbackurls = callbackurls;
    }

    @Override
    public String toString() {
        return "ScanRequest{" + "url=" + domain + ", userAgent=" + userAgent + ", noCrawl=" + crawl + ", maxCount="
                + maxCount + ", maxDepth=" + maxDepth + '}';
    }

}
