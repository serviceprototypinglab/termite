package ch.zhaw.splab.podilizerproc.aspect;


import ch.zhaw.splab.podilizerproc.annotations.Lambda;
import ch.zhaw.splab.podilizerproc.awslambda.AwsCredentialsReader;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Aspect
public class Invoke {

    @Around("@annotation(lambda)")
    public Object anyExec(ProceedingJoinPoint joinPoint,
                          ch.zhaw.splab.podilizerproc.annotations.Lambda lambda) throws Throwable {
        System.out.println("[TERMITE] Annotation invoked");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        return invokeOnLambda(method, lambda, joinPoint);
    }

    private Object invokeOnLambda(Method method, Lambda lambda, ProceedingJoinPoint joinPoint) throws Throwable {
        Thread.dumpStack();
        Class inClazz = null;
        Class outClazz = null;
        try {
            inClazz = Class.forName(getInputPackage(method));
            outClazz = Class.forName(getOutputPackage(method));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        AwsCredentialsReader credentialsReader = new AwsCredentialsReader();
        credentialsReader.read();
        String awsAccessKeyId = credentialsReader.getAwsAccessKeyId();
        String awsSecretKeyAccessKey = credentialsReader.getAwsSecretAccessKey();
        String regionName = lambda.region();
        String functionName = getFunctionName(method);

        AWSCredentials credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKeyAccessKey);
        Regions region = Regions.fromName(regionName);


        AWSLambdaClientBuilder clientBuilder = AWSLambdaClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region);

        if (!lambda.endPoint().equals("")) {
            clientBuilder = clientBuilder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(lambda.endPoint(), regionName));
        }
        AWSLambda awsLambda = clientBuilder.build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String json = "";
        try {
            List<Object> ctorArgs = new ArrayList<>();
            if (!Modifier.isStatic(joinPoint.getSignature().getModifiers())) {
                ctorArgs.add(joinPoint.getTarget());
            }
            ctorArgs.addAll(Arrays.asList(joinPoint.getArgs()));


            Constructor<Object> correctCtor = null;
            for (Constructor<Object> constructor : inClazz.getConstructors()) {
                if (constructor.getParameterCount() == ctorArgs.size()) {
                    correctCtor = constructor;
                    break;
                }
            }

            Object inputObj = correctCtor.newInstance(ctorArgs.toArray());
            json = objectMapper.writeValueAsString(inputObj);
            // TODO: Remove these verbose printouts (Or make log level configurable)
            System.out.println("[TERMITE] Sending: " + json);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | JsonProcessingException e) {
            e.printStackTrace();
        }
        Object outObj = null;
        Object methodResult = null;

        try {
            InvokeRequest invokeRequest = new InvokeRequest();
            invokeRequest.setFunctionName(functionName);
            invokeRequest.setPayload(json);
            String resultJson = byteBufferToString(awsLambda.invoke(invokeRequest).getPayload(), StandardCharsets.UTF_8);
            System.out.println("[TERMITE] Receiving: " + resultJson);
            try {
                outObj = objectMapper.readValue(resultJson, outClazz);
                objectMapper.readerForUpdating(outObj).readValue(resultJson);
            } catch (Throwable t) {
                System.out.println("[TERMITE] Failed to deserialize result.");
            }
            // TODO: Remove these verbose printouts (Or make log level configurable)

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            if (outObj != null && signature != null && !void.class.equals(signature.getReturnType())) {
                // GetResult is only generated for non void methods
                Method getResultMethod = outObj.getClass().getDeclaredMethod("getResult");
                methodResult = getResultMethod.invoke(outObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Function " + method.getName() + " is unreachable. Processing locally...");

            // Note: While using the joinPoint to proceed normal execution, the code will sometimes be executed twice.
            // This is a known AspectJ Bug documented here: https://github.com/spring-projects/spring-framework/issues/24046
            methodResult = joinPoint.proceed();
        }

        try {
            if (outObj != null) {
                String functionReport = "Thread of Function " + method.getName() + " invocation was finished. " +
                        "Function performed at - " + outObj.getClass().getDeclaredMethod("getDefaultReturn", null).invoke(outObj) +
                        " - for " + outObj.getClass().getDeclaredMethod("getTime", null).invoke(outObj) + " ms";
                if (!method.getReturnType().toString().equals("void")) {
                    functionReport += "; Return value is: " + outObj.getClass().getDeclaredMethod("getResult", null).invoke(outObj);
                }
                System.out.println(functionReport);
            } else {
                System.out.println("Processed " + method.getName() + " locally with result " + methodResult);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return methodResult;
    }


    /**
     * Generates package name for input type based on method signature
     *
     * @param method is method signature object
     * @return {@link String} package name of InputType
     */
    private String getInputPackage(Method method) {
        String fullClassName = method.getDeclaringClass().getName();
        return "aws." + fullClassName + "." + method.getName() + method.getParameterCount() + ".InputType";
    }

    /**
     * Generates package name for output type based on method signature
     *
     * @param method is method signature object
     * @return {@link String} package name of OutputType
     */
    private String getOutputPackage(Method method) {
        String fullClassName = method.getDeclaringClass().getName();
        return "aws." + fullClassName + "." + method.getName() + method.getParameterCount() + ".OutputType";
    }

    /**
     * Generates function name for annotated method over the load process
     *
     * @param method is annotated method to generate lambda function name for
     * @return {@link String} name of format 'package_class_method_#argsNumber'
     */
    private String getFunctionName(Method method) {
        String result = method.getDeclaringClass().getName().replace('.', '_');
        result += "_" + method.getName();
        result += method.getParameterCount();
        return result;
    }

    private static String byteBufferToString(ByteBuffer buffer, Charset charset) {
        byte[] bytes;
        if (buffer.hasArray()) {
            bytes = buffer.array();
        } else {
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        }
        return new String(bytes, charset);
    }
}
