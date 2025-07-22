package io.levysworks.jobs;

import com.rabbitmq.client.*;
import io.levysworks.beans.KeyManager;
import io.levysworks.config.AgentConfig;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bean for consuming the RabbitMQ {@code .add} and {@code .remove} queues
 */
@Startup
@ApplicationScoped
public class KeyPoller {
    private final Logger logger = Logger.getLogger(KeyPoller.class.getName());

    @Inject
    AgentConfig agentConfig;

    @Inject
    KeyManager keyManager;

    @Inject
    RabbitMQClient rabbitMQClient;

    private Connection connection;
    private Channel channel;

    /**
     * Initializes the RabbitMQ connection and declares necessary messaging infrastructure.
     * <p>
     * Connects to the configured RabbitMQ server, declares the {@code pollexchange} topic exchange,
     * and declares {@code .add} and {@code .remove} queues for the assigned in {@link AgentConfig}.
     * This method is called automatically after bean construction.
     */
    @PostConstruct
    public void init() {
        try {
            connection = rabbitMQClient.connect();
            channel = connection.createChannel();
            String agentName = agentConfig.agentName();

            try {
                channel.queueDeclare(agentName, true, false, false, null);
                channel.queueBind(agentName, "pollexchange", agentName + ".add");
                channel.queueBind(agentName, "pollexchange", agentName + ".remove");

                channel.basicConsume(agentConfig.agentName(), false, deliverCallback, cancelCallback);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Delivery callback used by {@link Channel#basicConsume} to handle incoming messages.
     * <p>
     * Processes routing keys ending in ".add" or ".remove" to add or remove keys via {@code keyManager},
     * and acknowledges the message after processing.
     */
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String routingKey = delivery.getEnvelope().getRoutingKey();
        String payload = new String(delivery.getBody(), StandardCharsets.UTF_8);

        if (routingKey.endsWith(".add")) {
            keyManager.addKey(payload);
            logger.log(Level.INFO, "Added key: " + payload);
        } else if (routingKey.endsWith(".remove")) {
            keyManager.removeKey(payload);
            logger.log(Level.INFO, "Removed key with UID: " + payload);
        }

        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    };

    /**
     * Cancel callback used by {@link Channel#basicConsume} to handle consumer cancellation.
     * <p>
     * Logs the consumer tag when the consumer is cancelled by the server or due to shutdown.
     */
    CancelCallback cancelCallback = consumerTag ->
            logger.log(Level.INFO, "Consumer " + consumerTag + " was cancelled");

    /**
     * Called before destroying the instance
     * <p>
     * Closes open connections to the {@code RabbitMQ} server
     */
    @PreDestroy
    public void destroy() {
        try {
            if (connection != null) connection.close();
            if (channel != null) channel.close();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Cleanup failed", e);
        }
    }
}
