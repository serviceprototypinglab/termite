package ch.zhaw.splab.podilizerproc.awslambda;


import ch.zhaw.splab.podilizerproc.statistic.Timer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class JarUploader {
    private String region;
    private String runtime = "java11";
    private String role;
    private String functionName;
    private String zipFile;
    private String handler;
    private String endPoint;
    private int timeout;
    private int memorySize;

    JarUploader(String functionName, String zipFile, String handler, String region, int timeout, int memorySize, String endPoint) {
        this.functionName = functionName;
        this.zipFile = zipFile;
        this.handler = handler;
        this.timeout = timeout;
        this.memorySize = memorySize;
        this.region = region;
        this.endPoint = endPoint;
    }

    /**
     * Writes command into CMD and run it
     *
     * @param command the {@code String} to be run
     */
    private void writeIntoCMD(final String command) {
        Runtime runtime = Runtime.getRuntime();
        try {
            final Process process = runtime.exec(command);
            new Thread(new Runnable() {
                public void run() {
                    Timer.start();
                    boolean error = false;
                    BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    //String line = null;
                    BufferedReader outErrors = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String lineError = null;
                    try {
                        if (command.startsWith("aws sts")) {
                            //System.out.println("\n\naws sts output: " + input.readLine() + "\n\n");
                            role = "arn:aws:iam::" + input.readLine() + ":role/service-role/lambda_basic_execution";
                            return;
                        }
//                        while ((line = input.readLine()) != null)
//                            System.out.println(line);
                        while ((lineError = outErrors.readLine()) != null) {
                            if (command.startsWith("aws lambda create-function") ||
                                    command.startsWith("aws lambda delete-function")){
                                error = true;
                            }
                            if (!command.startsWith("aws lambda delete-function"))
                            System.err.println(lineError);
                        }
                        if (command.startsWith("aws lambda create-function") && !error){
                            Timer.stop();
                            System.out.println("[TERMITE] New Lambda Function " + functionName + " was successfully created");
                            System.out.println("[TERMITE] Uploading time is " + Timer.getFormatedTime());
                        }
                        if (command.startsWith("aws lambda delete-function") && !error){
                            Timer.stop();
                            System.out.println("[TERMITE] Old function " + functionName + " was successfully deleted");
                            System.out.println("[TERMITE] Deletion time is " + Timer.getFormatedTime());
                        }
                        System.out.println("\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the CMD command which creates 'Lambda Function'
     *
     * @return generated {@code String}
     */
    private String getCommand() {
//        AwsCredentialsReader credentialsReader = new AwsCredentialsReader();
//        credentialsReader.read();
        String result = "aws lambda create-function" +
                " --function-name " + functionName +
                " --region " + region +
                " --zip-file fileb://" + zipFile +
                " --role " + role +
//                " --environment Variables={awsAccessKeyId=" + credentialsReader.getAwsAccessKeyId() + "," +
//                "awsSecretAccessKey=" + credentialsReader.getAwsSecretAccessKey() + "," +
//                "awsRegion=" + region + "}" +
                " --handler " + handler +
                " --runtime " + runtime +
                " --timeout " + timeout +
                " --memory-size " + memorySize;
        if (!endPoint.equals("")){
            result += " --endpoint-url " + endPoint;
        }
        System.out.println("[TERMITE] Building Command:\n" + result);
        return result;
    }

    private String getDeleteCommand() {
        String result = "aws lambda delete-function " +
                "--region " + region + " " +
                "--function-name " + functionName;
        if (!endPoint.equals("")){
            result += " --endpoint-url " + endPoint;
        }
        return result;
    }

    private String getRoleCommand() {
        return "aws sts get-caller-identity --output text --query Account";
    }

    /**
     * Creates Lambda Function on AWS and uploads the source code jar
     */
    void uploadFunction() {
        writeIntoCMD(getRoleCommand());
        Timer.start();
        writeIntoCMD(getDeleteCommand());
        writeIntoCMD(getCommand());
    }


}
