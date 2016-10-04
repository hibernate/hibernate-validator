# Performance test for Hibernate Validator

The tests are written as JMH benchmarks. They can be developed and executed as usual in your IDE. However, they are not
executed during regular build as unit tests when a _mvn test_ is running. Instead, to execute the performance tests,
one should build a jar file with one of the profiles mentioned below and run it.

## Profiles

To allow performance testing of different Hibernate Validator versions there are multiple profiles configured.
Choosing a profile executes the tests against the specified Hibernate Validator resp. BVal version. The defined profiles are:

* hv-current (Hibernate Validator 6.0.0-SNAPSHOT)
* hv-5.3 (Hibernate Validator 5.3.0.CR1)
* hv-5.2 (Hibernate Validator 5.2.4.Final)
* hv-5.1 (Hibernate Validator 5.1.3.Final)
* hv-5.0 (Hibernate Validator 5.0.1.Final)
* hv-4.3 (Hibernate Validator 4.3.0.Final)
* hv-4.2 (Hibernate Validator 4.2.0.Final)
* hv-4.1 (Hibernate Validator 4.1.0.Final)
* bval-0.5 (Apache BVAl 0.5)

## Executing the performance tests

Some tips before you start:

* **These tests are not there to measure absolute execution times! Instead there value lies in the ability to compare against different
versions of Hibernate Validator using the same tests. This allows to detect and address performance regressions.**
* You need to specify a profile!

### Using the maven build

The following command line will run all performance tests listed in the main method of TestRunner class:

    > mvn clean package -P hv-current
    > java -jar target/hibernate-validator-performance.jar

It will generate a set of reports from the each test execution. Currently it'll be next three files:

    > SimpleValidationJmhResult.json
    > CascadedValidationJmhResult.json
    > StatisticalValidationJmhResult.json


#### Profiling

The pom contains an example on how you can run the tests while attaching a profiler. You will need to
adjust the property _profilingOptions_ used in the _chronos-jmeter-maven-plugin_ configuration.

For [JProfiler](http://www.ej-technologies.com/products/jprofiler/overview.html) on MacOS, the options look like similar to this:

    -agentpath:/Applications/Development/jprofiler7/bin/macos/libjprofilerti.jnilib=port=8849,nowait

There exists _jprofiler_ profile with the above options which you can enable via:

     mvn clean package chronos-jmeter:jmeter -P hv-5.1,jprofiler

Instead of editing the pom you can pass the profiling options via the command line:

    mvn clean package chronos-jmeter:jmeter -DprofilingOptions=<youroptions> -P hv-5.1

#### Creating reports for all major Validator versions

    > mkdir reports
    > for i in "hv-4.3" "hv-4.2" "hv-4.1" "hv-5.0" "hv-5.1" "hv-5.2" "hv-5.3" "hv-current"
    > do
    > mvn -P $i clean package ; java -jar target/hibernate-validator-performance.jar ;
    > cp target/SimpleValidationJmhResult.json reports/$iSimpleValidationJmhResult.json;
    > cp target/CascadedValidationJmhResult.json reports/$iCascadedValidationJmhResult.json;
    > cp target/StatisticalValidationJmhResult.json reports/$iStatisticalValidationJmhResult.json;
    > done

## Existing tests

At the moment the following benchmarks are defined:

### [SimpleValidation](https://github.com/hibernate/hibernate-validator/blob/master/performance/src/main/java/org/hibernate/validator/performance/simple/SimpleValidation.java)

A simple bean with a random number of failing constraints gets initialized and validated. The test is once executed with
a shared _ValidatorFactory_ and once the factory is recreated on each invocation.

### [CascadedValidation](https://github.com/hibernate/hibernate-validator/blob/master/performance/src/main/java/org/hibernate/validator/performance/cascaded/CascadedValidation.java)

Simple bean with cascaded validation which gets executed over and over.

### [StatisticalValidation](https://github.com/hibernate/hibernate-validator/blob/master/performance/src/main/java/org/hibernate/validator/performance/statistical/StatisticalValidation.java)

A number of _TestEntity_s is created where each entity contains a property for each built-in constraint type and also a reference
to another _TestEntity_. All constraints are evaluated by a single ConstraintValidator implementation which fails a specified
percentage of the validations.



