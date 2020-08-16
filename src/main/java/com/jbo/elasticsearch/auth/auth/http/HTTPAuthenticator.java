package com.jbo.elasticsearch.auth.auth.http;

import com.jbo.elasticsearch.auth.auth.user.AuthUser;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.rest.RestRequest;

public interface HTTPAuthenticator {

    String getType();

    AuthUser auth(RestRequest request) throws ElasticsearchSecurityException;
    
}
