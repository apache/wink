This document describes the examples available in Wink SDK distribution
===============================================================================

Available examples:

1. "app" module contains simple applications that use both JAX-RS and Wink extension features (ordered by the level of complexity)
- HelloWorld
- Bookmarks
- QADefect
- QADefectWithSpring
- SimpleDefects

2. "core" module contains JAX-RS core features
- CustomContext
- CustomExceptionMapper
- Jaxb
- Preconditions

3. "client" module contains Wink client examples
- QADefects-client

4. "ext" module contains examples that demonstrate Wink additional features 
- Asset
- DynamicResource
- History
- LinkBuilders
- RuntimeRegistration
- Spring
- Scope
- WebDAV


For more details regarding each example, please refer to the readme.txt file located
in each example directory.


Build examples using Ant (SDK libraries are taken from SDK distribution)
===============================================================================
Each example directory contains a build.xml for use with Ant. In order to build 
a file for the example go to the specific example directory and run "ant". 
The WAR file is created in the same example directory.
Examples were tested with Ant 1.6.5. 
Apache Ant project: http://ant.apache.org/

Ant targets (run e.g. "ant compile"):
- dist - the default target (to build WAR)
- init - preparation before compile target
- compile - compilation of source codes into directory "build" before dist
- clean - the clean-up target

Note: Each example results in its own WAR file, meaning there is no common WAR
file for all examples.


Build examples using Maven 2 (SDK libraries are taken from Maven repository)
===============================================================================
Each example directory contains a pom.xml for use with Maven.
The Maven repository for SDK is located at server "ilbldwin04.mercury.co.il".
Before using Maven, you must:
   1) Install and configure Maven 2.0.9 or later (http://maven.apache.org/)
      a) Download and install Maven 2.0.9 or later 
      b) Put "%MAVEN_HOME%/bin" into your path (for MS Windows)    

   2) Set the environment variable JAVA_HOME to the location where JDK 1.5
      is installed.

Maven targets (to build use "mvn package"):
- compile - compilation of source codes into directory "target".
- package - builds the WAR file in the example directory "target".
- install - creates a package and uploads it to the local Maven repository.
- clean - deletes the directory "target".
- idea:idea - create IntelliJ Idea project files.
- eclipse:eclipse - create Eclipse project files.

Examples were tested with Maven 2.0.9.


Java IDE support
===============================================================================
IntelliJ Idea and Eclipse project files can be created in the example 
directories by using Maven.
The project files were tested with IntelliJ Idea 6.0 and Eclipse 3.3.

Note: Run "mvn install" before creating project files with Maven 

Create IntelliJ Idea project files by running "mvn idea:idea" in the example
directory. Open the created *.ipr file in IntelliJ Idea.

Create Eclipse project files by running "mvn eclipse:eclipse" in the example
directory. Create an Eclipse workspace and use the workspace path when running:

   mvn -Declipse.workspace=<path-to-eclipse-workspace> eclipse:add-maven-repo 

This command sets the project variable M2_REPO, which is used in library paths.
In Eclipse, select "File->Import->General->Existing Projects into Workspace",
click Next, select the example directory, and click Finish.

Note: Eclipse project files for plugin WTP are also created.


Deploy and run the examples
===============================================================================

Use the command "mvn jetty:run-war" in order to run the example with jetty web server.
From the distribution zip file extract the required example and run the "mvn jetty:run-war" command from the command line. 
This will start the jetty web server and will enable access to the example application through 8080 port.
e.g.:
C:\QADefect> mvn jetty:run-war
after the server has started, access the application through: http://localhost:8080/QADefect/rest

Alternatively, example WAR files can be deployed to any servlet container.
Use e.g. hot-deployment: copy a created WAR file into the deployment directory 
e.g. "JBOSS/server/default/deploy".

Product site
===============================================================================
http://incubator.apache.org/wink/

--- readme.txt EOF ---
