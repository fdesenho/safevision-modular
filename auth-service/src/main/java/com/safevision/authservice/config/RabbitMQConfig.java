package com.safevision.authservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	 @Value("safevision.vision.configuration.queue")
	private String visionConfiguration;
    

    @Bean
    public Queue visionConfigQueue() {
        return new Queue(visionConfiguration, true);
    }
    @Bean
    public String visionConfigurationQueueName() {
        return visionConfiguration;
    } 
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}