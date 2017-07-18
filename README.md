![logo](/Documentation/logo/v0_5_0.png=100x "logo")

## Overview
Termite is another research project of "Lambdafication" process after
 [Podilizer](https://github.com/serviceprototypinglab/podilizer) in scope of Service Tooling initiative.
 We developed the library which would take care of automatic Lambda functions generating based on the the written
 code. This library could be used both in existing projects and in developing from scratch.

## Idea
The idea of library is to use java custom annotations for marking and configuring Lambda functions.
This approach allows to abstract the function creation process which helps developer to focus on the
actual code writing. Java annotations have already proven themselves being used in successful projects,
for instance [Spring](https://spring.io/) framework.
 We want to create a convenient tool with least configurations to simplify Lambdalizing process and help users
 to get involved into FaaS.

## Implementation

The library does two main jobs: creating functions and invoking them when it's needed.

To implement the first part we used java built-in annotation processor linked to custom annotation. Annotation
processors are being performed over the compilation phase. It means that after compilation of your project marked
with particular annotations methods are already created at provider's service
([AWS Lambda](https://aws.amazon.com/lambda/) as first implementation).

The invoking part of the library was implemented using java aspectj library, that allows to intercept the
appropriate method call over the runtime phase and call already created Lambda function instead of the local method.
Furthermore every function invocation starts in the new thread, it provides the use of the such strength of FaaS as
auto-scaling.

![Structure diagram](/Documentation/diagrams/Termite_structure_simple.png?raw=true "structure")

## How to

Before using Termite directly from our Git repository, make sure to fulfil the following prerequisites:

* AWS account and credentials configured at your machine (aws command-line tool should work: aws lambda list-functions)
* JDK 1.8+ installed
* Maven installed

Clone from repository (In the folder ‘example’ you can find example project which uses Termite):
```
clone https://github.com/serviceprototypinglab/termite.git
```
Compile the project:
```
cd termite
mvn clean install
```
Afterwards, Termite is ready to be used within your Java application. Assuming you use Maven, create your own Maven project and configure dependencies. In case you use another build system, Termite does not offer integration yet but we are happy to accept patches of course.
```
<dependency>
    <groupId>ch.zhaw.splab.servicetooling</groupId>
    <artifactId>Termite</artifactId>
    <version>0.1</version>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>${aspectj.version}</version>
    <scope>runtime</scope>
</dependency>
```
Correct execution is able only with the following argument to the Maven execution plugin:
```
-javaagent:${settings.localRepository}/org/aspectj/aspectjweaver/${aspectj.version}/
aspectjweaver-${aspectj.version}.jar
```
Also you need the generic Maven plugin for compiling generated sources because Termite will generate some source code files:
```xml
<plugin>
    <groupId>org.bsc.maven</groupId>
    <artifactId>maven-processor-plugin</artifactId>
    <version>2.2.4</version>
        <executions>
            <execution>
                <id>process</id>
                <goals>
                    <goal>process</goal>
                </goals>
                <phase>process-sources</phase>
                <configuration>
                    <proc>none</proc>
                    <outputDirectory>${generated.sources.directory}</outputDirectory>
                    <processors>
                        <processor>ch.zhaw.splab.podilizerproc.annotations.LambdaProcessor
                        </processor>
                    </processors>
                </configuration>
            </execution>
        </executions>
</plugin>
```
A full example of a Maven configuration file is located in the Termite repository at
```
example/pom.xml
```
Afterwards, you’re ready to go serverless! Annotate functions you want to upload with “@Lambda”, see the example above.

Compile the created project:
```
mvn clean install
```
