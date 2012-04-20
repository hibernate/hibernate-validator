# Performance test for Hibernate Validator

The tests are written as JUnit tests. They can be developed and executed as usual in your IDE. However, they are not
run as unit tests during a _mvn test_. Instead, to execute the performance tests [Apache JMeter](http://jmeter.apache.org/)
and the [chronos-jmeter-maven-plugin](http://mojo.codehaus.org/chronos/chronos-jmeter-maven-plugin) is used.

## Profiles

To allow performance testing of different Hibernate Validator versions there are multiple profiles configured.
Choosing a profile executes the tests against the specified Hibernate Validator version. The defined profiles are:

* hv-4.3 (Hibernate Validator 4.3.0-SNAPSHOT)
* hv-4.2 (Hibernate Validator 4.2.0.Final)
* hv-4.1 (Hibernate Validator 4.1.0.Final)

## Executing the performance tests

Some tips before you start:

* When running the test for the first time (either via the GUI or the command line), the specified JMeter version is
downloaded and extracted into the performance module.
* For JMeter to execute JUnit tests they have to be copied to _\<jmeter_install_dir\>/lib/junit_. This happens automatically
  when _chronos-jmeter:jmetergui_ or _chronos-jmeter:jmeter_ is executed. After the execution the artifacts are removed.
* You need to specify a profile!

### Using the JMeter GUI

To run or configure a test via the GUI you run:

    > mvn chronos-jmeter:jmetergui -P hv-4.3 &

Then open a JMeter test file from _src/jmx_ and run the test.

### Using the maven build

The following command line will run all performance tests found under _src/jmx/_  (_*.jmx_ files):

    > mvn clean package chronos-jmeter:jmeter -P hv-4.3

To generate a report from the test execution run:

    > mvn chronos-jmeter:jmeteroutput chronos-report:report

After the completion of the report generation open:

    > open target/site/performancetest.html

#### Profiling

The pom contains an example on how you can run the tests while attaching a profiler. You will need to uncomment and
adjust the property _profilingOptions_ and make sure it is used in the _chronos-report-maven-plugin_ configuration.

#### Creating reports for all major Validator versions

    > for i in "hv-4.3" "hv-4.2" "hv-4.1"
    > do
    > mvn -P $i clean package chronos-jmeter:jmeter ; mvn chronos-jmeter:jmeteroutput chronos-report:report; open target/site/performancetest.html;
    > done

## Existing tests (work in progress)

At the moment the following tests are defined

### Simple ValidationTest

A simple bean with a random number of failing constraints gets initalized and validated. The test is once executed with
a shared _ValidatorFactory_ and once the factory is recreated on each invocation.

### CascadedValidationTest

Simple bean with cascaded validation ...

### StatisticalValidationTest

A number of _TestEntity_s is created where each entity contains a property for each built-in constraint type and also a reference
to another _TestEntity_. All constraints are evaluated by a single ConstraintValidator implementation which fails a specified
percentage of the validations.



