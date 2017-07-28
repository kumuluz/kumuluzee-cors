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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by zvoneg on 27/07/17.
 */
public class CrossOriginAnnotationProcessorUtil {

    private static final Logger log = Logger.getLogger(CrossOriginAnnotationProcessorUtil.class.getName());

    private List<CorsRegistration> corsRegistry;

    public void init() {
        List<Application> applications = new ArrayList<>();
        ServiceLoader.load(Application.class).forEach(applications::add);

        corsRegistry = new ArrayList<>();
        for (Application application : applications) {
            corsRegistry.addAll(getCorsRegistrations(application.getClass()));
        }
    }

    private List<CorsRegistration> getCorsRegistrations(Class<?> applicationClass) {

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
            updateCorsConfig(applicationCorsConfig, applicationCrossOriginAnnotation);
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
                updateCorsConfig(resourceCorsConfig, resourceCrossOriginAnnotation);
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
                        methodPath = replaceParameters(methodPath);
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
                    updateCorsConfig(corsConfig, methodCrossOriginAnnotation);

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

    private void updateCorsConfig(CorsConfig config, CrossOrigin annotation) {
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

    private List<Class<?>> getResourceClasses(Class applicationClass) {
        List<Class<?>> resourceClasses = new ArrayList<>();

        ClassLoader classLoader = getClass().getClassLoader();
        URL fileUrl = classLoader.getResource("META-INF/resources/java.lang.Object");
        if (fileUrl != null) {
            File file = new File(fileUrl.getFile());

            try (Scanner scanner = new Scanner(file)) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resourceClasses;
    }

    private String replaceParameters(String path) {
        return path.replaceAll("\\{.*", "*");
    }

    /**
     * Check if target class is proxied.
     *
     * @param targetClass target class
     * @return true if target class is proxied
     */
    private boolean targetClassIsProxied(Class targetClass) {
        return targetClass.getCanonicalName().contains("$Proxy");
    }

    public List<CorsRegistration> getCorsRegistry() {
        return corsRegistry;
    }
}
