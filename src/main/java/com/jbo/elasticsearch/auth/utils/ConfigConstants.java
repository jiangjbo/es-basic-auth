package com.jbo.elasticsearch.auth.utils;

import com.google.common.collect.Lists;

import java.util.List;

public class ConfigConstants {


    public static final String AUTH_CONFIG_PREFIX = "_auth_";

    public static final String AUTH_REMOTE_ADDRESS = AUTH_CONFIG_PREFIX + "remote_address";

    public static final String AUTH_CONFIG_INDEX = "auth.config_index_name";


    public static final String INDEX_TYPE_INTERNAL_USER = "internalusers";
    public static final String INDEX_TYPE_CONFIG = "config";
    public static final String INDEX_TYPE_WHITELIST = "whitelist";
    public static final String DEFAULT_CONFIG_INDEX = "inner_auth";
    public static final List<String> DEFAULT_CONFIG_TYPE = Lists.newArrayList(INDEX_TYPE_CONFIG, INDEX_TYPE_INTERNAL_USER, INDEX_TYPE_WHITELIST);


    public static final String AUTH_WHITELIST = AUTH_CONFIG_PREFIX + "whitelist";

}
