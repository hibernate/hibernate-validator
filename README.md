# Hibernate Validator

*Version: 5.1.3.Final, 22.10.2014*


## What is it?

This is the reference implementation of [JSR-349 - Bean Validation 1.1](http://www.beanvalidation.org/).
Bean Validation defines a metadata model and API for JavaBean as well as method validation.
The default metadata source are annotations, with the ability to override and extend
the meta-data through the use of XML validation descriptors.

## Documentation

The documentation for this release is included in the _docs_ directory of the distribution package or can be accessed [online](http://www.hibernate.org/subprojects/validator/docs.html).

## Release Notes

The full list of changes for this release can be found in changelog.txt.

## System Requirements

JDK 1.6 or above.

## Using Hibernate Validator

* In case you use the distribution archive from the download site, copy _dist/hibernate-validator-&lt;version&gt;.jar_ together with all
jar files from _dist/lib/required_ into the classpath of your application. For the purposes of logging, Hibernate Validator uses
the JBoss Logging API, an abstraction layer which supports several logging solutions such (e.g. log4j or the logging framework
provided by the JDK) as implementation. Just add a supported logging library to the classpath (e.g. _log4j-&lt;version&gt;.jar_) and JBoss
Logging will delegate any log requests to that provider.

* Add the following to your Maven or Ivy dependency list (Hibernate Validator can be found in the [JBoss Maven repository](http://repository.jboss.org/nexus/content/groups/public-jboss)):

        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>5.1.3.Final</version>
        </dependency>

      You also need an API and implementation of the Unified Expression Language. These dependencies must be explicitly added in an SE environment.
      In an EE environment they are often already provided.

        <dependency>
           <groupId>javax.el</groupId>
           <artifactId>javax.el-api</artifactId>
           <version>2.2.4</version>
        </dependency>
        <dependency>
           <groupId>org.glassfish.web</groupId>
           <artifactId>javax.el</artifactId>
           <version>2.2.4</version>
        </dependency>

* Bean Validation defines integration points with [CDI](http://jcp.org/en/jsr/detail?id=346). If your application runs
in an environment which does not provide this integration out of the box, you may use the Hibernate Validator CDI portable
extension by adding the following dependency:

        <dependency>
           <groupId>org.hibernate</groupId>
           <artifactId>hibernate-validator-cdi</artifactId>
           <version>5.1.3.Final</version>
        </dependency>

* _hibernate-validator-annotation-processor-&lt;version&gt;.jar_ is an optional jar which can be integrated with your build
environment respectively IDE to verify that constraint annotations are correctly used. Refer to the [online
documentation](http://docs.jboss.org/hibernate/stable/validator/reference/en-US/html/validator-annotation-processor.html) for more information.

## Licensing

Hibernate Validator itself as well as the Bean Validation API and TCK are all provided and distributed under
the Apache Software License 2.0. Refer to license.txt for more information.

## Build from Source

You can build Hibernate Validator from source by cloning the git repository git://github.com/hibernate/hibernate-validator.git.
You will also need a JDK 7 and Maven 3 (>= 3.0.3). With these prerequisites in place you can compile the source via

    mvn clean install -s settings-example.xml

The documentation module requires an additional tool called po2xml. If you don't have po2xml installed you can
skip the building of the documentation via:

    mvn clean install -DdisableDocumentationBuild=true -s settings-example.xml

There are more build options available as well. For more information refer to [Contributing to Hibernate Validator](http://community.jboss.org/wiki/ContributingtoHibernateValidator).

## Hibernate Validator URLs

* [Home Page](http://validator.hibernate.org)
* [Bean Validation Home](http://beanvalidation.org)
* [Downloads](http://www.hibernate.org/subprojects/validator/download.html)
* [Mailing Lists](http://www.hibernate.org/community/mailinglists.html)
* [Source Code](git://github.com/hibernate/hibernate-validator.git)
* [Issue Tracking](http://opensource.atlassian.com/projects/hibernate/browse/HV)
* [Continuous Integration](https://hibernate-validator.ci.cloudbees.com/) [![Build Status](https://hibernate-validator.ci.cloudbees.com/buildStatus/icon?job=HV-5-MASTER)](https://hibernate-validator.ci.cloudbees.com/view/Hibernate%20Validator%205/job/HV-5-MASTER/)
