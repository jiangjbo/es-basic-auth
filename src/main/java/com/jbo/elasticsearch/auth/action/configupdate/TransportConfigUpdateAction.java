package com.jbo.elasticsearch.auth.action.configupdate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.jbo.elasticsearch.auth.configuration.ConfigChangeListener;
import com.jbo.elasticsearch.auth.action.configupdate.ConfigUpdateResponse.Node;
import com.jbo.elasticsearch.auth.auth.BackendRegistry;
import com.jbo.elasticsearch.auth.configuration.ConfigurationLoader;
import com.jbo.elasticsearch.auth.utils.ConfigConstants;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.nodes.BaseNodeRequest;
import org.elasticsearch.action.support.nodes.TransportNodesAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.component.LifecycleListener;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Provider;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class TransportConfigUpdateAction extends
        TransportNodesAction<ConfigUpdateRequest, ConfigUpdateResponse, TransportConfigUpdateAction.NodeConfigUpdateRequest, ConfigUpdateResponse.Node> {

    private final ClusterService clusterService;
    private final ConfigurationLoader cl;
    private final Provider<BackendRegistry> backendRegistry;
    private final ListMultimap<String, ConfigChangeListener> multimap = Multimaps.synchronizedListMultimap(ArrayListMultimap
            .<String, ConfigChangeListener>create());

    private final String authIndex;

    @Inject
    public TransportConfigUpdateAction(final Provider<Client> clientProvider, final Settings settings, final ClusterName clusterName,
                                       final ThreadPool threadPool, final ClusterService clusterService, final TransportService transportService,
                                       final ConfigurationLoader cl, final ActionFilters actionFilters, final IndexNameExpressionResolver indexNameExpressionResolver,
                                       Provider<BackendRegistry> backendRegistry) {
        super(settings, ConfigUpdateAction.NAME, clusterName, threadPool, clusterService, transportService, actionFilters,
                indexNameExpressionResolver, ConfigUpdateRequest.class, TransportConfigUpdateAction.NodeConfigUpdateRequest.class,
                ThreadPool.Names.MANAGEMENT);
        this.cl = cl;
        this.clusterService = clusterService;
        this.backendRegistry = backendRegistry;
        this.authIndex = settings.get(ConfigConstants.AUTH_CONFIG_INDEX, ConfigConstants.DEFAULT_CONFIG_INDEX);

        clusterService.addLifecycleListener(new LifecycleListener() {

            private void sleep30s(){
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
            }

            private ClusterHealthResponse waitAuthIndexYellow(Client client) {
                ClusterHealthResponse response = null;
                try {
                    response = client.admin().cluster().health(new ClusterHealthRequest(authIndex).waitForYellowStatus()).actionGet();
                } catch (Exception e1) {
                    logger.warn("wait {} ready  ...", authIndex);
                }
                return response;
            }

            @Override
            public void afterStart() {

                final Thread ct = new Thread(() -> {
                    try {
                        Client client = clientProvider.get();
                        logger.debug("Node started, try to initialize it. Wait for at least yellow cluster state....");
                        ClusterHealthResponse response = waitAuthIndexYellow(client);

                        while (response == null || response.isTimedOut() || response.getStatus() == ClusterHealthStatus.RED) {
                            logger.warn("index '{}' not healthy yet, we try again ... (Reason: {})", authIndex, response == null ? "no response" : (response.isTimedOut() ? "timeout" : "other, maybe red cluster"));
                            sleep30s();
                            response = waitAuthIndexYellow(client);
                        }

                        Map<String, Settings> setMap = null;

                        while (setMap == null || !setMap.keySet().containsAll(ConfigConstants.DEFAULT_CONFIG_TYPE)) {

                            if (setMap != null) {
                                sleep30s();
                            }

                            try {
                                setMap = cl.load(ConfigConstants.DEFAULT_CONFIG_TYPE, 1, TimeUnit.MINUTES);
                            } catch (Exception e) {
                                logger.warn("load failed, we just try again in a few seconds ... ");
                            }

                        }


                        for (final String evt : setMap.keySet()) {
                            for (final ConfigChangeListener cl1 : new ArrayList<>(multimap.get(evt))) {
                                Settings settings1 = setMap.get(evt);
                                cl1.onChange(evt, settings1);
                                logger.debug("Updated {} for {} due to initial configuration on node '{}'", evt, cl1.getClass().getSimpleName(), clusterService.localNode().getName());
                            }
                        }

                        logger.info("Node '{}' initialized", clusterService.localNode().getName());
                    } catch (Exception e) {
                        logger.error("Unexpected exception while initializing node " + e, e);
                    }
                });

                logger.info("Check if " + authIndex + " index exists ...");

                try {
                    IndicesExistsRequest ier = new IndicesExistsRequest(authIndex)
                            .masterNodeTimeout(TimeValue.timeValueMinutes(1));
                    clientProvider.get().admin().indices().exists(ier, new ActionListener<IndicesExistsResponse>() {

                        @Override
                        public void onResponse(IndicesExistsResponse response) {
                            if (response != null && response.isExists()) {
                                ct.start();
                            } else {
                                if (settings.getAsBoolean("action.master.force_local", false) && settings.getByPrefix("tribe").getAsMap().size() > 0) {
                                    logger.info("{} index does not exist yet, but we are a tribe node. So we will load the config anyhow until we got it ...", authIndex);
                                    ct.start();
                                } else {
                                    logger.info("{} index does not exist yet, so no need to load config on node startup", authIndex);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable e) {
                            logger.error("Failure while checking {} index {}", e, authIndex, e);
                            ct.start();
                        }
                    });
                } catch (Throwable e2) {
                    logger.error("Failure while executing IndicesExistsRequest", e2);
                    ct.start();
                }
            }
        });

    }

    public static class NodeConfigUpdateRequest extends BaseNodeRequest {

        ConfigUpdateRequest request;

        private NodeConfigUpdateRequest(final String nodeId, final ConfigUpdateRequest request) {
            super(request, nodeId);
            this.request = request;
        }

        @Override
        public void readFrom(final StreamInput in) throws IOException {
            super.readFrom(in);
            request = new ConfigUpdateRequest();
            request.readFrom(in);
        }

        @Override
        public void writeTo(final StreamOutput out) throws IOException {
            super.writeTo(out);
            request.writeTo(out);
        }
    }

    @Override
    protected ConfigUpdateResponse newResponse(final ConfigUpdateRequest request, final AtomicReferenceArray nodesResponses) {

        final List<ConfigUpdateResponse.Node> nodes = Lists.newArrayList();
        for (int i = 0; i < nodesResponses.length(); i++) {
            final Object resp = nodesResponses.get(i);
            if (resp instanceof ConfigUpdateResponse.Node) {
                nodes.add((ConfigUpdateResponse.Node) resp);
            }
        }
        return new ConfigUpdateResponse(this.clusterName, nodes.toArray(new ConfigUpdateResponse.Node[nodes.size()]));

    }

    @Override
    protected NodeConfigUpdateRequest newNodeRequest(final String nodeId, final ConfigUpdateRequest request) {
        return new NodeConfigUpdateRequest(nodeId, request);
    }

    @Override
    protected Node newNodeResponse() {
        return new ConfigUpdateResponse.Node(clusterService.localNode(), new String[0], null);
    }

    @Override
    protected Node nodeOperation(final NodeConfigUpdateRequest request) {
        try {
            Map<String, Settings> setn = cl.load(Arrays.asList(request.request.getConfigTypes()), 60, TimeUnit.SECONDS);
            logger.debug("Retrieved config ({}) due to config update request and will now update config change listeners", Arrays.toString(request.request.getConfigTypes()));
            for (final String evt : setn.keySet()) {
                for (final ConfigChangeListener cl : multimap.get(evt)) {
                    Settings settings = setn.get(evt);
                    cl.onChange(evt, settings);
                    logger.debug("Updated {} for {} due to node operation on node {}", evt, cl.getClass().getSimpleName(),
                            clusterService.localNode().getName());
                }
            }
            return new ConfigUpdateResponse.Node(clusterService.localNode(), setn.keySet().toArray(new String[]{}), null);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
            logger.debug("Thread was interrupted, we return just a empty response");
            return new ConfigUpdateResponse.Node(clusterService.localNode(), new String[0], "Interrupted");
        } catch (TimeoutException e1) {
            logger.error("Timeout {}", e1, e1);
            return new ConfigUpdateResponse.Node(clusterService.localNode(), new String[0], "Timeout (" + e1 + ")");
        }
    }

    public void addConfigChangeListener(final String event, final ConfigChangeListener listener) {
        logger.debug("Add config listener {}", listener.getClass());
        multimap.put(event, listener);
    }

    @Override
    protected boolean accumulateExceptions() {
        return false;
    }

}
