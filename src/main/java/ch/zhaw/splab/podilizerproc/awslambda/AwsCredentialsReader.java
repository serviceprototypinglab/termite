package ch.zhaw.splab.podilizerproc.awslambda;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class AwsCredentialsReader {
    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    // TODO: 4/6/17 Implement aws user's configurations handling
    /**
     * Reads credentials from aws default config file
     */
    public void read(){
        String awsCredentialsPath = System.getProperty("user.home") + "/.aws/credentials";
        try {
            Files.lines(Paths.get(awsCredentialsPath)).forEach(this::readLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    /**
     * Reads aws keys from line
     * @param line {@link String}
     */
    private void readLine(String line){
        String accessKeyAttr = "aws_access_key_id = ";
        String secretKeyAttr = "aws_secret_access_key = ";
        if (line.startsWith(accessKeyAttr)){
            awsAccessKeyId = line.split(accessKeyAttr)[1];
        }
        if (line.startsWith(secretKeyAttr)){
            awsSecretAccessKey = line.split(secretKeyAttr)[1];
        }

    }
}
