package com.jbo.searchguard.rest;

import com.jbo.searchguard.utils.ConfigConstants;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.*;

import static org.elasticsearch.rest.RestRequest.Method.GET;

public class SearchGuardUserInfoAction extends BaseRestHandler {

    @Inject
    public SearchGuardUserInfoAction(final Settings settings, final RestController controller, final Client client) {
        super(settings, controller, client);
        controller.registerHandler(GET, "/_auth/userinfo", this);
    }

    @Override
    protected void handleRequest(final RestRequest request, final RestChannel channel, final Client client) throws Exception {

        BytesRestResponse response = null;
        XContentBuilder builder = channel.newBuilder();
        try {
            GetResponse getResponse = client.get(new GetRequest(ConfigConstants.DEFAULT_CONFIG_INDEX, ConfigConstants.INDEX_TYPE_INTERNAL_USER, "0")).actionGet();
            builder.startObject();
            builder.map(getResponse.getSourceAsMap());
            builder.endObject();
            response = new BytesRestResponse(RestStatus.OK, builder);
        } catch (final Exception e1) {
            builder = channel.newBuilder();
            builder.startObject();
            builder.field("error", e1.toString());
            builder.endObject();
            response = new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, builder);
        }

        channel.sendResponse(response);
    }
}
