package com.jbo.searchguard.auth.http;

import com.jbo.searchguard.auth.user.AuthUser;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

import java.util.Optional;

public interface HTTPAuthenticator {

    String getType();

    AuthUser auth(RestRequest request) throws ElasticsearchSecurityException;
    
}
