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

    public ScanRequest() {
    }

    public ScanRequest(String url, String useragent) {
        this.url = url;
        this.userAgent = useragent;
    }

    public String getUrl() {
        return url;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return "ScanRequest{" + "url=" + url + ", useragent=" + userAgent + '}';
    }
}
