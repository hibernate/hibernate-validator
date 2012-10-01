# Hibernate Validator TCK Runner

This maven module allows to run the Bean Validation TCK against Hibernate Validator.
There are several ways of doing this:

## Standalone

You can run the test as normal unit test using a JVM forked by the Surefire plugin. This is the default mode and
used when running

    $ mvn clean test

## In container

You can also run the TCK test against JBoss AS7. In this case the tests are bundled as war files and executed
in a remote JBoss AS7 instance:

    $ mvn clean test -Dincontainer

In this mode the tests will run against the Hibernate Validator version bundled with AS 7 (currently 4.2.0.Final. See also _modules/org/hibernate/validator/main_ in the AS 7 installation).

You can also bundle the Hibernate Validator jar with the deployed war files:

    $ mvn test -Dincontainer -Dbundled-dependencies

You can enforce the TCK via:

    $ mvn test -Dincontainer -Djsr303.tck.version=1.0.4.GA

You can find more information about the Bean Validation TCK [here](http://docs.jboss.org/hibernate/stable/beanvalidation/tck/reference/html_single/).
