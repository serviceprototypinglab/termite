package ch.zhaw.splab.podilizerproc.aspect;


import ch.zhaw.splab.podilizerproc.awslambda.InvokeThread;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class Invoke {

    @Around("@annotation(lambda)")
    public Object anyExec(ProceedingJoinPoint joinPoint,
                        ch.zhaw.splab.podilizerproc.annotations.Lambda lambda) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        InvokeThread invokeThread = new InvokeThread(method, lambda, joinPoint);
        invokeThread.start();
        return null;
    }

}
