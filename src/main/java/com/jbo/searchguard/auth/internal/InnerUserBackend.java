package com.jbo.searchguard.auth.internal;

import com.jbo.searchguard.action.configupdate.TransportConfigUpdateAction;
import com.jbo.searchguard.configuration.ConfigChangeListener;
import com.jbo.searchguard.configuration.ConfigurationService;
import com.jbo.searchguard.auth.user.AuthUser;
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
