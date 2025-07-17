package io.levysworks.jobs;

import io.levysworks.client.PollMaster;
import io.levysworks.config.AgentConfig;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class KeyPoller {
    private final Logger logger = Logger.getLogger(KeyPoller.class.getName());

    @Inject
    @RestClient
    PollMaster pollMaster;

    @Inject
    AgentConfig agentConfig;

    @Scheduled(every = "${poll.scheduler.timer}")
    public void cronJob() {
        List<String> pubKeys = pollMaster.pollPublicKeys(agentConfig.agentName(), agentConfig.pollKey());

        if (pubKeys == null || pubKeys.isEmpty()) {
            logger.warning("No new keys found!");
        } else {
            logger.info(pubKeys.toString());
        }
    }
}
