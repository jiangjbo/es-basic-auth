package com.jbo.searchguard.action.configupdate;

import org.elasticsearch.action.support.nodes.NodesOperationRequestBuilder;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.ElasticsearchClient;

public class ConfigUpdateRequestBuilder extends NodesOperationRequestBuilder<ConfigUpdateRequest, ConfigUpdateResponse, ConfigUpdateRequestBuilder> {

    public ConfigUpdateRequestBuilder(final ClusterAdminClient client) {
        this(client, ConfigUpdateAction.INSTANCE);
    }

    public ConfigUpdateRequestBuilder(final ElasticsearchClient client, final ConfigUpdateAction action) {
        super(client, action, new ConfigUpdateRequest());
    }

    public ConfigUpdateRequestBuilder setShardId(final String[] configTypes) {
        request().setConfigTypes(configTypes);
        return this;
    }
}
