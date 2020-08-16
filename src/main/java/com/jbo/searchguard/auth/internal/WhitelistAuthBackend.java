package com.jbo.searchguard.auth.internal;

import com.jbo.searchguard.action.configupdate.TransportConfigUpdateAction;
import com.jbo.searchguard.configuration.ConfigChangeListener;
import com.jbo.searchguard.configuration.ConfigurationService;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WhitelistAuthBackend implements ConfigChangeListener {

    //elasticsearch.yml配置文件中的白名单，只对当前机器生效
    private Set<String> configWhitelist = new HashSet<>();
    //es中的白名单，对所有es节点生效
    private volatile Set<String> globalWhitelist;

    @Inject
    public WhitelistAuthBackend(final Settings unused, final TransportConfigUpdateAction tcua) {
        super();
        tcua.addConfigChangeListener(ConfigurationService.CONFIG_NAME_WHITELIST, this);
    }

    @Override
    public void onChange(String event, Settings settings) {
        globalWhitelist = new HashSet<>(settings.getAsMap().values());
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    public void setConfigWhitelist(String... ips){
        configWhitelist.addAll(Arrays.asList(ips));
    }

    public Set<String> globalWhitelist(){
        return globalWhitelist;
    }

    public boolean isWhitelist(String ip) {
        return !Strings.isNullOrEmpty(ip) && (configWhitelist.contains(ip) || globalWhitelist.contains(ip));
    }
}
