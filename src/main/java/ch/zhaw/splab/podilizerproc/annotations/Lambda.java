package ch.zhaw.splab.podilizerproc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark the method you want to deploy to FaaS service
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Lambda {
    String region() default "us-west-2";

    String endPoint() default "";

    int timeOut() default 60;

    int memorySize() default 1024;
}
