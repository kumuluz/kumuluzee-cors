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

import com.kumuluz.ee.cors.annotations.CrossOrigin;
import com.kumuluz.ee.cors.config.CorsConfig;
import com.kumuluz.ee.cors.config.CorsRegistration;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * JaxRsCrossOriginAnnotationProcessorUtil class.
 *
 * @author Zvone Gazvoda
 * @since 1.0.0
 */
public class JaxRsCrossOriginAnnotationProcessorUtil implements CrossOriginAnnotationProcessorUtil {

    private static final Logger LOG = Logger.getLogger(JaxRsCrossOriginAnnotationProcessorUtil.class.getName());

    private List<CorsRegistration> corsRegistry;

    public void init() {
        List<Application> applications = new ArrayList<>();
        ServiceLoader.load(Application.class).forEach(applications::add);

        corsRegistry = new ArrayList<>();
        for (Application application : applications) {
            corsRegistry.addAll(getCorsRegistrations(application.getClass()));
        }
    }

    public List<CorsRegistration> getCorsRegistrations(Class<?> applicationClass) {

        if (targetClassIsProxied(applicationClass)) {
            applicationClass = applicationClass.getSuperclass();
        }

        List<CorsRegistration> corsRegistrations = new ArrayList<>();

        String applicationPath = "";
        ApplicationPath applicationPathAnnotation = applicationClass.getAnnotation(ApplicationPath.class);
        if (applicationPathAnnotation != null) {
            applicationPath = applicationPathAnnotation.value();
            if (!applicationPath.isEmpty() && !applicationPath.startsWith("/")) {
                applicationPath = "/" + applicationPath;
            }
        }

        CrossOrigin applicationCrossOriginAnnotation = applicationClass.getAnnotation(CrossOrigin.class);
        CorsConfig applicationCorsConfig = null;
        if (applicationCrossOriginAnnotation != null) {
            applicationCorsConfig = new CorsConfig().applyPermitDefaultValues();
            CorsConfigUtil.updateCorsConfig(applicationCorsConfig, applicationCrossOriginAnnotation, applicationClass, null);
        }


        List<Class<?>> resourceClasses = getResourceClasses(applicationClass);
        for (Class<?> resourceClass : resourceClasses) {
            if (targetClassIsProxied(resourceClass)) {
                resourceClass = resourceClass.getSuperclass();
            }

            String resourcePath = "";
            Path resourcePathAnnotation = resourceClass.getAnnotation(Path.class);
            if (resourcePathAnnotation != null) {
                resourcePath = resourcePathAnnotation.value();
                if (!resourcePath.isEmpty() && !resourcePath.startsWith("/")) {
                    resourcePath = applicationPath + "/" + resourcePath;
                } else if (!resourcePath.isEmpty() && resourcePath.startsWith("/") && !resourcePath.equals("/")) {
                    resourcePath = applicationPath + resourcePath;
                } else {
                    resourcePath = applicationPath;
                }
            }

            CrossOrigin resourceCrossOriginAnnotation = resourceClass.getAnnotation(CrossOrigin.class);
            CorsConfig resourceCorsConfig = null;
            if (resourceCrossOriginAnnotation != null) {
                resourceCorsConfig = new CorsConfig().applyPermitDefaultValues();
                CorsConfigUtil.updateCorsConfig(resourceCorsConfig, resourceCrossOriginAnnotation, resourceClass, null);
            }

            for (Method resourceMethod : Arrays.asList(resourceClass.getMethods())) {
                boolean hasMethodCrossOriginAnnotation = false;
                CrossOrigin methodCrossOriginAnnotation = null;
                String methodHttpMethod = null;
                boolean methodPathPresent = false;
                String methodPath = resourcePath;

                for (Annotation methodAnnotation : Arrays.asList(resourceMethod.getAnnotations())) {
                    if (methodAnnotation instanceof Path) {
                        methodPathPresent = true;
                        Path methodPathAnnotation = (Path) methodAnnotation;
                        methodPath = methodPathAnnotation.value();
                        if (!methodPath.isEmpty()) {
                            if (methodPath.startsWith("/")) {
                                methodPath = resourcePath + methodPath;
                            } else if (!methodPath.startsWith("/")) {
                                methodPath = resourcePath + "/" + methodPath;
                            } else {
                                methodPath = resourcePath;
                            }
                        } else {
                            methodPath = resourcePath;
                        }
                    } else if (methodAnnotation instanceof CrossOrigin) {
                        hasMethodCrossOriginAnnotation = true;
                        methodCrossOriginAnnotation = (CrossOrigin) methodAnnotation;
                    } else if (methodAnnotation.annotationType().getAnnotation(HttpMethod.class) != null) {
                        HttpMethod methodHttpMethodAnnotation = methodAnnotation.annotationType().getAnnotation(HttpMethod.class);
                        methodHttpMethod = methodHttpMethodAnnotation.value();
                    }
                }

                if (methodHttpMethod == null && methodPathPresent) {
                    methodHttpMethod = "GET";
                }

                if (methodHttpMethod == null) {
                    continue;
                }

                CorsRegistration registration = null;
                if (hasMethodCrossOriginAnnotation) {

                    CorsConfig corsConfig = new CorsConfig().applyPermitDefaultValues();
                    CorsConfigUtil.updateCorsConfig(corsConfig, methodCrossOriginAnnotation, resourceClass, resourceMethod);

                    registration = new CorsRegistration(methodPath, corsConfig);
                } else if (resourceCorsConfig != null) {
                    registration = new CorsRegistration(methodPath, resourceCorsConfig);
                } else if (applicationCorsConfig != null) {
                    registration = new CorsRegistration(methodPath, applicationCorsConfig);
                }

                if (registration != null) {
                    corsRegistrations.add(registration);
                }
            }
        }

        return corsRegistrations;
    }

    public List<Class<?>> getResourceClasses(Class applicationClass) {
        List<Class<?>> resourceClasses = new ArrayList<>();

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream("META-INF/resources/java.lang.Object");

        if (is != null) {

            Scanner scanner = new Scanner(is);

            while (scanner.hasNextLine()) {
                String className = scanner.nextLine();
                if (className.startsWith(applicationClass.getPackage().getName())) {
                    try {
                        Class resourceClass = Class.forName(className);
                        resourceClasses.add(resourceClass);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            scanner.close();

        }

        return resourceClasses;
    }

    public List<CorsRegistration> getCorsRegistry() {
        return corsRegistry;
    }
}
