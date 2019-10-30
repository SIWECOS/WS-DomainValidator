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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import de.rub.nds.siwecos.validator.crawler.Crawler;
import de.rub.nds.siwecos.validator.dns.DnsQuery;
import de.rub.nds.siwecos.validator.json.TestResult;
import de.rub.nds.siwecos.validator.ws.ScanRequest;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.xbill.DNS.MXRecord;

/**
 *
 * @author Robert Merget <robert.merget@rub.de>
 */
public class ValidatorCallback implements Runnable {

    protected static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(ValidatorCallback.class
            .getName());

    private final ScanRequest request;

    private List<String> prioDomainStrings;

    public ValidatorCallback(ScanRequest request) {
        this.request = request;
        prioDomainStrings = new LinkedList<>();
        prioDomainStrings.add("app");
        prioDomainStrings.add("admin");
        prioDomainStrings.add("blog");
        prioDomainStrings.add("cms");
        prioDomainStrings.add("drupal");
        prioDomainStrings.add("impressum");
        prioDomainStrings.add("joomla");
        prioDomainStrings.add("typo3");
        prioDomainStrings.add("wordpress");
        prioDomainStrings.add("wp-content");
        prioDomainStrings.add("wiki");
        prioDomainStrings.add("tools");
        prioDomainStrings.add("jobs");
        prioDomainStrings.add("forum");
        prioDomainStrings.add("download");
        prioDomainStrings.add("shop");
        prioDomainStrings.add("contact");
        prioDomainStrings.add("kontakt");
        prioDomainStrings.add("wp-admin");
        prioDomainStrings.add("login");
        String additionalString = System.getenv("ADDITIONAL_PRIO_STRINGS");
        if (additionalString != null) {
            String[] split = additionalString.split(",");
            for (String s : split) {
                prioDomainStrings.add(s);
            }
        }
    }

    private String callbackUrlsToId(String[] urls) {
        StringBuilder builder = new StringBuilder();
        for (String s : urls) {
            builder.append(s);
        }
        return "" + Math.abs(builder.toString().hashCode());
    }

    @Override
    public void run() {
        LOGGER.info("Validating: " + request.getDomain());
        request.setDomain(request.getDomain().toLowerCase());
        if (request.getAllowSubdomains() == null) {
            request.setAllowSubdomains(Boolean.TRUE);
        }
        if (request.getUserAgent() == null) {
            request.setUserAgent("Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0)");
        }
        String[] schemes = { "http", "https" };
        if (!(request.getDomain().toLowerCase().contains("http") || request.getDomain().toLowerCase().contains("https"))) {
            LOGGER.info("No protocol specified for " + request.getDomain() + " assuming http");
            request.setDomain("http://" + request.getDomain());
        }
        UrlValidator urlValidator = new UrlValidator(schemes);
        LOGGER.info("Testing:" + request.getDomain());
        String idn;
        try {
            idn = convertUnicodeURLToAscii(request.getDomain());
            LOGGER.info("Converted to :" + idn);
            request.setDomain(idn);
        } catch (URISyntaxException ex) {
            LOGGER.info("Conversion failed", ex);
        }
        boolean valid = urlValidator.isValid(request.getDomain());
        if (!valid) {
            LOGGER.info("URL:" + request.getDomain() + " is not valid for us");
            TestResult result = new TestResult("Validator", false, null, request.getDomain(), null, false, null, null,
                    null);
            answer(result);
            return;
        }
        try {

            URI uri = null;

            Boolean syntaxCorrect = false;
            String domain = null;
            try {
                uri = new URI(request.getDomain());
                syntaxCorrect = true;
                domain = uri.getHost();
                if (domain == null) {
                    domain = request.getDomain();
                }
            } catch (URISyntaxException E) {
                syntaxCorrect = false;
                LOGGER.warn(E);
            }
            boolean dnsResolves = DnsQuery.isDnsResolvable(domain);
            String targetUrl = request.getDomain();
            Boolean isRedirecting = null;
            Boolean isConnectableHTTP = false;
            Integer statusCode = null;
            if (dnsResolves) {
                try {
                    RedirectEvaluator evaluator = new RedirectEvaluator(request.getDomain(), request.getUserAgent());
                    if (evaluator.isRedirecting()) {
                        targetUrl = evaluator.getNewUrl();
                    }
                    statusCode = evaluator.getStatusCode();
                    isConnectableHTTP = evaluator.isCouldConnect();
                    isRedirecting = evaluator.isRedirecting();
                } catch (Exception E) {
                    LOGGER.info("Could not retrieve status code for redirection evaluation.");
                    // Could not check if redirection is present checking with
                    // different protocol
                    if (request.getDomain().toLowerCase().contains("http://")) {
                        request.setDomain(request.getDomain().replace("http://", "https://"));
                        LOGGER.info("Rechecking with HTTPS");
                        RedirectEvaluator evaluator = new RedirectEvaluator(request.getDomain(), request.getUserAgent());
                        if (evaluator.isRedirecting()) {
                            targetUrl = evaluator.getNewUrl();
                        }

                        statusCode = evaluator.getStatusCode();
                        isConnectableHTTP = evaluator.isCouldConnect();
                        isRedirecting = evaluator.isRedirecting();

                    } else if (request.getDomain().contains("https://")) {
                        LOGGER.info("Rechecking with HTTP");
                        request.setDomain(request.getDomain().replace("https://", "http://"));

                        RedirectEvaluator evaluator = new RedirectEvaluator(request.getDomain(), request.getUserAgent());
                        if (evaluator.isRedirecting()) {
                            targetUrl = evaluator.getNewUrl();
                        }
                        isRedirecting = evaluator.isRedirecting();
                    } else {
                        LOGGER.error("Cannot retrieve statuscode, likely no http/https supported");

                        isRedirecting = null;
                    }
                }
            }
            List<URI> mailUrlList;
            mailUrlList = new LinkedList<>();
            if (dnsResolves) {
                List<MXRecord> mxRecords = DnsQuery.getMxRecords(domain);
                for (MXRecord mxRecord : mxRecords) {
                    mailUrlList.add(new URI(mxRecord.getTarget().toString()
                            .substring(0, mxRecord.getTarget().toString().length() - 1)));
                }
            }
            List<URI> crawledDomains = new LinkedList<>();
            if (Objects.equals(request.getCrawl(), Boolean.TRUE)) {
                Crawler crawler = new Crawler(request.getDomain());
                if (request.getMaxCount() == null) {
                    request.setMaxCount(10);
                }

                if (request.getMaxDepth() == null) {
                    request.setMaxDepth(4);
                }

                List<URI> tempCrawledUrls = crawler.crawl(request.getMaxCount(), request.getMaxDepth(),
                        request.getUserAgent());
                int i = 0;

                LOGGER.info("Filtering URI's...");

                for (URI tempUri : tempCrawledUrls) {
                    tempUri.normalize();
                    if (urlValidator.isValid(tempUri.toURL().toString()) && i < request.getMaxCount()) {
                        for (String s : prioDomainStrings) {
                            String urlToScan = targetUrl.replace("https://", "").replace("http://", "");
                            boolean isNonSubDomain = urlToScan.equals(tempUri.getHost());
                            if (tempUri.getPath() != null && tempUri.getPath().contains(s)
                                    && (isNonSubDomain || request.getAllowSubdomains())) {
                                crawledDomains.add(tempUri.normalize());
                                i++;
                                break;
                            }
                        }
                    }
                }
                String urlToScan = targetUrl.replace("https://", "").replace("http://", "").replace("/", "");
                for (URI tempUri : tempCrawledUrls) {

                    if (urlValidator.isValid(tempUri.toString())) {
                        if (i < request.getMaxCount()) {
                            if (!crawledDomains.contains(tempUri.normalize())) {
                                if (urlToScan.equals(tempUri.getHost()) || request.getAllowSubdomains()) {
                                    crawledDomains.add(tempUri.normalize());
                                    LOGGER.debug("Added:" + tempUri.normalize().toASCIIString());
                                    i++;
                                } else {
                                    LOGGER.debug("Filtered:" + tempUri.normalize().toASCIIString()
                                            + " - subdomain not conform");
                                }
                            } else {
                                LOGGER.debug("Filtered:" + tempUri.normalize().toASCIIString() + " - duplicate");
                            }
                        } else {
                            LOGGER.debug("Filtered:" + tempUri.normalize().toASCIIString() + " - already got enough ");
                        }
                    } else {
                        LOGGER.debug("Filtered:" + tempUri.normalize().toASCIIString() + " - not valid");
                    }
                    LOGGER.info("Filtering URI's finished");
                }
            }

            TestResult result = new TestResult("Validator", false, domain, request.getDomain(), targetUrl,
                    syntaxCorrect, dnsResolves, isRedirecting, mailUrlList);
            result.setCrawledUrls(crawledDomains);
            result.setHttpStatusCode(statusCode);
            result.setHttpCouldConnect(isConnectableHTTP);
            LOGGER.debug("Finished validation. Calling back");
            answer(result);
        } catch (Exception E) {
            LOGGER.error("Could not validate " + request.getDomain(), E);
            TestResult result = new TestResult("Validator", true, null, request.getDomain(), null, null, null, null,
                    null);
            answer(result);
        }
    }

    public void answer(TestResult result) {
        LOGGER.debug("Answering..." + Arrays.toString(this.request.getCallbackurls()));
        for (String callback : request.getCallbackurls()) {
            LOGGER.info("Calling back: " + callback);
            try {
                URL url = new URL(callback);
                URLConnection con = url.openConnection();
                HttpURLConnection http = (HttpURLConnection) con;
                con.setConnectTimeout(10000);
                http.setRequestMethod("POST");
                http.setDoInput(true);
                http.setDoOutput(true);
                String json = testResultToJson(result);

                http.setFixedLengthStreamingMode(json.getBytes().length);
                http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                http.connect();
                try (OutputStream os = http.getOutputStream()) {
                    os.write(json.getBytes("UTF-8"));
                    os.flush();
                }
                LOGGER.debug(json);
                http.disconnect();
            } catch (Exception ex) {
                LOGGER.warn("Failed to callback:" + callback, ex);
            }
        }
    }

    private String convertUnicodeURLToAscii(String url) throws URISyntaxException {
        if (url != null) {
            url = url.trim();
            // Handle international domains by detecting non-ascii and
            // converting them to punycode
            boolean isAscii = CharMatcher.ASCII.matchesAllOf(url);
            if (!isAscii) {
                URI uri = new URI(url);
                boolean includeScheme = true;

                // URI needs a scheme to work properly with authority parsing
                if (uri.getScheme() == null) {
                    uri = new URI("http://" + url);
                    includeScheme = false;
                }

                String scheme = uri.getScheme() != null ? uri.getScheme() + "://" : null;
                String authority = uri.getRawAuthority() != null ? uri.getRawAuthority() : ""; // includes
                // domain
                // and
                // port
                String path = uri.getRawPath() != null ? uri.getRawPath() : "";
                String queryString = uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "";

                // Must convert domain to punycode separately from the path
                url = (includeScheme ? scheme : "") + IDN.toASCII(authority) + path + queryString;

                // Convert path from unicode to ascii encoding
                url = new URI(url).toASCIIString();
            }
        }
        return url;
    }

    public String testResultToJson(TestResult result) {
        ObjectMapper ow = new ObjectMapper();
        String json = "";
        try {
            json = ow.writeValueAsString(result);
        } catch (JsonProcessingException ex) {
            LOGGER.error("Could not convert to json", ex);
        }
        return json;
    }
}
