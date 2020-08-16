package com.jbo.elasticsearch.auth;

import com.google.common.collect.ImmutableList;
import com.jbo.elasticsearch.auth.action.configupdate.ConfigUpdateAction;
import com.jbo.elasticsearch.auth.action.configupdate.TransportConfigUpdateAction;
import com.jbo.elasticsearch.auth.auth.http.HTTPAuthenticator;
import com.jbo.elasticsearch.auth.auth.internal.InnerUserBackend;
import com.jbo.elasticsearch.auth.configuration.AuthSettingsFilter;
import com.jbo.elasticsearch.auth.configuration.ConfigurationService;
import com.jbo.elasticsearch.auth.filter.AuthFilter;
import com.jbo.elasticsearch.auth.http.AuthHttpServerTransport;
import com.jbo.elasticsearch.auth.rest.SearchGuardUserInfoAction;
import com.jbo.elasticsearch.auth.transport.AuthTransportService;
import com.jbo.elasticsearch.auth.auth.BackendRegistry;
import com.jbo.elasticsearch.auth.auth.http.HTTPBasicAuthenticator;
import com.jbo.elasticsearch.auth.auth.internal.WhitelistAuthBackend;
import com.jbo.elasticsearch.auth.auth.tcp.TCPAuthenticator;
import com.jbo.elasticsearch.auth.auth.tcp.TcpBasicAuthenticator;
import com.jbo.elasticsearch.auth.rest.SearchGuardWhitelistAction;
import org.elasticsearch.action.ActionModule;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.http.HttpServerModule;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.transport.TransportModule;

import java.util.ArrayList;
import java.util.Collection;

public final class AuthPlugin extends Plugin {

    private final ESLogger log = Loggers.getLogger(this.getClass());
    private final Settings settings;
    private final boolean disabled;

    public AuthPlugin(final Settings settings) {
        super();
        log.info("cluster name: {}", settings.get("cluster.name","elasticsearch"));

        disabled = settings.getAsBoolean("auth.disabled", false);
        if(disabled) {
            this.settings = null;
            log.warn("auth plugin installed but disabled.");
            return;
        }
        this.settings = settings;
    }

    @Override
    public String name() {
        return "elasticsearch-auth";
    }

    @Override
    public String description() {
        return "elasticsearch-auth";
    }
    
    public Collection<Module> shardModules(Settings settings)
    {
      return ImmutableList.of();
    }

    /**
     * 注册ioc
     */
    @Override
    public Collection<Module> nodeModules() {
        final Collection<Module> modules = new ArrayList<>();
        if (!disabled) {
            modules.add(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(AuthSettingsFilter.class).asEagerSingleton();
                    bind(ConfigurationService.class).asEagerSingleton();
                    bind(WhitelistAuthBackend.class).asEagerSingleton();
                    bind(InnerUserBackend.class).asEagerSingleton();
                    bind(BackendRegistry.class).asEagerSingleton();
                    bind(TCPAuthenticator.class).to(TcpBasicAuthenticator.class).asEagerSingleton();
                    bind(HTTPAuthenticator.class).to(HTTPBasicAuthenticator.class).asEagerSingleton();
                }
            });
        }
        return modules;
    }

    public void onModule(final ActionModule module) {
        if(!disabled) {
            module.registerAction(ConfigUpdateAction.INSTANCE, TransportConfigUpdateAction.class);
            module.registerFilter(AuthFilter.class);
        }
    }

	public void onModule(final RestModule module) {
        if (!disabled) {
            module.addRestAction(SearchGuardWhitelistAction.class);
            module.addRestAction(SearchGuardUserInfoAction.class);
        }
    }

    public void onModule(final TransportModule module) {
        if (!disabled) {
            module.setTransportService(AuthTransportService.class, name());
        }
    }
    
    public void onModule(final HttpServerModule module) {
        if (!disabled) {
            module.setHttpServerTransport(AuthHttpServerTransport.class, name());
        }
    }

    @Override
    public Settings additionalSettings() {
        if(disabled) {
            return Settings.EMPTY;
        }
        final Settings.Builder builder = Settings.settingsBuilder();
        return builder.build();
    }

}
