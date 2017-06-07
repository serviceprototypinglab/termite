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


