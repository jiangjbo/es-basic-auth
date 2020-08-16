package com.jbo.searchguard.configuration;

import org.elasticsearch.common.settings.Settings;

public interface ConfigChangeListener {

    void onChange(String event, Settings settings);

    boolean isInitialized();
}
