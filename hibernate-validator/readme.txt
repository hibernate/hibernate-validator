
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
  is not based JSR 303. This code can be accessed via 
  http://anonsvn.jboss.org/repos/hibernate/validator/trunk/hibernate-validator-legacy

  Status
  ------

  This is an beta release and even though it should be nearly feature complete there might be
  still some variances between the specification and this implementation.

  You can find more information about the current state of the implementation on the
  Bean Validation RI Roadmap - http://www.hibernate.org/459.html and there is copy of
  JSR as of the time of this release available here - http://in.relation.to/service/File/11150

  Documentation
  -------------

  The documentation available as of the date of this release is included in
  HTML and PDf format in the docs directory.

  Release Notes
  -------------

  The full list of changes can be found at 
  http://opensource.atlassian.com/projects/hibernate/secure/ConfigureReleaseNote.jspa?projectId=10060.

  System Requirements
  -------------------

  JDK 1.5 or above.

  Using Hibernate Validator
  -------------------------

  - Copy hibernate-validator-*.jar together will all jar files from lib into the 
    classpath of your application 

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

  Hibernate Validator URLS
  ------------------------

  Home Page:          http://validator.hibernate.org/
  Downloads:          http://www.hibernate.org/6.html
  Mailing Lists:      http://www.hibernate.org/20.html
  Source Code:        http://anonsvn.jboss.org/repos/hibernate/validator/trunk/
  Issue Tracking:     http://opensource.atlassian.com/projects/hibernate/browse/HV
