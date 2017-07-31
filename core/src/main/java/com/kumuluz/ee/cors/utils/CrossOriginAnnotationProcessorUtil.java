package com.kumuluz.ee.cors.utils;

import com.kumuluz.ee.cors.config.CorsRegistration;

import java.util.List;

/**
 * Created by zvoneg on 31/07/17.
 */
public interface CrossOriginAnnotationProcessorUtil {

    void init();

    List<CorsRegistration> getCorsRegistrations(Class<?> applicationClass);

    List<Class<?>> getResourceClasses(Class applicationClass);

    List<CorsRegistration> getCorsRegistry();

    default boolean targetClassIsProxied(Class targetClass) {
        return targetClass.getCanonicalName().contains("$Proxy");
    }
}
