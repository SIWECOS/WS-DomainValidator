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
import de.rub.nds.siwecos.validator.crawler.Crawler;
import de.rub.nds.siwecos.validator.dns.DnsQuery;
import de.rub.nds.siwecos.validator.json.TestResult;
import de.rub.nds.siwecos.validator.ws.ScanRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.validator.UrlValidator;
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
        System.out.println("" + Arrays.toString(request.getCallbackurls()));
        if (request.getAllowSubdomains() == null) {
            request.setAllowSubdomains(Boolean.TRUE);
        }
        if (request.getUserAgent() == null) {
            request.setUserAgent("Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0)");
        }
        String[] schemes = {"http", "https"};
        if (!(request.getDomain().toLowerCase().contains("http") || request.getDomain().toLowerCase().contains("https"))) {
            LOGGER.info("No protocol specified for " + request.getDomain() + " assuming http");
            request.setDomain("http://" + request.getDomain());
        }
        UrlValidator urlValidator = new UrlValidator(schemes);
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
                LOGGER.warn(E);
            }
            boolean dnsResolves = DnsQuery.isDnsResolvable(domain);
            String targetUrl = request.getDomain();
            Boolean isRedirecting = null;
            if (dnsResolves) {
                RedirectEvaluator evaluator = new RedirectEvaluator(request.getDomain(), request.getUserAgent());
                if (evaluator.isRedirecting()) {
                    targetUrl = evaluator.getNewUrl();
                }
                isRedirecting = evaluator.isRedirecting();
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
                            if (tempUri.getPath() != null && tempUri.getPath().contains(s)) {
                                if (tempUri.getHost().equals(targetUrl) || request.getAllowSubdomains() == Boolean.TRUE) {
                                    crawledDomains.add(tempUri.normalize());
                                    i++;
                                    break;
                                }

                            }
                        }
                    }
                }
                for (URI tempUri : tempCrawledUrls) {
                    if (urlValidator.isValid(tempUri.toURL().toString()) && i < request.getMaxCount() && !crawledDomains.contains(tempUri.normalize())) {
                        if (tempUri.getHost().equals(targetUrl) || request.getAllowSubdomains() == Boolean.TRUE) {

                            crawledDomains.add(tempUri.normalize());
                            i++;
                        }
                    }
                }
                LOGGER.info("Filtering URI's finished");
            }

            TestResult result = new TestResult("Validator", false, domain, request.getDomain(), targetUrl,
                    syntaxCorrect, dnsResolves, isRedirecting, mailUrlList);
            result.setCrawledUrls(crawledDomains);
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
            } catch (IOException ex) {
                LOGGER.warn("Failed to callback:" + callback, ex);
            }
        }
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
