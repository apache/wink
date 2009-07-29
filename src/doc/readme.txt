Apache Wink Distribution
===============================================================================

Wink version
===============================================================================
@PROJECT_VERSION@

Wink distribution content
===============================================================================
dist
  - contains all Wink binaries. 
  - wink-@PROJECT_VERSION@.jar is all-in-one archive
docs
  - contains Wink documentation (API, User Guide)
examples
  - contains Wink examples
lib
  - contains Wink dependencies
lib\apis
  - contains Wink compile-time dependencies
lib\examples
  - contains Wink client examples dependencies

Wink installation
===============================================================================
Install Wink by unzipping the distribution archive. As a result the directory 
apache-wink-@PROJECT_VERSION@ is created. This directory is referred to as WINK_HOME.

Wink examples
===============================================================================
Directory $WINK_HOME/examples contains Wink examples. The examples contain source codes, 
project files, and other resources. Example projects are pre-configured to use libraries and the javadoc available in this 
distribution.
A description of an example is available in the example directory.

Wink sources 
===============================================================================
Wink distribution contains sources under dist directory in jar files "*-sources.jar".

Maven
===============================================================================
Apache Wink is built using using Maven 2, version 2.0.9

Product site
===============================================================================
http://incubator.apache.org/wink

Issues 
===============================================================================
Jira https://issues.apache.org/jira/browse/WINK

--- readme.txt EOF ---
