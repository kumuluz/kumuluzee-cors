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
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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

        try {
            Class.forName("javax.ws.rs.core.Application");
        } catch (ClassNotFoundException e) {
            LOG.info("javax.ws.rs.core.Application not found, skipping JAX-RS CORS");
            return false;
        }

        elements = roundEnv.getElementsAnnotatedWith(CrossOrigin.class);
        elements.forEach(e -> {
            if (e.getAnnotation(ApplicationPath.class) != null) {
                getElementName(applicationElementNames, e);
            } else {
                getElementName(resourceElementNames, e);
            }
        });

        // If Application level annotation exists scann all resources
        if (applicationElementNames.size() > 0) {
            elements = roundEnv.getElementsAnnotatedWith(Path.class);
            elements.forEach(e -> getElementName(resourceElementNames, e));
        }

        elements = roundEnv.getElementsAnnotatedWith(ApplicationPath.class);
        elements.forEach(e -> getElementName(applicationElementNames, e));

        try {
            writeFile(resourceElementNames, "META-INF/resources/java.lang.Object");
            writeFile(applicationElementNames, "META-INF/services/javax.ws.rs.core.Application");
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

    private void writeFile(Set<String> content, String resourceName) throws IOException {
        FileObject file = readOldFile(content, resourceName);
        if (file != null) {
            try {
                writeFile(content, resourceName, file);
                return;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        writeFile(content, resourceName, null);
    }

    private void writeFile(Set<String> content, String resourceName, FileObject overrideFile) throws IOException {
        FileObject file = overrideFile;
        if (file == null) {
            file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
        }
        try (Writer writer = file.openWriter()) {
            for (String serviceClassName : content) {
                writer.write(serviceClassName);
                writer.write(System.lineSeparator());
            }
        }
    }

    private FileObject readOldFile(Set<String> content, String resourceName) throws IOException {
        Reader reader = null;
        try {
            final FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
            reader = resource.openReader(true);
            AnnotationProcessorUtil.readOldFile(content, reader);
            return resource;
        } catch (FileNotFoundException e) {
            // close reader, return null
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return null;
    }
}
