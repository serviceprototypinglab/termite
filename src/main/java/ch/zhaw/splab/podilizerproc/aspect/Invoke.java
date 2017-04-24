package ch.zhaw.splab.podilizerproc.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
public class Invoke {

    @Around("@annotation(lambda)")
    public void anyExec(ProceedingJoinPoint joinPoint, ch.zhaw.splab.podilizerproc.annotations.Lambda lambda) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Class clazz = Class.forName("aws.inputgenerated.InputType");

       // Object result = joinPoint.proceed();



        System.out.println(clazz.getSimpleName() + "  " + Arrays.toString(clazz.getDeclaredMethods()));

    }
}
