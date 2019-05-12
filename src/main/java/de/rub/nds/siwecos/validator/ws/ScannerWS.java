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

import de.rub.nds.siwecos.validator.DebugManager;
import de.rub.nds.siwecos.validator.RedirectEvaluator;
import de.rub.nds.siwecos.validator.ValidatorCallback;
import de.rub.nds.siwecos.validator.json.TestResult;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;

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
        LOGGER.info("Validatating: " + request.getUrl());
        try {
            URL url = null;

            Boolean syntaxCorrect;
            String domain = null;
            try {
                url = new URL(request.getUrl());
                syntaxCorrect = true;
                domain = url.getHost();

            } catch (MalformedURLException E) {
                syntaxCorrect = false;
            }
            RedirectEvaluator evaluator = new RedirectEvaluator(request.getUrl(), request.getUseragent());
            String targetUrl = request.getUrl();
            if (evaluator.isRedirecting()) {
                url = evaluator.getNewUrl();
            }
            TestResult result = new TestResult("Validator", false, domain, request.getUrl(), url, syntaxCorrect, syntaxCorrect, syntaxCorrect, mailServerUrlList), urlToScan, true, true, true, mailServerUrlList
            )
            return Response.status(Response.Status.OK).entity("Success").type(MediaType.APPLICATION_JSON).build();

        } catch (Exception E) {
            TestResult result = new TestResult("Validator", true, null, request.getUrl(), null, null, null, null, null);
            return Response.status(Response.Status.OK).entity("Success").type(MediaType.APPLICATION_JSON).build();
        }
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
