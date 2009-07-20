Wink Distribution
==============================
==============================

The distribution of Wink REST SDK occurs during the normal build process at the deploy phase.
All the configuration of the distribution is taken from the maven configuration files.

It's possible to run the generation of the distribution zip by running "mvn antrun:run"
from the command line. The prerequisites of this step is to run "mvn package" and "mvn site" of the root.
Zip file will be created under target folder.

Note: running "mvn site" may take a long time. Running "mvn javadoc:javadoc" should be enough to run ant script.