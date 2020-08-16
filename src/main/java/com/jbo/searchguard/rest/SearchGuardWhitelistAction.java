package com.jbo.searchguard.rest;

import com.jbo.searchguard.action.configupdate.ConfigUpdateAction;
import com.jbo.searchguard.action.configupdate.ConfigUpdateRequest;
import com.jbo.searchguard.auth.internal.WhitelistAuthBackend;
import com.jbo.searchguard.utils.ConfigConstants;
import org.elasticsearch.action.WriteConsistencyLevel;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.*;

import static org.elasticsearch.rest.RestRequest.Method.*;

public class SearchGuardWhitelistAction extends BaseRestHandler {

    private final String index = ConfigConstants.DEFAULT_CONFIG_INDEX;
    private final String type = "whitelist";
    private final WhitelistAuthBackend wab;

    @Inject
    public SearchGuardWhitelistAction(final Settings settings, final RestController controller, final Client client, WhitelistAuthBackend wab) {
        super(settings, controller, client);
        this.wab = wab;
        controller.registerHandler(GET, "/_auth/whitelist", this);
        controller.registerHandler(POST, "/_auth/whitelist", this);
        controller.registerHandler(PUT, "/_auth/whitelist", this);
        controller.registerHandler(DELETE, "/_auth/whitelist", this);
    }

    @Override
    protected void handleRequest(final RestRequest request, final RestChannel channel, final Client client) throws Exception {

        BytesRestResponse response = null;
        XContentBuilder builder = channel.newBuilder();

        try {
            if (request.method() == RestRequest.Method.GET) {
                builder.startObject();
                builder.field("whitelist", wab.globalWhitelist());
                builder.endObject();
                response = new BytesRestResponse(RestStatus.OK, builder);
            } else {
                String id = "1";
                if (request.method() == RestRequest.Method.POST || request.method() == RestRequest.Method.PUT) {
                    id = client.update(new UpdateRequest(index, type, "0").refresh(true)
                            .consistencyLevel(WriteConsistencyLevel.DEFAULT).doc(request.content().array())).actionGet().getId();
                } else if (request.method() == RestRequest.Method.DELETE) {
                    id = client.delete(new DeleteRequest(index, type, "0").refresh(true)).actionGet().getId();
                }
                if ("0".equals(id)) {
                    builder.startObject();
                    builder.field("acknowledged", true);
                    builder.endObject();
                    response = new BytesRestResponse(RestStatus.OK, builder);
                } else {
                    logger.error("Configuration for '" + type + "' failed for unknown reasons.");
                }

                client.execute(ConfigUpdateAction.INSTANCE, new ConfigUpdateRequest(ConfigConstants.DEFAULT_CONFIG_TYPE.toArray(new String[]{}))).actionGet();

            }

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
