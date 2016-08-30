# Hibernate Validator

*Version: 5.3.0.Alpha1, 15.01.2016*


## What is it?

This is the reference implementation of [JSR-380 - Bean Validation 2.0](http://beanvalidation.org/).
Bean Validation defines a metadata model and API for JavaBean as well as method validation.
The default metadata source are annotations, with the ability to override and extend
the meta-data through the use of XML validation descriptors.

## Documentation

The documentation for this release is included in the _docs_ directory of the distribution package or can be accessed [online](http://hibernate.org/validator/documentation/).

## Release Notes

The full list of changes for this release can be found in changelog.txt.

## System Requirements

JDK 1.8 or above.

## Using Hibernate Validator

* In case you use the distribution archive from the download site, copy _dist/hibernate-validator-&lt;version&gt;.jar_ together with all
jar files from _dist/lib/required_ into the classpath of your application. For the purposes of logging, Hibernate Validator uses
the JBoss Logging API, an abstraction layer which supports several logging solutions such (e.g. log4j or the logging framework
provided by the JDK) as implementation. Just add a supported logging library to the classpath (e.g. _log4j-&lt;version&gt;.jar_) and JBoss
Logging will delegate any log requests to that provider.

* Add the following to your Maven or Ivy dependency list

        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>5.3.0.Alpha1</version>
        </dependency>

      You also need an API and implementation of the Unified Expression Language. These dependencies must be explicitly added in an SE environment.
      In an EE environment they are often already provided.

        <dependency>
           <groupId>org.glassfish</groupId>
           <artifactId>javax.el</artifactId>
           <version>3.0.1-b08</version>
        </dependency>

* Bean Validation defines integration points with [CDI](http://jcp.org/en/jsr/detail?id=346). If your application runs
in an environment which does not provide this integration out of the box, you may use the Hibernate Validator CDI portable
extension by adding the following dependency:

        <dependency>
           <groupId>org.hibernate</groupId>
           <artifactId>hibernate-validator-cdi</artifactId>
           <version>5.3.0.Alpha1</version>
        </dependency>

* _hibernate-validator-annotation-processor-&lt;version&gt;.jar_ is an optional jar which can be integrated with your build
environment respectively IDE to verify that constraint annotations are correctly used. Refer to the [online
documentation](http://docs.jboss.org/hibernate/stable/validator/reference/en-US/html/validator-annotation-processor.html) for more information.

## Licensing

Hibernate Validator itself as well as the Bean Validation API and TCK are all provided and distributed under
the Apache Software License 2.0. Refer to license.txt for more information.

## Build from Source

You can build Hibernate Validator from source by cloning the git repository git://github.com/hibernate/hibernate-validator.git.
You will also need a JDK 8 and Maven 3 (>= 3.0.3). With these prerequisites in place you can compile the source via

    mvn -s settings-example.xml clean install

There are more build options available as well. For more information refer to [Contributing to Hibernate Validator](http://hibernate.org/validator/contribute/).

### Build on JDK 9

To build Hibernate Validator with JDK 9, export the following environment variable:

    export MAVEN_OPTS="-addmods java.annotations.common,java.xml.bind"

Then the build can be started like this:

    mvn -s settings-example.xml clean install -DdisableDocumentationBuild=true -DdisableDistributionBuild

The documentation and distribution modules are known to not work on Java 9 for the time being, hence they need to be excluded.
Also the integration tests on WildFly will fail on Java 9 currently, hence this "integration" module is excluded automatically when building on JDK 9.


## Continuous Integration

The official Continuous Integration service for the project is hosted on [ci.hibernate.org](http://ci.hibernate.org/view/Validator/).

We provide a `.travis.yml` file so that you can enable CI for your GitHub fork by enabling the build in [your Travis CI account](https://travis-ci.org/).

## Hibernate Validator URLs

* [Home Page](http://hibernate.org/validator/)
* [Bean Validation Home](http://beanvalidation.org/)
* [Downloads](http://hibernate.org/validator/downloads/)
* [Mailing Lists](http://hibernate.org/community/)
* [Issue Tracking](https://hibernate.atlassian.net/browse/HV)
* [Continuous Integration](http://ci.hibernate.org/view/Validator/) [![Build Status](http://ci.hibernate.org/buildStatus/icon?job=hibernate-validator-master)](http://ci.hibernate.org/view/Validator/job/hibernate-validator-master/)
