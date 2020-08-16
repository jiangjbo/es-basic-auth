package com.jbo.elasticsearch.auth.auth;

import com.jbo.elasticsearch.auth.action.configupdate.TransportConfigUpdateAction;
import com.jbo.elasticsearch.auth.auth.http.HTTPAuthenticator;
import com.jbo.elasticsearch.auth.auth.internal.InnerUserBackend;
import com.jbo.elasticsearch.auth.auth.internal.WhitelistAuthBackend;
import com.jbo.elasticsearch.auth.auth.tcp.TCPAuthenticator;
import com.jbo.elasticsearch.auth.auth.user.AuthUser;
import com.jbo.elasticsearch.auth.configuration.ConfigChangeListener;
import com.jbo.elasticsearch.auth.configuration.ConfigurationService;
import com.jbo.elasticsearch.auth.filter.AuthRestFilter;
import com.jbo.elasticsearch.auth.utils.ConfigConstants;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.rest.*;
import org.elasticsearch.transport.TransportRequest;

import java.net.InetSocketAddress;

public class BackendRegistry implements ConfigChangeListener {

    protected final ESLogger log = Loggers.getLogger(this.getClass());
    private volatile boolean initialized;
    private final TCPAuthenticator tcpAuth;
    private final HTTPAuthenticator httpAuth;
    private final WhitelistAuthBackend whiteAuth;
    private final InnerUserBackend innerUser;


    @Inject
    public BackendRegistry(final Settings settings, final RestController controller, final TransportConfigUpdateAction tcua,
                           final TCPAuthenticator tcpAuth, final HTTPAuthenticator httpAuth,
                           final WhitelistAuthBackend whiteAuth, final InnerUserBackend innerUserBackend) {
        tcua.addConfigChangeListener(ConfigurationService.CONFIG_NAME_CONFIG, this);
        controller.registerFilter(new AuthRestFilter(this));
        this.tcpAuth = tcpAuth;
        this.httpAuth = httpAuth;
        this.whiteAuth = whiteAuth;
        this.innerUser = innerUserBackend;

        String whiteStr = settings.get("searchguard.whitelist");
        if (whiteStr != null && !"".equals(whiteStr.trim())) {
            whiteAuth.setConfigWhitelist(whiteStr.split(","));
        }
    }

    @Override
    public void onChange(final String event, final Settings settings) {
        initialized = true;
    }

    public boolean authenticate(final TransportRequest request) throws ElasticsearchSecurityException {

        TransportAddress remoteAddress = request.getFromContext(ConfigConstants.AUTH_REMOTE_ADDRESS);
        if (whiteAuth.isWhitelist(remoteAddress.getAddress()) || whiteAuth.isWhitelist(remoteAddress.getHost())) {
            request.putInContext(ConfigConstants.AUTH_WHITELIST, true);
            return true;
        }

        AuthUser authUser = tcpAuth.auth(request);
        return authUser != null && innerUser.auth(authUser);
    }

    public boolean authenticate(final RestRequest request, final RestChannel channel) throws ElasticsearchSecurityException {

        if (!isInitialized()) {
            log.error("not yet initialized");
            channel.sendResponse(new BytesRestResponse(RestStatus.SERVICE_UNAVAILABLE, "not yet initialized"));
            return false;
        }

        TransportAddress remoteAddress = new InetSocketTransportAddress(((InetSocketAddress)request.getRemoteAddress()).getAddress(),
                ((InetSocketAddress)request.getRemoteAddress()).getPort());
        request.putInContext(ConfigConstants.AUTH_REMOTE_ADDRESS, remoteAddress);
        if (whiteAuth.isWhitelist(remoteAddress.getAddress()) || whiteAuth.isWhitelist(remoteAddress.getHost())) {
            request.putInContext(ConfigConstants.AUTH_WHITELIST, true);
            return true;
        }

        AuthUser authUser = httpAuth.auth(request);
        boolean authenticated = authUser != null && innerUser.auth(authUser);
        if (!authenticated) {
            channel.sendResponse(new BytesRestResponse(RestStatus.UNAUTHORIZED));
            return false;
        }
        return true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

}
