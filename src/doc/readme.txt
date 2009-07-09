
Apache Wink Distribution
===============================================================================

SDK version
===============================================================================
@PROJECT_VERSION@


Wink distribution file
===============================================================================
wink-dist-@PROJECT_VERSION@.zip


Java version
===============================================================================
- Java 1.5 



List of files and directories in SDK distribution home
===============================================================================
dist
  - contains SDK jar files; wink-@PROJECT_VERSION@.jar is full version for Java 1.5,
    SDK components are separately available in subdirectory components 
docs
  - directory containing the SDK documentation
examples
  - directory containing SDK example projects
lib
  - directory containing SDK dependencies
lib\apis
  - directory containing SDK compilation dependencies only
LICENSE.txt
  - SDK license file
NOTICE.txt
  - NOTICE file corresponding to third party licenses requirements
readme.txt
  - this file
third-party-licenses.txt
  - Third party components license file


SDK installation
===============================================================================
Install SDK by unzipping the distribution file. As a result the directory 
wink-@PROJECT_VERSION@ is created. This directory is referred to as SDK_HOME.


SDK documentation
===============================================================================
$SDK_HOME/docs
  - Developer Guide (pdf)
$SDK_HOME/docs/api
  - SDK javadocs


SDK examples
===============================================================================
Directory $SDK_HOME/examples contains several examples of SDK usage. The 
examples contain source codes, project files, and other resources. Example 
projects are preconfigured to use libraries and the javadoc available in this 
distribution.

A description of an example is available in the example directory.


SDK source files
===============================================================================
SDK distribution contains sources in dist directory in jar files "*-sources.jar".
Sources of SDK components are separately available in subdirectory components. 


List of libraries SDK is dependent on
===============================================================================
geronimo-j2ee_1.4_spec-1.1.jar (only for compilation)
activation-1.1.jar
commons-lang-2.3.jar
commons-logging-1.1.jar
jaxb-api-2.1.jar
jaxb-api-2.1-sources.jar
jaxb-impl-2.1.4.jar
jaxb-impl-2.1.4-sources.jar
spring-2.5.jar
stax-api-1.0.2.jar
xercesImpl-2.6.2.jar


Maven
===============================================================================
Apache wink is built using using Maven 2, version 2.0.9


Product site
===============================================================================
http://incubator.apache.org/wink


Known issues
===============================================================================
See Jira https://issues.apache.org/jira/browse/WINK

--- readme.txt EOF ---
