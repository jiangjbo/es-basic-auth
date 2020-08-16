package com.jbo.searchguard.configuration;

import com.jbo.searchguard.utils.ConfigConstants;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

import java.io.Closeable;

public class ConfigurationService extends AbstractLifecycleComponent<ConfigurationService> implements Closeable {
	
	public final static String CONFIG_NAME_INTERNAL_USERS = ConfigConstants.INDEX_TYPE_INTERNAL_USER;
	public final static String CONFIG_NAME_CONFIG = ConfigConstants.INDEX_TYPE_CONFIG;
	public final static String CONFIG_NAME_WHITELIST = ConfigConstants.INDEX_TYPE_WHITELIST;

    @Inject
    public ConfigurationService(final Settings settings, final Client client) {
        super(settings);
    }

    @Override
    protected void doStart() {
     // do nothing
    }

    @Override
    protected void doStop() {
     // do nothing
    }

    @Override
    protected void doClose() {
     // do nothing
    }   
}
