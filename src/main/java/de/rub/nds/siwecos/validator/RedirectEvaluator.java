/**
 *  SIWECOS-Host-Validator - A Webservice for the Siwecos Infrastructure to validate user provided hosts
 *
 *  Copyright 2019 Ruhr University Bochum / Hackmanit GmbH
 *
 *  Licensed under Apache License 2.0
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package de.rub.nds.siwecos.validator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RedirectEvaluator {

    private Logger LOGGER = LogManager.getLogger();

    private Boolean redirecting = false;

    private boolean couldConnect;

    private Integer statusCode;

    private String newUrl = null;

    public RedirectEvaluator(String url, String useragent) throws MalformedURLException, IOException {
        URL obj = new URL(url);
        HttpURLConnection conn;
        if (!(obj.getProtocol().toUpperCase().equals("HTTPS") || obj.getProtocol().toUpperCase().equals("HTTP"))) {

            obj = new URL("https://" + url);
            try {
                conn = (HttpURLConnection) obj.openConnection();
                couldConnect = true;
            } catch (Exception E) {
                LOGGER.warn(E);
                obj = new URL("http://" + url);
                try {
                    conn = (HttpURLConnection) obj.openConnection();
                    couldConnect = true;
                } catch (Exception ex) {
                    LOGGER.warn("Could not establish connection to check for redirects", ex);
                    couldConnect = false;
                    return;
                }
            }
        } else {
            try {
                conn = (HttpURLConnection) obj.openConnection();
                couldConnect = true;
            } catch (Exception ex) {
                LOGGER.warn("Could not establish connection to check for redirects", ex);
                couldConnect = false;
                return;
            }
        }

        conn.setReadTimeout(5000);
        conn.setConnectTimeout(5000);
        conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.addRequestProperty("User-Agent", useragent);
        conn.addRequestProperty("Referer", "siwecos.de");
        redirecting = false;

        // normally, 3xx is redirect
        statusCode = conn.getResponseCode();
        if (statusCode != HttpURLConnection.HTTP_OK) {
            if (statusCode == HttpURLConnection.HTTP_MOVED_TEMP || statusCode == HttpURLConnection.HTTP_MOVED_PERM
                    || statusCode == HttpURLConnection.HTTP_SEE_OTHER) {
                redirecting = true;
                newUrl = conn.getHeaderField("Location");
            }
        }
    }

    public Boolean isRedirecting() {
        return redirecting;
    }

    public String getNewUrl() {
        return newUrl;
    }

    public boolean isCouldConnect() {
        return couldConnect;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

}
