/*
 * Copyright 2015 floragunn UG (haftungsbeschränkt)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.jbo.searchguard.utils;

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
