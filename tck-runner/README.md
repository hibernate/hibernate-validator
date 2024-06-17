# Hibernate Validator TCK Runner

This maven module allows to run the Bean Validation TCK against Hibernate Validator.
There are several ways of doing this:

## Standalone

You can run the test as normal unit test using a JVM forked by the Surefire plugin. This is the default mode and
used when running

    $ mvn clean test

## In container

You can also run the TCK test against WildFly. In this case the tests are bundled as war files and executed
in a remote WildFly instance (the HV dependencies are updated to the latest SNAPSHOT version):

    $ mvn clean test -Dincontainer -Dincontainer-prepared

You can enforce the TCK version via:

    $ mvn clean test -Dincontainer -Dincontainer-prepared -Dtck.version=<tck version>

You can find more information about the Bean Validation TCK [here](http://beanvalidation.org/tck/).

## In container provided

Sometimes you want to test the TCK against a provided WildFly container.
The approach is exactly the same as for `incontainer` testing except we do not prepare a WildFly for you
and you have to provide one yourself.

Note that in this case the JavaFX tests are not run as they require some changes to WildFly to work.

You can run the TCK tests with:

    $ mvn clean test -Dincontainer -Dincontainer-provided -Dwildfly.target-dir=<your WildFly installation>

You can enforce the TCK version via:

    $ mvn clean test -Dincontainer -Dincontainer-provided -Dwildfly.target-dir=<your WildFly installation> -Dtck.version=<tck version>
