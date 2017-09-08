package com.kumuluz.ee.cors.utils;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.cors.annotations.CrossOrigin;
import com.kumuluz.ee.cors.config.CorsConfig;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Created by zvoneg on 31/07/17.
 */
public class CorsConfigUtil {

    static void updateCorsConfig(CorsConfig config, CrossOrigin annotation, Class clazz, Method method) {

        if (annotation == null) {
            return;
        }

        String key;

        if (annotation.name().length() != 0) {
            key = annotation.name();
        } else {
            key = clazz.getSimpleName();

            if (method != null) {
                key += "-" + method.getName();
            }
        }

        ConfigurationUtil cfg = ConfigurationUtil.getInstance();

        String configKey = "kumuluzee.cors-filter.annotations." + key;
        Optional<String> corsFilterOpt = cfg.get(configKey);

        if (corsFilterOpt.isPresent()) {
            Optional<String> allowGenericHttpRequests = cfg.get(configKey + ".allow-generic-http-requests");
            allowGenericHttpRequests.ifPresent(config::setAllowGenericHttpRequests);

            Optional<String> allowOrigin = cfg.get(configKey + ".allow-origin");
            allowOrigin.ifPresent(config::setAllowOrigin);

            Optional<String> allowSubdomains = cfg.get(configKey + ".allow-subdomains");
            allowSubdomains.ifPresent(config::setAllowSubdomains);

            Optional<String> supportedMethods = cfg.get(configKey + ".supported-methods");
            supportedMethods.ifPresent(config::setSupportedMethods);

            Optional<String> supportedHeaders = cfg.get(configKey + ".supported-headers");
            supportedHeaders.ifPresent(config::setSupportedHeaders);

            Optional<String> exposedHeaders = cfg.get(configKey + ".exposed-headers");
            exposedHeaders.ifPresent(config::setExposedHeaders);

            Optional<String> supportsCredentials = cfg.get(configKey + ".supports-credentials");
            supportsCredentials.ifPresent(config::setSupportsCredentials);

            Optional<String> maxAge = cfg.get(configKey + ".max-age");
            maxAge.ifPresent(config::setMaxAge);

            Optional<String> tagRequest = cfg.get(configKey + ".tag-requests");
            tagRequest.ifPresent(config::setTagRequests);

            Optional<String> urlPattern = cfg.get(configKey + ".url-pattern");
            urlPattern.ifPresent(config::setPathSpec);

            return;
        }

        config.setAllowGenericHttpRequests(Boolean.toString(annotation.allowGenericHttpRequests()));
        config.setAllowOrigin(annotation.allowOrigin());
        config.setAllowSubdomains(Boolean.toString(annotation.allowSubdomains()));
        config.setSupportedHeaders(annotation.supportedHeaders());
        config.setExposedHeaders(annotation.exposedHeaders());
        config.setMaxAge(Integer.toString(annotation.maxAge()));
        config.setSupportedMethods(annotation.supportedMethods());
        config.setSupportsCredentials(Boolean.toString(annotation.supportsCredentials()));
        config.setTagRequests(Boolean.toString(annotation.tagRequests()));
    }
}
