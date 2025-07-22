package io.levysworks.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

/**
 * Configuration mapping interface for server information with prefix {@code poll}.
 */
@ConfigMapping(prefix = "poll")
public interface AgentConfig {
    /**
     * Returns the name of this agent.
     *
     * @return the agent name
     */
    @WithName("agent-name")
    String agentName();
}