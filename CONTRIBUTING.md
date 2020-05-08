# Contributing to Java for IBM Watson IoT Platform

## Running the Tests
```
set JAVA_HOME=/path/to/java/sdk
set MAVEN_HOME=/path/to/maven
set WIOTP_API_KEY=a-xxxxx-xxxxxxxxxx
set WIOTP_API_TOKEN=xxxxxxxxxxxxxxxxxx
set WIOTP_IDENTITY_ORGID=xxxxx
set WIOTP_IDENTITY_TYPEID=test
set WIOTP_IDENTITY_DEVICEID=000001
set WIOTP_AUTH_TOKEN=passw0rd
```

Test results are automatically submitted to Coveralls.  Ensure that new commits don't lower the code coverage %: https://coveralls.io/github/ibm-watson-iot/iot-java


## Deploying to Maven

We live here: https://mvnrepository.com/artifact/com.ibm.wiotp

```
mvn versions:set -DnewVersion=0.1.0
mvn clean package -Dmaven.test.skip=true
mvn deploy -P sign-artifacts
```

Go to https://oss.sonatype.org/#stagingRepositories
- Close the staging repo after verifying content
- Promote the repo to release after it's closed

## Useful links
- Summary: https://central.sonatype.org/pages/ossrh-guide.html
- Maven overview: https://central.sonatype.org/pages/apache-maven.html
- Requirements for release in Maven Central:  https://central.sonatype.org/pages/requirements.html
- Setting up PGP: https://central.sonatype.org/pages/working-with-pgp-signatures.html
- Releasing the deployment:  https://central.sonatype.org/pages/releasing-the-deployment.html
- Check released artifacts: https://search.maven.org/search?q=com.ibm.wiotp (Sync to Maven Central occurs roughly every two hours)
- Sonatype staging: https://oss.sonatype.org/content/groups/staging/com/ibm/wiotp/
- Sonatype release: https://oss.sonatype.org/content/groups/public/com/ibm/wiotp/
- Snapshots: https://oss.sonatype.org/content/repositories/snapshots/com/ibm/wiotp/
- Releases: https://repo1.maven.org/maven2/com/ibm/wiotp
