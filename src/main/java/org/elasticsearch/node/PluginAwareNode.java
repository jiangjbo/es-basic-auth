package org.elasticsearch.node;

import org.elasticsearch.Version;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.internal.InternalSettingsPreparer;
import org.elasticsearch.plugins.Plugin;

import java.util.Arrays;

public class PluginAwareNode extends Node {

    @SafeVarargs
    public PluginAwareNode(final Settings preparedSettings, final Class<? extends Plugin>... plugins) {
        super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null), Version.CURRENT, Arrays.asList(plugins));
    }
}
