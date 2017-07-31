package com.kumuluz.ee.cors.utils;

import com.kumuluz.ee.cors.annotations.CrossOrigin;
import com.kumuluz.ee.cors.config.CorsConfig;
import com.kumuluz.ee.cors.config.CorsRegistration;

import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Created by zvoneg on 27/07/17.
 */
public class ServletCrossOriginAnnotationProcessorUtil implements CrossOriginAnnotationProcessorUtil {

    private static final Logger LOG = Logger.getLogger(ServletCrossOriginAnnotationProcessorUtil.class.getName());

    private List<CorsRegistration> corsRegistry;

    public void init() {
        corsRegistry = new ArrayList<>();
        for (Class servletClass : getResourceClasses(null)) {
            corsRegistry.addAll(getCorsRegistrations(servletClass));
        }
    }

    public List<CorsRegistration> getCorsRegistrations(Class<?> servletClass) {

        List<CorsRegistration> corsRegistrations = new ArrayList<>();

        if (targetClassIsProxied(servletClass)) {
            servletClass = servletClass.getSuperclass();
        }

        WebServlet servletAnnotation = servletClass.getAnnotation(WebServlet.class);
        if (servletAnnotation != null) {
            if (servletAnnotation.value().length > 0) {
                for (String urlPattern : servletAnnotation.value()) {
                    CrossOrigin servletCrossOriginAnnotation = servletClass.getAnnotation(CrossOrigin.class);

                    if (servletCrossOriginAnnotation != null) {
                        CorsConfig servletCorsConfig = new CorsConfig().applyPermitDefaultValues();
                        CorsConfigUtil.updateCorsConfig(servletCorsConfig, servletCrossOriginAnnotation);

                        CorsRegistration registration = new CorsRegistration("/" + urlPattern, servletCorsConfig);

                        corsRegistrations.add(registration);
                    }

                }
            } else {
                CrossOrigin servletCrossOriginAnnotation = servletClass.getAnnotation(CrossOrigin.class);

                if (servletCrossOriginAnnotation != null) {
                    CorsConfig servletCorsConfig = new CorsConfig().applyPermitDefaultValues();
                    CorsConfigUtil.updateCorsConfig(servletCorsConfig, servletCrossOriginAnnotation);

                    CorsRegistration registration = new CorsRegistration("", servletCorsConfig);
                    corsRegistrations.add(registration);
                }
            }
        }

        return corsRegistrations;
    }

    public List<Class<?>> getResourceClasses(Class applicationClass) {
        List<Class<?>> servletClasses = new ArrayList<>();

        ClassLoader classLoader = getClass().getClassLoader();
        URL fileUrl = classLoader.getResource("META-INF/servlets/java.lang.Object");
        if (fileUrl != null) {
            File file = new File(fileUrl.getFile());

            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String className = scanner.nextLine();
                    try {
                        Class servletClass = Class.forName(className);
                        servletClasses.add(servletClass);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                scanner.close();
            } catch (IOException e) {
                LOG.warning(e.getMessage());
            }
        }

        return servletClasses;
    }

    public List<CorsRegistration> getCorsRegistry() {
        return corsRegistry;
    }
}
