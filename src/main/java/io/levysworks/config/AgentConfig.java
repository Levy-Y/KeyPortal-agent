package io.levysworks.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "poll")
public interface AgentConfig {
    @WithName("agent-name")
    String agentName();

    @WithName("poll-key")
    String pollKey();
}