package com.example.kbuddy_backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.beans.factory.annotation.Value;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.properties.mail.transport.protocol}")
    private String protocol;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean auth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean starttlsEnable;

    @Value("${spring.mail.properties.mail.smtp.ssl.trust}")
    private String sslTrust;

    @Value("${spring.mail.properties.mail.smtp.ssl.protocols}")
    private String sslProtocols;

    @Value("${spring.mail.properties.mail.debug}")
    private boolean debug;

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.transport.protocol", protocol);
        javaMailProperties.put("mail.smtp.auth", auth);
        javaMailProperties.put("mail.smtp.starttls.enable", starttlsEnable);
        javaMailProperties.put("mail.smtp.ssl.trust", sslTrust);
        javaMailProperties.put("mail.smtp.ssl.protocols", sslProtocols);
        javaMailProperties.put("mail.debug", debug);

        mailSender.setJavaMailProperties(javaMailProperties);

        return mailSender;
    }
}
