
                          Hibernate Validator

  What is it?
  -----------

  This is a reference implementation of JSR 303 - Bean Validation. 
  Bean Validation defines a metadata model and API for JavaBean validation. 
  The default metadata source is annotations, with the ability to override and extend 
  the meta-data through the use of XML validation descriptors.

  History
  -------

  Prior to version 4.x Hibernate Validators was based on a different source base which 
  is not based JSR 303. This code can be accessed via 
  http://anonsvn.jboss.org/repos/hibernate/validator/trunk/hibernate-validator-legacy

  Documentation
  -------------

  The documentation available as of the date of this release is included in
  HTML format in the dist/docs/ directory.

  Release Notes
  -------------

  The full list of changes can be found at 
  http://opensource.atlassian.com/projects/hibernate/secure/ConfigureReleaseNote.jspa?projectId=10090.

  System Requirements
  -------------------

  JDK 1.5 or above.

  Using Hibernate Validator
  -------------------------

  - Copy dist/hibernate-validator-*.jar together will all jar files from dist/lib into the 
    classpath of your application 

  or 

  - Add the following to your maven or ivy dependency list:

    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>x.y.z</version>
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
