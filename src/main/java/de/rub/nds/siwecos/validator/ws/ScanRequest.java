/**
 *  SIWECOS-TLS-Scanner - A Webservice for the TLS-Scanner Module of TLS-Attacker
 *
 *  Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
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

    private final String url;

    private final String useragent;

    public ScanRequest(String url, String useragent) {
        this.url = url;
        this.useragent = useragent;
    }

    public String getUrl() {
        return url;
    }

    public String getUseragent() {
        return useragent;
    }

    @Override
    public String toString() {
        return "ScanRequest{" + "url=" + url + ", useragent=" + useragent + '}';
    }
}
