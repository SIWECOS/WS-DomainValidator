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

    private String newUrl = null;

    public RedirectEvaluator(String url, String useragent) throws MalformedURLException, IOException {
        URL obj = new URL(url);
        HttpURLConnection conn;
        if (!(obj.getProtocol().toUpperCase().equals("HTTPS") || obj.getProtocol().toUpperCase().equals("HTTP"))) {

            obj = new URL("https://" + url);
            try {
                conn = (HttpURLConnection) obj.openConnection();
            } catch (Exception E) {
                LOGGER.warn(E);
                obj = new URL("http://" + url);
                try {
                    conn = (HttpURLConnection) obj.openConnection();
                } catch (Exception ex) {
                    LOGGER.warn("Could not establish connection to check for redirects", ex);
                    return;
                }
            }
        } else {
            conn = (HttpURLConnection) obj.openConnection();
        }

        conn.setReadTimeout(5000);
        conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.addRequestProperty("User-Agent", useragent);
        conn.addRequestProperty("Referer", "siwecos.de");
        redirecting = false;
        // normally, 3xx is redirect
        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER) {
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
}
