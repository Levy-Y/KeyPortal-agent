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

                channel.basicConsume(agentConfig.agentName(), false, new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        long deliveryTag = envelope.getDeliveryTag();
                        String routingKey = envelope.getRoutingKey();
                        String payload = new String(body, StandardCharsets.UTF_8);

                        if (routingKey.endsWith(".add")) {
                            keyManager.addKey(payload);
                            logger.log(Level.INFO, "Added key: " + payload);
                        } else if (routingKey.endsWith(".remove")) {
                            keyManager.removeKey(payload);
                            logger.log(Level.INFO, "Removed key with UID: " + payload);
                        }

                        channel.basicAck(deliveryTag, false);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

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
