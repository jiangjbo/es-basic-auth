package com.jbo.searchguard.auth.tcp;

import com.jbo.searchguard.auth.user.AuthUser;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.transport.TransportRequest;

import java.util.Optional;

public interface TCPAuthenticator {

    String getType();

    AuthUser auth(final TransportRequest request) throws ElasticsearchSecurityException;
    
}
