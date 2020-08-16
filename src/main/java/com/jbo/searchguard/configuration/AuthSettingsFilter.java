package com.jbo.searchguard.configuration;

import com.jbo.searchguard.utils.ConfigConstants;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;

public class AuthSettingsFilter {

    @Inject
    public AuthSettingsFilter(final SettingsFilter settingsFilter, final Settings settings) {
        super();
        String authIndex = settings.get(ConfigConstants.AUTH_CONFIG_INDEX, ConfigConstants.DEFAULT_CONFIG_INDEX);
        settingsFilter.addFilter(String.format("%s.*", authIndex));
    }
    
}
