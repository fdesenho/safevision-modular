package com.safevision.recognitionservice.config; 

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
	 
	 @Value("vision.raw.tracking")
	 private String rawTracking;
	 @Value("safevision.alerts.queue")
	 private String alerts;
	 
	 
	 
		 
	 /**
	  * Exposes the raw tracking queue name as a String Bean for use in @RabbitListener via SpEL.
	  * O nome do Bean será 'rawTrackingQueueName'.
	  */
	 @Bean
	 public String rawTrackingQueueName() {
	     return rawTracking;
	 }
	 
	 /**
	  * Exposes the alerts queue name as a String Bean for direct injection into AlertProducer.
	  * O nome do Bean será 'alertsQueueName'.
	  */
	 @Bean
	 public String alertsQueueName() {
	     return alerts;
	 }
	 
	 // ----------------------------------------------------
	
	 // 2. Declaração da Fila de Rastreamento (Objeto Queue)
	 @Bean
	 public Queue rawTrackingQueue() {
	     return new Queue(rawTracking, true);
	 }
	 
	 // 3. Declaração da Fila de Alertas (Objeto Queue)
	 @Bean
	 public Queue alertsQueue() {
	     return new Queue(alerts, true);
	 }
	
	
    // 4. Conversor de Mensagens (JSON)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}