package com.jbo.searchguard.configuration;

import org.elasticsearch.action.get.MultiGetResponse.Failure;
import org.elasticsearch.common.settings.Settings;

public interface ConfigCallback {
    
    void success(String type, Settings settings);
    void noData(String type);
    void singleFailure(Failure failure);
    void failure(Throwable t);

}
