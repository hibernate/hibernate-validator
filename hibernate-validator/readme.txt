
                          Hibernate Validator

  What is it?
  -----------

  This is the reference implementation of JSR 303 - Bean Validation. 
  Bean Validation defines a metadata model and API for JavaBean validation. 
  The default metadata source is annotations, with the ability to override and extend 
  the meta-data through the use of XML validation descriptors.

  History
  -------

  Prior to version 4.x Hibernate Validators was based on a different source base which 
  is not based on JSR 303. This code can be accessed via 
  http://anonsvn.jboss.org/repos/hibernate/validator/trunk/hibernate-validator-legacy

  Status
  ------

  This is the first GA release of Hibernate Validator 4.

  Documentation
  -------------

  The documentation for this release is included in the docs directory of distribution package
  or online under https://www.hibernate.org/5.html

  Release Notes
  -------------

  The full list of changes can be found at
  http://opensource.atlassian.com/projects/hibernate/secure/ReleaseNote.jspa?projectId=10060&version=10982

  System Requirements
  -------------------

  JDK 1.5 or above.

  Using Hibernate Validator
  -------------------------

  - Copy hibernate-validator-*.jar together will all jar files from lib into the 
    classpath of your application. In case you are running on JDK5 you have to also include
    all the jar files from the jdk5 subdirectory.

  or 

  - Add the following to your maven or ivy dependency list:

    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>${project.version}</version>
    </dependency>

    Hibernate Validator can be found in this repository: http://repository.jboss.com/maven2/  

  Licensing
  ---------

  Please see the file called license.txt

  Hibernate Validator URLs
  ------------------------

  Home Page:          http://validator.hibernate.org/
  Migration Guide:    http://www.hibernate.org/468.html
  Downloads:          http://www.hibernate.org/6.html
  Mailing Lists:      http://www.hibernate.org/20.html
  Source Code:        http://anonsvn.jboss.org/repos/hibernate/validator/trunk/
  Issue Tracking:     http://opensource.atlassian.com/projects/hibernate/browse/HV
