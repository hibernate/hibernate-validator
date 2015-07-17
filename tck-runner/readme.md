# Hibernate Validator TCK Runner

This maven module allows to run the Bean Validation TCK against Hibernate Validator.
There are several ways of doing this:

## Standalone

You can run the test as normal unit test using a JVM forked by the Surefire plugin. This is the default mode and
used when running

    $ mvn clean test

By default this will run the tests with a security manager enabled in order to make sure Hibernate Validator invokes
security-relevant APIs using privileged actions. You can run the tests without the security manager e.g. for analysis
purposes like this:

    $ mvn clean test -Dwith-security-manager=false

The policy file is located at src/test/resources/test.policy. You may need to adapt the URL of the Hibernate Validator
engine entry depending on your specific set-up, e.g. when obtaining these classes from engine/target/classes while
running the tests from within an IDE.

## In container

You can also run the TCK test against Wildfly. In this case the tests are bundled as war files and executed
in a remote Wildfly instance (the HV dependencies are updated to the latest SNAPSHOT version):

    $ mvn clean test -Dincontainer

You can enforce the TCK via:

    $ mvn test -Dincontainer -Dtck.version=1.1.2.Final

You can find more information about the Bean Validation TCK [here](http://beanvalidation.org/1.1/).
