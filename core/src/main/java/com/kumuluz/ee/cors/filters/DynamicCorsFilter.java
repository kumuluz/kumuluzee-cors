package com.kumuluz.ee.cors.filters;

import com.kumuluz.ee.cors.config.CorsConfig;
import com.kumuluz.ee.cors.config.CorsRegistration;
import com.kumuluz.ee.cors.utils.CrossOriginAnnotationProcessorUtil;
import com.thetransactioncompany.cors.CORSConfiguration;
import com.thetransactioncompany.cors.CORSConfigurationException;
import com.thetransactioncompany.cors.CORSFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by zvoneg on 27/07/17.
 */
public class DynamicCorsFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(DynamicCorsFilter.class.getName());

    private Map<String, CorsConfig> corsConfigs;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        CrossOriginAnnotationProcessorUtil crossProcessorUtil = new CrossOriginAnnotationProcessorUtil();

        crossProcessorUtil.init();

        corsConfigs = new LinkedHashMap<>(crossProcessorUtil.getCorsRegistry().size());

        for (CorsRegistration registration : crossProcessorUtil.getCorsRegistry()) {
            corsConfigs.put(registration.getPathPattern(), registration.getConfig());
        }

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException,
            ServletException {

        if (servletRequest instanceof HttpServletRequest) {

            HttpServletRequest request = (HttpServletRequest) servletRequest;

            CORSConfiguration configuration = getMaxMatchingUrlPatternConfig(request.getServletPath() + request.getPathInfo());

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
