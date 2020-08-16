package com.jbo.elasticsearch.auth.filter;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.tasks.Task;

public class AuthFilter implements ActionFilter {

    protected final ESLogger log = Loggers.getLogger(this.getClass());
    private final Settings settings;

    @Inject
    public AuthFilter(final Settings settings) {
        this.settings = settings;
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void apply(Task task, final String action, final ActionRequest request, final ActionListener listener, final ActionFilterChain chain) {

        if (log.isTraceEnabled()) {
            log.trace("Action {} from {}/{}", action, request.remoteAddress(), listener.getClass().getSimpleName());
            log.trace("Context {}", request.getContext());
            log.trace("Header {}", request.getHeaders());

        }
        chain.proceed(task, action, request, listener);
    }

    @Override
    public void apply(final String action, final ActionResponse response, final ActionListener listener, final ActionFilterChain chain) {
        chain.proceed(action, response, listener);
    }


}
