package com.jbo.elasticsearch.auth.utils;

import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.transport.TransportMessage;

import java.util.Map.Entry;

public class HeaderHelper {

    // 禁止使用的请求头
    public static void checkHeader(RestRequest request) {
        if (request != null) {
            for (String header : request.getHeaders()) {
                if (header != null && header.trim().toLowerCase().startsWith(ConfigConstants.AUTH_CONFIG_PREFIX.toLowerCase())) {
                    throw new ElasticsearchSecurityException("invalid header found");
                }
            }

            for (final Entry<String, String> header : request.headers()) {
                if (header != null && header.getKey() != null
                        && header.getKey().trim().toLowerCase().startsWith(ConfigConstants.AUTH_CONFIG_PREFIX.toLowerCase())) {
                    throw new ElasticsearchSecurityException("invalid header found");
                }
            }
        }
    }

    public static void checkHeader(final TransportMessage<?> request) {
        if (request != null) {
            for (final String header : request.getHeaders()) {
                if (header != null && header.trim().toLowerCase().startsWith(ConfigConstants.AUTH_CONFIG_PREFIX.toLowerCase())) {
                    throw new ElasticsearchSecurityException("invalid header found");
                }
            }
        }
    }

}
