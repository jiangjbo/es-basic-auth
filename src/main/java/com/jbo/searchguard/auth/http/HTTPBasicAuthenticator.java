package com.jbo.searchguard.auth.http;

import com.jbo.searchguard.auth.user.AuthUser;
import com.jbo.searchguard.utils.BasicAuthHelper;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestRequest;

import java.util.Optional;

public class HTTPBasicAuthenticator implements HTTPAuthenticator {

    protected final ESLogger log = Loggers.getLogger(this.getClass());

    public HTTPBasicAuthenticator(final Settings settings) {
    
    }

    @Override
    public AuthUser auth(final RestRequest request) {

        final String authorizationHeader = request.header("Authorization");
        
        return BasicAuthHelper.extractCredentials(authorizationHeader, log);
    }

    @Override
    public String getType() {
        return "basic";
    }
}
