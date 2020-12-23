# Hibernate Validator

*Version: 7.0.0.Final - 23-12-2020*


## What is it?

This is the reference implementation of [Jakarta Bean Validation 3.0](http://beanvalidation.org/).
Jakarta Bean Validation defines a metadata model and API for JavaBean as well as method validation.
The default metadata source are annotations, with the ability to override and extend
the metadata through the use of XML validation descriptors.

## Documentation

The documentation for this release is included in the _docs_ directory of the distribution package or can be accessed [online](http://hibernate.org/validator/documentation/).

## Release Notes

The full list of changes for this release can be found in changelog.txt.

## System Requirements

JDK 8 or above.

## Using Hibernate Validator

* In case you use the distribution archive from the download site, copy _dist/hibernate-validator-&lt;version&gt;.jar_ together with all
jar files from _dist/lib/required_ into the classpath of your application. For the purposes of logging, Hibernate Validator uses
the JBoss Logging API, an abstraction layer which supports several logging solutions such (e.g. log4j or the logging framework
provided by the JDK) as implementation. Just add a supported logging library to the classpath (e.g. _log4j-&lt;version&gt;.jar_) and JBoss
Logging will delegate any log requests to that provider.

* Add the following artifact to your Maven/Ivy/Gradle dependency list:

        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>7.0.0.Final</version>
        </dependency>

  You also need an API and implementation of the Unified Expression Language. These dependencies must be explicitly added in an SE environment.
  In a Jakarta EE environment, they are often already provided.

        <dependency>
           <groupId>org.glassfish</groupId>
           <artifactId>jakarta.el</artifactId>
           <version>4.0.0-RC2</version>
        </dependency>

* Jakarta Bean Validation defines integration points with [CDI](http://jcp.org/en/jsr/detail?id=346). If your application runs
in an environment which does not provide this integration out of the box, you may use the Hibernate Validator CDI portable
extension by adding the following dependency:

        <dependency>
           <groupId>org.hibernate.validator</groupId>
           <artifactId>hibernate-validator-cdi</artifactId>
           <version>7.0.0.Final</version>
        </dependency>

* _hibernate-validator-annotation-processor-&lt;version&gt;.jar_ is an optional jar which can be integrated with your build
environment respectively IDE to verify that constraint annotations are correctly used. Refer to the [online
documentation](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#validator-annotation-processor) for more information.

## Licensing

Hibernate Validator itself as well as the Jakarta Bean Validation API and TCK are all provided and distributed under
the Apache Software License 2.0. Refer to license.txt for more information.

## Build from Source

You can build Hibernate Validator from source by cloning the git repository `git://github.com/hibernate/hibernate-validator.git`.
You will also need a JDK 8+ and Maven 3 (>= 3.3.1). With these prerequisites in place you can compile the source via:

    mvn clean install

There are more build options available as well. For more information refer to [Contributing to Hibernate Validator](http://hibernate.org/validator/contribute/).

## Continuous Integration

The official Continuous Integration service for the project is hosted on [ci.hibernate.org](http://ci.hibernate.org/view/Validator/).

We provide a `.travis.yml` file so that you can enable CI for your GitHub fork by enabling the build in [your Travis CI account](https://travis-ci.org/).

## Hibernate Validator URLs

* [Home Page](http://hibernate.org/validator/)
* [Jakarta Bean Validation Home](http://beanvalidation.org/)
* [Downloads](http://hibernate.org/validator/downloads/)
* [Mailing Lists](http://hibernate.org/community/)
* [Issue Tracking](https://hibernate.atlassian.net/browse/HV)
* [Continuous Integration](http://ci.hibernate.org/view/Validator/) | [![Build Status](http://ci.hibernate.org/view/Validator/job/hibernate-validator-master/badge/icon)](http://ci.hibernate.org/view/Validator/job/hibernate-validator-master/)
