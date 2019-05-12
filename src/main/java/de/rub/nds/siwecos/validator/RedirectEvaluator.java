/*
 */
package de.rub.nds.siwecos.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RedirectEvaluator {

    private boolean redirecting = false;

    private String newUrl = null;

    public RedirectEvaluator(String url, String useragent) throws MalformedURLException, IOException {
        URL obj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
        conn.setReadTimeout(5000);
        conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.addRequestProperty("User-Agent", useragent);
        conn.addRequestProperty("Referer", "siwecos.de");
        System.out.println("Request URL ... " + url);
        boolean redirect = false;
        // normally, 3xx is redirect
        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER) {
                redirect = true;
                newUrl = conn.getHeaderField("Location");

            }
        }
    }

    public boolean isRedirecting() {
        return redirecting;
    }

    public String getNewUrl() {
        return newUrl;
    }
}
