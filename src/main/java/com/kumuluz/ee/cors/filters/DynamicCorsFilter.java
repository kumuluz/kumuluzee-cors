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
package com.kumuluz.ee.cors.filters;

import com.kumuluz.ee.cors.config.CorsConfig;
import com.kumuluz.ee.cors.config.CorsRegistration;
import com.kumuluz.ee.cors.utils.CrossOriginAnnotationProcessorUtil;
import com.kumuluz.ee.cors.utils.JaxRsCrossOriginAnnotationProcessorUtil;
import com.kumuluz.ee.cors.utils.ServletCrossOriginAnnotationProcessorUtil;
import com.thetransactioncompany.cors.CORSConfiguration;
import com.thetransactioncompany.cors.CORSConfigurationException;
import com.thetransactioncompany.cors.CORSFilter;
import org.glassfish.jersey.uri.UriTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * DynamicCorsFilter class.
 *
 * @author Zvone Gazvoda
 * @since 1.0.0
 */
public class DynamicCorsFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(DynamicCorsFilter.class.getName());

    private Map<String, CorsConfig> corsConfigs;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        List<CorsRegistration> corsRegistryList = new ArrayList<>();

        if (filterConfig.getInitParameter("isJaxRS").equals("true")) {
            CrossOriginAnnotationProcessorUtil jaxRsCrossProcessorUtil = new JaxRsCrossOriginAnnotationProcessorUtil();
            jaxRsCrossProcessorUtil.init();

            corsRegistryList.addAll(jaxRsCrossProcessorUtil.getCorsRegistry());
        }

        CrossOriginAnnotationProcessorUtil servletCrossProcessorUtil = new ServletCrossOriginAnnotationProcessorUtil();
        servletCrossProcessorUtil.init();

        corsRegistryList.addAll(servletCrossProcessorUtil.getCorsRegistry());

        corsConfigs = new LinkedHashMap<>(corsRegistryList.size());

        for (CorsRegistration registration : corsRegistryList) {
            corsConfigs.put(registration.getPathPattern(), registration.getConfig());
        }

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException,
            ServletException {

        if (servletRequest instanceof HttpServletRequest) {

            HttpServletRequest request = (HttpServletRequest) servletRequest;

            String path = request.getServletPath();

            if (request.getPathInfo() != null) {
                path += request.getPathInfo();
            }

            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 2);
            }

            CORSConfiguration configuration = getMaxMatchingUrlPatternConfig(path);

            if (configuration != null) {
                CORSFilter filter = new CORSFilter(configuration);
                filter.doFilter(servletRequest, servletResponse, filterChain);
            } else {
                filterChain.doFilter(servletRequest, servletResponse);
            }

        } else {
            throw new ServletException("Cannot filter non-HTTP requests/responses");
        }

    }

    @Override
    public void destroy() {

    }

    private CORSConfiguration getMaxMatchingUrlPatternConfig(String path) {

        CorsConfig corsConfig = corsConfigs.get(path);

        if (corsConfig == null) {
            String key = null;
            for (String uriTemplate : corsConfigs.keySet()) {
                if (new UriTemplate(uriTemplate).match(path, new ArrayList<>())) {
                    key = uriTemplate;
                    break;
                }
            }

            corsConfig = corsConfigs.get(key);
        }

        try {

            Map<String, String> corsFilterParams = new HashMap<>();

            if (corsConfig != null) {

                if (corsConfig.getAllowGenericHttpRequests() != null) {
                    corsFilterParams.put("cors.allowGenericHttpRequests", corsConfig.getAllowGenericHttpRequests().toString
                            ());
                }

                if (corsConfig.getAllowOrigin() != null) {
                    corsFilterParams.put("cors.allowOrigin", corsConfig.getAllowOrigin());
                }

                if (corsConfig.getAllowSubdomains() != null) {
                    corsFilterParams.put("cors.allowSubdomains", corsConfig.getAllowSubdomains().toString());
                }

                if (corsConfig.getSupportedMethods() != null) {
                    corsFilterParams.put("cors.supportedMethods", corsConfig.getSupportedMethods());
                }

                if (corsConfig.getSupportedHeaders() != null) {
                    corsFilterParams.put("cors.supportedHeaders", corsConfig.getSupportedHeaders());
                }

                if (corsConfig.getExposedHeaders() != null) {
                    corsFilterParams.put("cors.exposedHeaders", corsConfig.getExposedHeaders());
                }

                if (corsConfig.getSupportsCredentials() != null) {
                    corsFilterParams.put("cors.supportsCredentials", corsConfig.getSupportsCredentials().toString());
                }

                if (corsConfig.getMaxAge() != null) {
                    corsFilterParams.put("cors.maxAge", corsConfig.getMaxAge().toString());
                }

                if (corsConfig.getTagRequests() != null) {
                    corsFilterParams.put("cors.tagRequests", corsConfig.getTagRequests().toString());
                }

                Properties properties = new Properties();
                properties.putAll(corsFilterParams);

                return new CORSConfiguration(properties);
            }

        } catch (CORSConfigurationException e) {
            LOG.warning(e.getMessage());
        }

        return null;
    }
}
