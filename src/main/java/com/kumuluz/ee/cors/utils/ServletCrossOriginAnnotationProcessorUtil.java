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

import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * ServletCrossOriginAnnotationProcessorUtil class.
 *
 * @author Zvone Gazvoda
 * @since 1.0.0
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
                        CorsConfigUtil.updateCorsConfig(servletCorsConfig, servletCrossOriginAnnotation, servletClass, null);

                        CorsRegistration registration = new CorsRegistration("/" + urlPattern, servletCorsConfig);

                        corsRegistrations.add(registration);
                    }

                }
            } else {
                CrossOrigin servletCrossOriginAnnotation = servletClass.getAnnotation(CrossOrigin.class);

                if (servletCrossOriginAnnotation != null) {
                    CorsConfig servletCorsConfig = new CorsConfig().applyPermitDefaultValues();
                    CorsConfigUtil.updateCorsConfig(servletCorsConfig, servletCrossOriginAnnotation, servletClass, null);

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
        InputStream is = classLoader.getResourceAsStream("META-INF/servlets/java.lang.Object");

        if (is != null) {

            Scanner scanner = new Scanner(is);
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
        }

        return servletClasses;
    }

    public List<CorsRegistration> getCorsRegistry() {
        return corsRegistry;
    }
}
