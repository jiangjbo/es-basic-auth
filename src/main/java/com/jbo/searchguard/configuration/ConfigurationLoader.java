package com.jbo.searchguard.configuration;

import com.jbo.searchguard.utils.ConfigConstants;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.get.MultiGetResponse.Failure;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Provider;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.loader.JsonSettingsLoader;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConfigurationLoader {

    protected final ESLogger log = Loggers.getLogger(this.getClass());
    private final Provider<Client> client;

    private final String authIndex;

    @Inject
    public ConfigurationLoader(final Provider<Client> client, final Settings settings) {
        super();
        this.client = client;
        this.authIndex = settings.get(ConfigConstants.AUTH_CONFIG_INDEX, ConfigConstants.DEFAULT_CONFIG_INDEX);
        log.debug("Index is: {}", authIndex);
    }

    public Map<String, Settings> load(final List<String> events, long timeout, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
        final CountDownLatch latch = new CountDownLatch(events.size());
        final Map<String, Settings> rs = new HashMap<>(events.size());

        loadAsync(events, new ConfigCallback() {

            @Override
            public void success(String type, Settings settings) {
                if (latch.getCount() <= 0) {
                    log.error("Latch already counted down (for {} of {})  (index={})", type, events, authIndex);
                }

                rs.put(type, settings);
                latch.countDown();
                if (log.isDebugEnabled()) {
                    log.debug("Received config for {} (of {}) with current latch value={}", type, events, latch.getCount());
                }
            }

            @Override
            public void singleFailure(Failure failure) {
                latch.countDown();
                log.error("Failure {} retrieving configuration for {} (index={})", failure == null ? null : failure.getMessage(), events, authIndex);
            }

            @Override
            public void noData(String type) {
                latch.countDown();
                log.error("No data for {} while retrieving configuration for {}  (index={})", type, events, authIndex);
            }

            @Override
            public void failure(Throwable t) {
                for (int i = 0; i < latch.getCount(); i++) {
                    latch.countDown();
                }
                log.error("Exception {} while retrieving configuration for {}  (index={})", t, t.toString(), events, authIndex);
            }
        });

        if (!latch.await(timeout, timeUnit)) {
            throw new TimeoutException("Timeout after " + timeout + "" + timeUnit + " while retrieving configuration for " + events + "(index=" + authIndex + ")");
        }

        return rs;
    }

    private void loadAsync(List<String> events, final ConfigCallback callback) {
        if (events == null || events.size() == 0) {
            log.warn("No config events requested to load");
            return;
        }

        final MultiGetRequest mget = new MultiGetRequest();
        for (String event : events) {
            mget.add(authIndex, event, "0");
        }

        mget.refresh(true);
        mget.realtime(true);

        client.get().multiGet(mget, new ActionListener<MultiGetResponse>() {

            @Override
            public void onResponse(MultiGetResponse response) {
                MultiGetItemResponse[] responses = response.getResponses();
                for(MultiGetItemResponse singleResponse: responses) {
                    if (singleResponse != null && !singleResponse.isFailed()) {
                        GetResponse singleGetResponse = singleResponse.getResponse();
                        if (singleGetResponse.isExists() && !singleGetResponse.isSourceEmpty()) {
                            Settings settings = toSettings(singleGetResponse.getSourceAsBytesRef(), singleGetResponse.getType());
                            if(settings != null) {
                                callback.success(singleGetResponse.getType(), settings);
                            }
                        } else {
                            callback.noData(singleGetResponse.getType());
                        }
                    } else {
                        callback.singleFailure(singleResponse == null ? null : singleResponse.getFailure());
                    }
                }
            }

            @Override
            public void onFailure(Throwable e) {
                callback.failure(e);
            }

        });
    }

    private Settings toSettings(final BytesReference ref, String type) {
        if (ref == null || ref.length() == 0) {
            log.error("Null or empty BytesReference for " + type);
            return null;
        }
        try {
            return Settings.builder().put(new JsonSettingsLoader().load(XContentHelper.createParser(ref))).build();
        } catch (final Exception e) {
            log.error("Unable to parse {} due to {}", e, type, e.toString());
            return null;
        }
    }

}
