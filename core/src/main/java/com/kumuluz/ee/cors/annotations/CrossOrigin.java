package com.kumuluz.ee.cors.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zvoneg on 26/07/17.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CrossOrigin {

    String name() default "";

    boolean allowGenericHttpRequests() default true;

    String allowOrigin() default "*";

    boolean allowSubdomains() default false;

    String supportedMethods() default "GET, POST, HEAD, OPTIONS";

    String supportedHeaders() default "*";

    String exposedHeaders() default "";

    boolean supportsCredentials() default true;

    int maxAge() default -1;

    boolean tagRequests() default false;
}