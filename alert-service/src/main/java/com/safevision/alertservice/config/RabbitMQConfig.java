package com.safevision.alertservice.config; 

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("safevision.alerts.queue")
    private String alerts;
    
    
   
    
    @Bean
    public String alertsQueueName() {
        return alerts;
    } 
    
    @Bean
    public Queue alertsQueue() {
        return new Queue(alerts, true); 
    }

    // Configura o Spring para enviar/receber JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
}