package com.jbo.searchguard.filter;

import com.jbo.searchguard.auth.BackendRegistry;
import com.jbo.searchguard.utils.HeaderHelper;
import org.elasticsearch.rest.*;
import org.elasticsearch.rest.RestRequest.Method;

public class AuthRestFilter extends RestFilter {

    private final BackendRegistry registry;

    public AuthRestFilter(final BackendRegistry registry) {
        super();
        this.registry = registry;
    }

    @Override
    public void process(final RestRequest request, final RestChannel channel, final RestFilterChain filterChain) throws Exception {
        
        try {
            HeaderHelper.checkHeader(request);
        } catch (Exception e) {
            channel.sendResponse(new BytesRestResponse(channel, RestStatus.FORBIDDEN, e));
            return;
        }
        
        if(request.method() != Method.OPTIONS) {
            if (!registry.authenticate(request, channel)) {
                return;
            }
        }

        filterChain.continueProcessing(request, channel);
    }

}
