/**
 *  SIWECOS-Host-Validator - A Webservice for the Siwecos Infrastructure to validate user provided hosts
 *
 *  Copyright 2019 Ruhr University Bochum / Hackmanit GmbH
 *
 *  Licensed under Apache License 2.0
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package de.rub.nds.siwecos.validator.dns;

import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class DnsQuery {

    private static final Logger LOGGER = LogManager.getLogger();

    private DnsQuery() {
    }

    public static boolean isDnsResolvable(String hostname) {
        try {
            Lookup lookup = new Lookup(hostname, Type.ANY);
            lookup.run();
            if (lookup.getResult() != Lookup.SUCCESSFUL) {
                return false;
            }
            for (Record r : lookup.getAnswers()) {
                if (r.getType() == Type.A || r.getType() == Type.AAAA) {
                    return true;
                }
            }
            return false;
        } catch (TextParseException ex) {
            LOGGER.warn("Could not resolve DNS", ex);
        }
        return false;
    }

    public static List<MXRecord> getMxRecords(String hostname) {
        Record[] records;
        List<MXRecord> recordList = new LinkedList<>();
        try {
            records = new Lookup(hostname, Type.MX).run();
            if (records != null) {
                for (Record record : records) {
                    MXRecord mx = (MXRecord) record;
                    recordList.add(mx);
                }
            }
        } catch (TextParseException ex) {
            LOGGER.warn("Could not resolve DNS", ex);
            return null;
        }
        return recordList;
    }
}
