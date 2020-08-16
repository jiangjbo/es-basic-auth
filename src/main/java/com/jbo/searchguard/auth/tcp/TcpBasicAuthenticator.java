package com.jbo.searchguard.auth.tcp;

import com.jbo.searchguard.auth.user.AuthUser;
import com.jbo.searchguard.utils.BasicAuthHelper;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.transport.TransportRequest;

import java.util.Optional;

public class TcpBasicAuthenticator implements TCPAuthenticator {

    protected final ESLogger log = Loggers.getLogger(this.getClass());

    public TcpBasicAuthenticator(final Settings settings) {
    
    }

    @Override
    public AuthUser auth(final TransportRequest request) {

        final String authorizationHeader = request.getHeader("Authorization");
        
        return BasicAuthHelper.extractCredentials(authorizationHeader, log);
    }

    @Override
    public String getType() {
        return "basic";
    }
}
