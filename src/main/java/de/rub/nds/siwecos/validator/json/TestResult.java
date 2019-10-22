/**
 *  SIWECOS-Host-Validator - A Webservice for the Siwecos Infrastructure to validate user provided hosts
 *
 *  Copyright 2019 Ruhr University Bochum / Hackmanit GmbH
 *
 *  Licensed under Apache License 2.0
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package de.rub.nds.siwecos.validator.json;

import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 *
 * @author Robert Merget - robert.merget@rub.de
 */
public class TestResult {

    private String name;

    private Boolean hasError;

    private String domain;

    private String originalUrl;

    private String urlToScan;

    private Boolean urlIsSyntacticalOk;

    private Boolean dnsResolves;

    private Boolean httpCouldConnect;

    private Integer httpStatusCode;

    private Boolean httpRedirect;

    private List<URI> mailServerDomainList;

    private List<URI> crawledUrls;

    public TestResult(String name, Boolean hasError, String domain, String originalUrl, String urlToScan,
            Boolean urlIsSyntacticalOk, Boolean dnsResolves, Boolean httpRedirect, List<URI> mailServerDomainList) {
        this.name = name;
        this.hasError = hasError;
        this.domain = domain;
        this.originalUrl = originalUrl;
        this.urlToScan = urlToScan;
        this.urlIsSyntacticalOk = urlIsSyntacticalOk;
        this.dnsResolves = dnsResolves;
        this.httpRedirect = httpRedirect;
        this.mailServerDomainList = mailServerDomainList;
    }

    public List<URI> getCrawledUrls() {
        return crawledUrls;
    }

    public void setCrawledUrls(List<URI> crawledUrls) {
        this.crawledUrls = crawledUrls;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getHasError() {
        return hasError;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getUrlToScan() {
        return urlToScan;
    }

    public void setUrlToScan(String urlToScan) {
        this.urlToScan = urlToScan;
    }

    public Boolean getUrlIsSyntacticalOk() {
        return urlIsSyntacticalOk;
    }

    public void setUrlIsSyntacticalOk(Boolean urlIsSyntacticalOk) {
        this.urlIsSyntacticalOk = urlIsSyntacticalOk;
    }

    public Boolean getDnsResolves() {
        return dnsResolves;
    }

    public void setDnsResolves(Boolean dnsResolves) {
        this.dnsResolves = dnsResolves;
    }

    public Boolean getHttpRedirect() {
        return httpRedirect;
    }

    public void setHttpRedirect(Boolean httpRedirect) {
        this.httpRedirect = httpRedirect;
    }

    public List<URI> getMailServerDomainList() {
        return mailServerDomainList;
    }

    public void setMailServerDomainList(List<URI> mailServerDomainList) {
        this.mailServerDomainList = mailServerDomainList;
    }

    public Boolean getHttpCouldConnect() {
        return httpCouldConnect;
    }

    public void setHttpCouldConnect(Boolean httpCouldConnect) {
        this.httpCouldConnect = httpCouldConnect;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

}
