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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rub.nds.siwecos.validator.DebugManager;
import de.rub.nds.siwecos.validator.RedirectEvaluator;
import de.rub.nds.siwecos.validator.crawler.Crawler;
import de.rub.nds.siwecos.validator.dns.DnsQuery;
import de.rub.nds.siwecos.validator.json.TestResult;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.validator.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.xbill.DNS.MXRecord;

/**
 *
 * @author Robert Merget - robert.merget@rub.de
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
public class ScannerWS {

    protected static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(ScannerWS.class.getName());

    @Context
    private UriInfo context;

    public ScannerWS() {
        Thread.currentThread().setName("Webservice-Thread");
    }

    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response scanHttps(ScanRequest request) throws URISyntaxException {
        LOGGER.info("Validating: " + request.getDomain());
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
            return Response.status(Response.Status.OK).entity(testResultToJson(result))
                    .type(MediaType.APPLICATION_JSON).build();
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
                    mailUrlList.add(new URI(mxRecord.getTarget().toString().substring(0, mxRecord.getTarget().toString().length() - 1)));
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

                List<URI> tempCrawledUrls = crawler.crawl(request.getMaxCount(), request.getMaxDepth(), request.getUserAgent());
                int i = 0;
                for (URI tempUri : tempCrawledUrls) {

                    if (urlValidator.isValid(tempUri.toURL().toString()) && i < request.getMaxCount()) {
                        crawledDomains.add(tempUri);
                        i++;
                    }
                }
            }

            TestResult result = new TestResult("Validator", false, domain, request.getDomain(), targetUrl, syntaxCorrect,
                    dnsResolves, isRedirecting, mailUrlList);
            result.setCrawledDomains(crawledDomains);
            return Response.status(Response.Status.OK).entity(testResultToJson(result))
                    .type(MediaType.APPLICATION_JSON).build();

        } catch (Exception E) {
            E.printStackTrace();
            LOGGER.warn(E);
            TestResult result = new TestResult("Validator", true, null, request.getDomain(), null, null, null, null, null);
            return Response.status(Response.Status.OK).entity(testResultToJson(result))
                    .type(MediaType.APPLICATION_JSON).build();
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

    @POST
    @Path("/toggleDebug")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getToggleDebug() throws URISyntaxException {
        DebugManager.getInstance().setDebugEnabled(!DebugManager.getInstance().isDebugEnabled());
        if (DebugManager.getInstance().isDebugEnabled()) {
            LOGGER.info("Switched DebugMode on");
            return Response.status(Response.Status.OK).entity("Switched DebugMode on").type(MediaType.TEXT_PLAIN_TYPE)
                    .build();
        } else {
            LOGGER.info("Switched DebugMode off");
            return Response.status(Response.Status.OK).entity("Switched DebugMode off").type(MediaType.TEXT_PLAIN_TYPE)
                    .build();
        }
    }
}
