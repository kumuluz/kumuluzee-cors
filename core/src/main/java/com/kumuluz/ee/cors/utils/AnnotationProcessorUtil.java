package com.kumuluz.ee.cors.utils;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by zvoneg on 31/07/17.
 */
public class AnnotationProcessorUtil {

    public static void readOldFile(Set<String> content, Reader reader) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                content.add(line);
                line = bufferedReader.readLine();
            }
        }
    }
}
