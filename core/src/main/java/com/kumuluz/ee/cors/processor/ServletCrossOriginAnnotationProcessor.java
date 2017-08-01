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
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by zvoneg on 27/07/17.
 */

public class ServletCrossOriginAnnotationProcessor extends AbstractProcessor {

    private static final Logger LOG = Logger.getLogger(ServletCrossOriginAnnotationProcessor.class.getName());

    private Set<String> servletElementNames = new HashSet<>();

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

        elements = roundEnv.getElementsAnnotatedWith(CrossOrigin.class);
        elements.forEach(e -> {
            if (e.getAnnotation(WebServlet.class) != null) {
                getElementName(servletElementNames, e);
            }
        });

        try {
            AnnotationProcessorUtil.writeFile(servletElementNames, "META-INF/servlets/java.lang.Object", filer);
        } catch (IOException e) {
            LOG.warning(e.getMessage());
        }

        return true;
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
