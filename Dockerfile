#       Copyright 2017-2021 IBM Corp All Rights Reserved

#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at

#       http://www.apache.org/licenses/LICENSE-2.0

#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

# FROM websphere-liberty:microProfile4
FROM openliberty/open-liberty:kernel-slim-java11-openj9-ubi

COPY --chown=1001:0 server.xml /config/server.xml

# This script will add the requested XML snippets to enable Liberty features and grow image to be fit-for-purpose using featureUtility. 
# Only available in 'kernel-slim'. The 'full' tag already includes all features for convenience.
RUN features.sh

COPY --chown=1001:0 jvm.options /config/jvm.options
COPY --chown=1001:0 server/target/server-1.0-SNAPSHOT.war /config/apps/looper.war
COPY --chown=1001:0 client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar /loopctl.jar
COPY --chown=1001:0 *.p12 /config/resources/security/
COPY --chown=1001:0 keystore.xml /config/configDropins/defaults/keystore.xml
COPY --chown=1001:0 client/loopctl.sh /loopctl.sh

EXPOSE 9080

RUN configure.sh
