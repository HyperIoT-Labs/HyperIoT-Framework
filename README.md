# HyperIoT Framework #

First microservices oriented framework.

## Deploy new framework version
This task must be used in a release to update HyperIoT/Karaf version inside the entire project.
It update also the version of test distribution.

gradle newRelease -Dhyperiot.version=X -Dkaraf.version=Y

#Install new plugin version
This task must be used in the plugin folder to install new plugin version.

gradle mavenToPublishLocal
