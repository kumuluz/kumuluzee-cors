package com.kumuluz.ee.cors.processor;

import com.kumuluz.ee.cors.annotations.CrossOrigin;
import com.kumuluz.ee.cors.utils.AnnotationProcessorUtil;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.servlet.annotation.WebServlet;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by zvoneg on 27/07/17.
 */

public class JaxRsCrossOriginAnnotationProcessor extends AbstractProcessor {

    private static final Logger LOG = Logger.getLogger(JaxRsCrossOriginAnnotationProcessor.class.getName());

    private Set<String> resourceElementNames = new HashSet<>();
    private Set<String> applicationElementNames = new HashSet<>();

    private Filer filer;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("*");
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements;

        boolean processed = true;

        try {
            Class.forName("javax.ws.rs.core.Application");
        } catch (ClassNotFoundException e) {
            LOG.info("javax.ws.rs.core.Application not found, skipping JAX-RS CORS annotation processing");
            return false;
        }

        elements = roundEnv.getElementsAnnotatedWith(CrossOrigin.class);
        for (Element e : elements) {
            if (e.getAnnotation(ApplicationPath.class) != null) {
                getElementName(applicationElementNames, e);
            } else if (e.getAnnotation(Path.class) != null) {
                getElementName(resourceElementNames, e);
            } else if (e.getAnnotation(WebServlet.class) != null) {
                processed = false;
            }
        }

        // If Application level annotation exists scann all resources
        if (applicationElementNames.size() > 0) {
            elements = roundEnv.getElementsAnnotatedWith(Path.class);
            elements.forEach(e -> getElementName(resourceElementNames, e));
        }

        elements = roundEnv.getElementsAnnotatedWith(ApplicationPath.class);
        elements.forEach(e -> getElementName(applicationElementNames, e));

        try {
            AnnotationProcessorUtil.writeFile(resourceElementNames, "META-INF/resources/java.lang.Object", filer);
            AnnotationProcessorUtil.writeFile(applicationElementNames, "META-INF/services/javax.ws.rs.core.Application", filer);
        } catch (IOException e) {
            LOG.warning(e.getMessage());
        }

        return processed;
    }

    private void getElementName(Set<String> corsElementNames, Element e) {

        ElementKind elementKind = e.getKind();

        if (elementKind.equals(ElementKind.CLASS)) {
            corsElementNames.add(e.toString());
        } else if (elementKind.equals(ElementKind.METHOD)) {
            corsElementNames.add(e.getEnclosingElement().toString());
        }
    }
}
