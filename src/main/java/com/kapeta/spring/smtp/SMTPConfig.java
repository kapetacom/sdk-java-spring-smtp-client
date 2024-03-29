/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.smtp;

import com.kapeta.spring.config.providers.KapetaConfigurationProvider;
import com.kapeta.spring.config.providers.types.ResourceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Optional;
import java.util.Properties;

@Slf4j
public class SMTPConfig {
    private static final String RESOURCE_TYPE = "kapeta/resource-type-smtp-client";

    private static final String PORT_TYPE = "smtp";

    private static final String RESOURCE_NAME = "smtpclient"; //SMTP resource name is always the same

    @Bean
    public JavaMailSender javaMailSender(KapetaConfigurationProvider configurationProvider) {

        final ResourceInfo info = configurationProvider.getResourceInfo(RESOURCE_TYPE, PORT_TYPE, RESOURCE_NAME);
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(info.getHost());
        mailSender.setPort(Integer.parseInt(info.getPort()));

        Optional<String> username = Optional.ofNullable(info.getCredentials().get("username"));
        Optional<String> password = Optional.ofNullable(info.getCredentials().get("password"));

        username.ifPresent(mailSender::setUsername);
        password.ifPresent(mailSender::setPassword);

        boolean hasAuth = username.isPresent() || password.isPresent();

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        if (hasAuth) {
            props.put("mail.smtp.auth", "true");
        }

        if (isTrueish(info.getOptions().get("tls"))) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        log.info("SMTP client configured for host: {}:{} [auth: {}]", info.getHost(), info.getPort(), hasAuth);

        return mailSender;
    }

    private boolean isTrueish(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }

        return false;
    }
}
