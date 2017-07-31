package com.kumuluz.ee.cors.utils;

import com.kumuluz.ee.cors.annotations.CrossOrigin;
import com.kumuluz.ee.cors.config.CorsConfig;

/**
 * Created by zvoneg on 31/07/17.
 */
public class CorsConfigUtil {

    static void updateCorsConfig(CorsConfig config, CrossOrigin annotation) {
        if (annotation == null) {
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
