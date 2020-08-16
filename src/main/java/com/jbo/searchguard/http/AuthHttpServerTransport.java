package com.jbo.searchguard.http;

import com.jbo.searchguard.utils.HeaderHelper;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.http.HttpChannel;
import org.elasticsearch.http.HttpRequest;
import org.elasticsearch.http.netty.NettyHttpServerTransport;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

public class AuthHttpServerTransport extends NettyHttpServerTransport {

    @Inject
    public AuthHttpServerTransport(Settings settings, NetworkService networkService, BigArrays bigArrays) {
        super(settings, networkService, bigArrays);
    }

    @Override
    public void dispatchRequest(final HttpRequest request, final HttpChannel channel) {
        
        try {
            HeaderHelper.checkHeader(request);
        } catch (Exception e) {
            try {
                channel.sendResponse(new BytesRestResponse(channel, RestStatus.FORBIDDEN, e));
            } catch (IOException e1) {
                //ignore
            }
            return;
        }

        super.dispatchRequest(request, channel);
    }
    
}
