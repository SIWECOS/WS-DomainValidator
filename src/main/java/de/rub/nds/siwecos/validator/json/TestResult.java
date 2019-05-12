/**
 *  SIWECOS-TLS-Scanner - A Webservice for the TLS-Scanner Module of TLS-Attacker
 *
 *  Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 *  Licensed under Apache License 2.0
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package de.rub.nds.siwecos.validator.json;

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

    private Boolean hostIsAlive;

    private Boolean httpRedirect;

    private List<URL> mailServerUrlList;

    public TestResult(String name, Boolean hasError, String domain, String originalUrl, String urlToScan, Boolean urlIsSyntacticalOk, Boolean hostIsAlive, Boolean httpRedirect, List<URL> mailServerUrlList) {
        this.name = name;
        this.hasError = hasError;
        this.domain = domain;
        this.originalUrl = originalUrl;
        this.urlToScan = urlToScan;
        this.urlIsSyntacticalOk = urlIsSyntacticalOk;
        this.hostIsAlive = hostIsAlive;
        this.httpRedirect = httpRedirect;
        this.mailServerUrlList = mailServerUrlList;
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

    public Boolean getHostIsAlive() {
        return hostIsAlive;
    }

    public void setHostIsAlive(Boolean hostIsAlive) {
        this.hostIsAlive = hostIsAlive;
    }

    public Boolean getHttpRedirect() {
        return httpRedirect;
    }

    public void setHttpRedirect(Boolean httpRedirect) {
        this.httpRedirect = httpRedirect;
    }

    public List<URL> getMailServerUrlList() {
        return mailServerUrlList;
    }

    public void setMailServerUrlList(List<URL> mailServerUrlList) {
        this.mailServerUrlList = mailServerUrlList;
    }
}
