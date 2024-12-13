# Hibernate Validator

*Version: 9.0.0.CR1 - 2024-12-13*

[![Maven Central](https://img.shields.io/maven-central/v/org.hibernate.validator/hibernate-validator.svg?label=Maven%20Central&style=for-the-badge)](https://central.sonatype.com/search?namespace=org.hibernate.validator&sort=name)
[![Build Status](https://img.shields.io/jenkins/build?jobUrl=https://ci.hibernate.org/view/Validator/job/hibernate-validator/job/main/&style=for-the-badge)](https://ci.hibernate.org/view/Validator/job/hibernate-validator/job/main/)
[![Sonar Coverage](https://img.shields.io/sonar/coverage/hibernate_hibernate-validator?server=https%3A%2F%2Fsonarcloud.io&style=for-the-badge)](https://sonarcloud.io/project/activity?id=hibernate_hibernate-validator&graph=coverage)
[![Quality gate](https://img.shields.io/sonar/alert_status/hibernate_hibernate-validator?logo=sonarcloud&server=https%3A%2F%2Fsonarcloud.io&style=for-the-badge)](https://sonarcloud.io/dashboard?id=hibernate_hibernate-validator)
[![Develocity](https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?style=for-the-badge&logo=gradle)](https://ge.hibernate.org/scans?search.rootProjectNames=Hibernate%20Validator)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/org/hibernate/validator/hibernate-validator/badge.json&style=for-the-badge)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/org/hibernate/validator/hibernate-validator/README.md)

## What is it?

This is the reference implementation of [Jakarta Validation 3.1](https://jakarta.ee/specifications/bean-validation/3.1/).
Jakarta Validation defines a metadata model and API for JavaBean as well as method validation.
The default metadata source are annotations, with the ability to override and extend
the metadata through the use of XML validation descriptors.

## Documentation

The documentation for this release is included in the _docs_ directory of the distribution package or can be accessed [online](https://hibernate.org/validator/documentation/).

## Release Notes

The full list of changes for this release can be found in changelog.txt.

## System Requirements

JDK 17 or above.

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
            <version>9.0.0.CR1</version>
        </dependency>

  You also need an API and implementation of the Unified Expression Language. These dependencies must be explicitly added in an SE environment.
  In a Jakarta EE environment, they are often already provided.

        <dependency>
           <groupId>org.glassfish.expressly</groupId>
           <artifactId>expressly</artifactId>
           <version>6.0.0-M1</version>
        </dependency>

* Jakarta Validation defines integration points with [CDI](http://jcp.org/en/jsr/detail?id=346). If your application runs
in an environment which does not provide this integration out of the box, you may use the Hibernate Validator CDI portable
extension by adding the following dependency:

        <dependency>
           <groupId>org.hibernate.validator</groupId>
           <artifactId>hibernate-validator-cdi</artifactId>
           <version>9.0.0.CR1</version>
        </dependency>

* _hibernate-validator-annotation-processor-&lt;version&gt;.jar_ is an optional jar which can be integrated with your build
environment respectively IDE to verify that constraint annotations are correctly used. Refer to the [online
documentation](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#validator-annotation-processor) for more information.

## Licensing

Hibernate Validator itself as well as the Jakarta Validation API and TCK are all provided and distributed under
the Apache Software License 2.0. Refer to license.txt for more information.

## Build from Source

You can build Hibernate Validator from source by cloning the git repository `git://github.com/hibernate/hibernate-validator.git`.
You will also need a JDK 17+ and Maven 3 (>= 3.9.8). With these prerequisites in place you can compile the source via:

    mvn clean install

There are more build options available as well. For more information refer to [Contributing to Hibernate Validator](https://hibernate.org/validator/contribute/).

## Continuous Integration

The official Continuous Integration service for the project is hosted on [ci.hibernate.org](http://ci.hibernate.org/view/Validator/).

We also include a [GitHub action build file](.github/workflows/build.yml) that can be used by those interested in
running builds on their own forks. 
This build runs on Linux and Windows and executes the TCK both in standalone and in container [modes](tck-runner/README.md). 

## Hibernate Validator URLs

* [Home Page](https://hibernate.org/validator/)
* [Jakarta Validation Home](https://beanvalidation.org/)
* [Jakarta Validation Specs](https://jakarta.ee/specifications/bean-validation/)
* [Downloads](https://hibernate.org/validator/downloads/)
* [Mailing Lists](https://hibernate.org/community/)
* [Issue Tracking](https://hibernate.atlassian.net/browse/HV)
* [Continuous Integration](http://ci.hibernate.org/view/Validator/)
