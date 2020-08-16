
package com.jbo.elasticsearch.auth.utils;

import com.jbo.elasticsearch.auth.auth.user.AuthUser;
import org.elasticsearch.common.logging.ESLogger;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

public class BasicAuthHelper {

    public static AuthUser extractCredentials(String authorizationHeader, ESLogger log) {

        if (authorizationHeader != null) {
            if (authorizationHeader.trim().toLowerCase().startsWith("basic ")) {
                String decodedBasicHeader = new String(DatatypeConverter.parseBase64Binary(authorizationHeader.split(" ")[1]),
                        StandardCharsets.UTF_8);

                String username = null;
                String password = null;
                int firstColonIndex = decodedBasicHeader.indexOf(':');
                if (firstColonIndex > 0) {
                    username = decodedBasicHeader.substring(0, firstColonIndex);
                    if(decodedBasicHeader.length() - 1 != firstColonIndex) {
                        password = decodedBasicHeader.substring(firstColonIndex + 1);
                    }
                }

                if (username != null && password != null) {
                    return new AuthUser(username, password);
                }
            }
        }
        log.trace("No 'Authorization' header, send 401 and 'WWW-Authenticate Basic'");
        return null;
    }

}
