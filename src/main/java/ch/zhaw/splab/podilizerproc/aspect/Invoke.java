package ch.zhaw.splab.podilizerproc.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class Invoke {

    @Before("annotatedMethod()")
    public void anyExec(JoinPoint joinPoint){
        System.out.println(joinPoint);
    }

    @Pointcut("execution(* *(..))")
    public void annotatedMethod(){

    }
}
