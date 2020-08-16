package com.jbo.elasticsearch.auth.action.configupdate;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ConfigUpdateAction extends Action<ConfigUpdateRequest, ConfigUpdateResponse, ConfigUpdateRequestBuilder> {

    public static final ConfigUpdateAction INSTANCE = new ConfigUpdateAction();
    public static final String NAME = "cluster:admin/auth/config/update";

    protected ConfigUpdateAction() {
        super(NAME);
    }

    @Override
    public ConfigUpdateRequestBuilder newRequestBuilder(final ElasticsearchClient client) {
        return new ConfigUpdateRequestBuilder(client, this);
    }

    @Override
    public ConfigUpdateResponse newResponse() {
        return new ConfigUpdateResponse();
    }

}
