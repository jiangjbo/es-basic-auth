package com.jbo.elasticsearch.auth.transport;

import com.jbo.elasticsearch.auth.utils.ConfigConstants;
import com.jbo.elasticsearch.auth.auth.BackendRegistry;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Provider;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class AuthTransportService extends TransportService {

    protected final ESLogger log = Loggers.getLogger(this.getClass());
    private final Provider<BackendRegistry> backendRegistry;
    private final List<String> hosts = new ArrayList<>();

    @Inject
    public AuthTransportService(final Settings settings, final Transport transport, final ThreadPool threadPool,
                                final Provider<BackendRegistry> backendRegistry) {
        super(settings, transport, threadPool);
        this.backendRegistry = backendRegistry;
        // 通过discovery.zen.ping.unicast.hosts识别内部ip
        for (String host : settings.getAsArray("discovery.zen.ping.unicast.hosts")) {
            String[] ipPorts = host.split(":");
            hosts.add(ipPorts[0]);
        }
    }

    @Override
    public <Request extends TransportRequest> void registerRequestHandler(final String action, final Callable<Request> requestFactory,
                                                                          final String executor, final TransportRequestHandler<Request> handler) {
        super.registerRequestHandler(action, requestFactory, executor, new Interceptor<>(handler));
    }

    @Override
    public <Request extends TransportRequest> void registerRequestHandler(String action, Class<Request> request, String executor,
                                                                          boolean forceExecution, TransportRequestHandler<Request> handler) {
        super.registerRequestHandler(action, request, executor, forceExecution, new Interceptor<>(handler));
    }

    @Override
    public <T extends TransportResponse> void sendRequest(final DiscoveryNode node, final String action, final TransportRequest request,
                                                          final TransportResponseHandler<T> handler) {
        super.sendRequest(node, action, request, handler);
    }

    @Override
    public <T extends TransportResponse> void sendRequest(final DiscoveryNode node, final String action, final TransportRequest request,
                                                          final TransportRequestOptions options, final TransportResponseHandler<T> handler) {
        super.sendRequest(node, action, request, options, handler);
    }

    private class Interceptor<Request extends TransportRequest> extends TransportRequestHandler<Request> {

        private final ESLogger log = Loggers.getLogger(this.getClass());
        private final TransportRequestHandler<Request> handler;

        private Interceptor(final TransportRequestHandler<Request> handler) {
            super();
            this.handler = handler;
        }

        @Override
        public void messageReceived(Request request, TransportChannel channel) throws Exception {
            messageReceived(request, channel, null);
        }

        @Override
        public void messageReceived(final Request request, final TransportChannel transportChannel, Task task) throws Exception {

            //bypass non-netty requests
            if (transportChannel.getChannelType().equals("local") || transportChannel.getChannelType().equals("direct")) {
                handler.messageReceived(request, transportChannel, task);
                return;
            }

            TransportAddress originalRemoteAddress = request.remoteAddress();

            if (originalRemoteAddress != null && (originalRemoteAddress instanceof InetSocketTransportAddress)) {
                request.putInContext(ConfigConstants.AUTH_REMOTE_ADDRESS, originalRemoteAddress);
            } else {
                log.error("Request has no proper remote address {}", originalRemoteAddress);
                transportChannel.sendResponse(new ElasticsearchException("Request has no proper remote address"));
                return;
            }
            if (!isInterClusterRequest(request)) {
                try {
                    if (!backendRegistry.get().authenticate(request)) {
                        log.error("auth failed");
                        transportChannel.sendResponse(new ElasticsearchSecurityException("auth failed"));
                        return;
                    }
                } catch (Exception e) {
                    log.error("Error authentication transport user " + e, e);
                    transportChannel.sendResponse(ExceptionsHelper.convertToElastic(e));
                    return;
                }

            }
            handler.messageReceived(request, transportChannel, task);
        }

    }

    private boolean isInterClusterRequest(final TransportRequest request) {
        TransportAddress originalRemoteAddress = request.getFromContext(ConfigConstants.AUTH_REMOTE_ADDRESS);
        return hosts.contains(originalRemoteAddress.getAddress()) || hosts.contains(originalRemoteAddress.getHost());
    }
}
