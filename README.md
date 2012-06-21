# Hibernate Validator

*Version: 5.0.0.Alpha1, 21.06.2012*


## What is it?

This is the reference implementation of JSR-349 - Bean Validation 1.1.
Bean Validation defines a metadata model and API for JavaBean validation. 
The default metadata source is annotations, with the ability to override and extend 
the meta-data through the use of XML validation descriptors.

## Documentation

The documentation for this release is included in the docs directory of distribution package or can be accessed [online](  http://www.hibernate.org/subprojects/validator/docs.html).

## Release Notes

The full list of changes for this release can be found in changelog.txt.

## System Requirements

JDK 1.6 or above.

## Using Hibernate Validator

* In case you use the distribution archive from the download site, copy dist/hibernate-validator-<version>.jar together with all
jar files from dist/lib/required into the classpath of your application. For the purposes of logging, Hibernate Validator uses
the JBoss Logging API, an abstraction layer which supports several logging solutions such (e.g. log4j or the logging framework
provided by the JDK) as implementation. Just add a supported logging library to the classpath (e.g. log4j-<version>.jar) and JBoss
Logging will delegate any log requests to that provider.

* Add the following to your maven or ivy dependency list (Hibernate Validator can be found in the [JBoss Maven repository](http://repository.jboss.org/nexus/content/groups/public-jboss)):

        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>5.0.0.Alpha1</version>
        </dependency>


*hibernate-validator-annotation-processor-<version>.jar* is an optional jar which can be integrated with your build
environment respectively IDE to verify that constraint annotations are correctly used. Refer to the online
documentation for more information.

## Licensing

Hibernate Validator itself as well as the Bean Validation API and TCK are all provided and distributed under
the Apache Software License 2.0. Refer to license.txt for more information.

## Build from Source

You can build Hibernate Validator from source by cloning the git repository git://github.com/hibernate/hibernate-validator.git
You will also need a JDK 6 or 7 and a Maven 3. With these prerequisites in place you can compile the source via

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
