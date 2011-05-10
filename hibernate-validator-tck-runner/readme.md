# Hibernate Validator TCK Runner

This maven module allows to run the Bean Validation TCK against Hibernate Validator.
There are several ways of doing this. In any case you will need to define the *JBOSS_HOME*
environment variable referencing a AS 7 instance.

## Standalone

You can run the test as normal unit test using a JVM forked by the Surefire plugin. This is the default mode and
used when running

    $ mvn clean test

## In container

You can also run the TCK test against JBoss AS7 (>Beta3). In this case the tests are bundled as war files and executed
in a remote JBoss AS7 instance:

    $ mvn clean test -Dincontainer

In this mode the tests will run against the Hibernate Validator version bundled with AS 7 (currently 4.1.0.Final) which
will lead to test errors. You can place the latest Hibernate Validator instance into your AS 7 instance or you can
bundle the Hibernate Validator jar with the deployed war files:

    $ mvn test -Dincontainer -Dbundled-dependencies

Alternatively you can run against the TCK version is in sync with Hibernate Validator 4.1.0.Final:

    $ mvn test -Dincontainer -Djsr303.tck.version=1.0.4.GA

You can find more information about the Bean Validation TCK [here](http://docs.jboss.org/hibernate/stable/beanvalidation/tck/reference/html_single/).
