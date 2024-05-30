package com.example.demo;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@Configuration
public class MqttBeans {
	public MqttPahoClientFactory pahoClientFactory() {
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		MqttConnectOptions options = new MqttConnectOptions();
		options.setServerURIs(new String[] { "tcp://mqtt.flespi.io:1883" });
		options.setUserName("Wi5UK6U0uKwIkykZMYz9FMPVUQhF50eP88zG2jwnRSTZe1PQNcNgOs1w7602W4FS");
		String pass = "";
		options.setPassword(pass.toCharArray());
		options.setCleanSession(true);

		factory.setConnectionOptions(options);
		return factory;

	}

	@Bean
	public MessageChannel mqttInputChannel() {
		return new DirectChannel();
	}

	@Bean
	public MessageProducer inbound() {
		MqttPahoMessageDrivenChannelAdapter	adapter = new MqttPahoMessageDrivenChannelAdapter("serverIn", pahoClientFactory(), "#");
		adapter.setCompletionTimeout(50000);
		adapter.setQos(2);
		adapter.setOutputChannel(mqttInputChannel());
		return adapter;
	}
	
	@Bean
	@ServiceActivator(inputChannel = "mqttInputChannel")
	public MessageHandler handler() {
		return new MessageHandler() {
			
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();
				
				if(topic.equals("myTopic")) {
					System.out.println("This is our Topic...");
				}
				System.out.println(message.getPayload());
				
			}
		};
	}
	
	@Bean
	public MessageChannel mqttOutBoundChannel() {
		return new DirectChannel();
	}
	
	@Bean
	@ServiceActivator(inputChannel = "mqttOutBoundChannel")
	public MessageHandler mqttOutBound() {
		MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("serverOut", pahoClientFactory());
		messageHandler.setAsync(true);
		messageHandler.setDefaultTopic("#");
		return messageHandler;
	}
}
