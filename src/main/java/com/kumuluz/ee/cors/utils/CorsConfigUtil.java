/*
 *  Copyright (c) 2014-2017 Kumuluz and/or its affiliates
 *  and other contributors as indicated by the @author tags and
 *  the contributor list.
 *
 *  Licensed under the MIT License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/MIT
 *
 *  The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement. in no event shall the
 *  authors or copyright holders be liable for any claim, damages or other
 *  liability, whether in an action of contract, tort or otherwise, arising from,
 *  out of or in connection with the software or the use or other dealings in the
 *  software. See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.kumuluz.ee.cors.utils;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.cors.annotations.CrossOrigin;
import com.kumuluz.ee.cors.config.CorsConfig;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * CorsConfigUtil class.
 *
 * @author Zvone Gazvoda
 * @since 1.0.0
 */
public class CorsConfigUtil {

    private CorsConfigUtil() {
        throw new IllegalStateException("Utility class");
    }

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
