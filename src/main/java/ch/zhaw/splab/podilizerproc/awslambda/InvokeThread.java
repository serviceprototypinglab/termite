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
        Class clazz = null;
        try {
            clazz = Class.forName(getInputPackage(method));
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
            Object inputObj = clazz.getConstructors()[0].newInstance(joinPoint.getArgs());
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
        try {
            InvokeRequest invokeRequest = new InvokeRequest();
            invokeRequest.setFunctionName(functionName);
            invokeRequest.setPayload(json);
            lambdaClient.invoke(invokeRequest).getPayload();
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Function " + method.getName() + " is unreachable. Processing locally...");
            try {
                joinPoint.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        System.out.println("The thread of Function " + method.getName() + " invocation was finished.");
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
}
