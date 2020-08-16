package com.jbo.elasticsearch.auth.auth.internal;

import com.jbo.elasticsearch.auth.action.configupdate.TransportConfigUpdateAction;
import com.jbo.elasticsearch.auth.configuration.ConfigChangeListener;
import com.jbo.elasticsearch.auth.configuration.ConfigurationService;
import com.jbo.elasticsearch.auth.auth.user.AuthUser;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

import java.util.Map;

public class InnerUserBackend implements ConfigChangeListener {

    private volatile Map<String, String> user;

    @Inject
    public InnerUserBackend(final Settings unused, final TransportConfigUpdateAction tcua) {
        super();
        tcua.addConfigChangeListener(ConfigurationService.CONFIG_NAME_WHITELIST, this);
    }

    @Override
    public void onChange(String event, Settings settings) {
        user = settings.getAsMap();
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    public boolean auth(AuthUser auth){
        return auth(auth.getUsername(), auth.getPassword());
    }

    public boolean auth(String username, String password){
        String realPassword = user.get(username);
        if(realPassword != null){
            if(realPassword.equals(password)) {
                return true;
            }
        }
        return false;
    }
}
