package com.jbo.elasticsearch.auth.auth.tcp;

import com.jbo.elasticsearch.auth.auth.user.AuthUser;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.transport.TransportRequest;

public interface TCPAuthenticator {

    String getType();

    AuthUser auth(final TransportRequest request) throws ElasticsearchSecurityException;
    
}
