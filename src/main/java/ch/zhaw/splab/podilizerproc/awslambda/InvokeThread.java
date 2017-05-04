package ch.zhaw.splab.podilizerproc.awslambda;

import ch.zhaw.splab.podilizerproc.annotations.Lambda;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class InvokeThread extends Thread {
    private Method method;
    private Lambda lambda;
    private ProceedingJoinPoint joinPoint;

    public InvokeThread(Method method, Lambda lambda, ProceedingJoinPoint joinPoint) {
        super();
        this.method = method;
        this.lambda = lambda;
        this.joinPoint = joinPoint;
    }

    @Override
    public void run() {
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
        Region region = Region.getRegion(Regions.fromName(regionName));
        AWSLambdaClient lambdaClient = new AWSLambdaClient(credentials);
        lambdaClient.setRegion(region);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String json = "";
        try {
            Object inputObj = inClazz.getConstructors()[0].newInstance(joinPoint.getArgs());
            json = objectMapper.writeValueAsString(inputObj);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Object outObj = null;
        try {
            InvokeRequest invokeRequest = new InvokeRequest();
            invokeRequest.setFunctionName(functionName);
            invokeRequest.setPayload(json);
            outObj = objectMapper.readValue(byteBufferToString( lambdaClient.invoke(invokeRequest).getPayload(),
                    Charset.forName("UTF-8")), outClazz);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Function " + method.getName() + " is unreachable. Processing locally...");
            try {
                Object tmp = joinPoint.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }
        try {
            System.out.println("The thread of Function " + method.getName() + " invocation was finished. " +
                    "Function performed at - " + outObj.getClass().getDeclaredMethod("getDefaultReturn", null).invoke(outObj) +
                    " - for " + outObj.getClass().getDeclaredMethod("getTime", null).invoke(outObj) + " ms");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * Generates package name for input type based on method signature
     * @param method is method signature object
     * @return {@link String} package name of InputType
     */
    private String getInputPackage(Method method){
        String fullClassName = method.getDeclaringClass().getName();
        return  "aws." + fullClassName + "." + method.getName() + method.getParameterCount() + ".InputType";
    }
    /**
     * Generates package name for output type based on method signature
     * @param method is method signature object
     * @return {@link String} package name of OutputType
     */
    private String getOutputPackage(Method method){
        String fullClassName = method.getDeclaringClass().getName();
        return  "aws." + fullClassName + "." + method.getName() + method.getParameterCount() + ".OutputType";
    }

    /**
     * Generates function name for annotated method over the load process
     * @param method is annotated method to generate lambda function name for
     * @return {@link String} name of format 'package_class_method_#argsNumber'
     */
    private String getFunctionName(Method method){
        String result = method.getDeclaringClass().getName().replace('.', '_');
        result += "_" + method.getName();
        result += method.getParameterCount();
        return result;
    }

    public static String byteBufferToString(ByteBuffer buffer, Charset charset) {
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
