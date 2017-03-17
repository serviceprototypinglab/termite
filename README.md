## Overview
This is another research project of "Lambdalizing" process after [Podilizer](https://github.com/serviceprototypinglab/podilizer) in scope of Service Tooling initiative. The idea of research is to develop library which would take care of automatic Lambda functions generating and invokers creating. This library could be used both in existing projects and in developing from scratch.

## Idea
The idea of library is to use java custom annotations for marking and configuring Lambda functions. This approach allows to abstract the implementation of Lambdalizing which helps developer to focus on the actuall code writing. Java annotations already recomended themselfs being used in successfull projects, for instance [Spring](https://spring.io/) frameworks for java web development. We want to create a convenient tool with least configurations to simplify Lambdalizing process and help users to get involved into FaaS. 

## Implementation
In the end library should create Lambda functions based on the annotated methods and then delpoy them into existing FaaS service([AWS Lambda](https://aws.amazon.com/lambda/) as first implementation). Also library generates code which invokes appropriate Lambda fucntion and integrates in in the runtime workflow.