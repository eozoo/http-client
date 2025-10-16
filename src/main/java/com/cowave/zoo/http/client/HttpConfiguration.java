package com.cowave.zoo.http.client;

import com.cowave.zoo.http.client.annotation.EnableHttpClient;
import com.cowave.zoo.http.client.asserts.I18Messages;
import org.springframework.context.MessageSource;

import javax.annotation.Resource;

/**
 *
 * @author shanhuiming
 *
 */
@EnableHttpClient
public class HttpConfiguration {

    @Resource
    public void setMessageSource(MessageSource messageSource) {
        I18Messages.setMessageSource(messageSource);
    }
}
