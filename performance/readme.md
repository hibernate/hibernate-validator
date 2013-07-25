# Performance test for Hibernate Validator

The tests are written as JUnit tests. They can be developed and executed as usual in your IDE. However, they are not
run as unit tests during a _mvn test_. Instead, to execute the performance tests, [Apache JMeter](http://jmeter.apache.org/)
and the [chronos-jmeter-maven-plugin](http://mojo.codehaus.org/chronos/chronos-jmeter-maven-plugin) are used.

## Profiles

To allow performance testing of different Hibernate Validator versions there are multiple profiles configured.
Choosing a profile executes the tests against the specified Hibernate Validator resp. BVal version. The defined profiles are:

* hv-5.1 (Hibernate Validator 5.1.0-SNAPSHOT)
* hv-5.0 (Hibernate Validator 5.0.1.Final)
* hv-4.3 (Hibernate Validator 4.3.0.Final)
* hv-4.2 (Hibernate Validator 4.2.0.Final)
* hv-4.1 (Hibernate Validator 4.1.0.Final)
* bval-0.5 (Apache BVAl 0.5)

## Executing the performance tests

Some tips before you start:

* **This tests are not there to measure absolute execution times! Instead there value lies in the ability to compare against different
versions of Hibernate Validator using the same tests. This allows to detect and address performance regressions.**
* When running the test for the first time (either via the GUI or the command line), the specified JMeter version is
downloaded and extracted into the performance module.
* For JMeter to execute JUnit tests they have to be copied to _\<jmeter_install_dir\>/lib/junit_. This happens automatically
  when _chronos-jmeter:jmetergui_ or _chronos-jmeter:jmeter_ is executed. After the execution the artifacts are removed.
* You need to specify a profile!

### Using the JMeter GUI

To run or configure a test via the GUI you run:

    > mvn chronos-jmeter:jmetergui -P hv-5.1 &

Then open a JMeter test file from _src/jmx_ and run the test.

### Using the maven build

The following command line will run all performance tests found under _src/jmx/_  (_*.jmx_ files):

    > mvn clean package chronos-jmeter:jmeter -P hv-5.1

To generate a report from the test execution run:

    > mvn chronos-jmeter:jmeteroutput chronos-report:report

After the completion of the report generation open:

    > open target/site/performancetest.html

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

    > for i in "hv-4.3" "hv-4.2" "hv-4.1" "hv-5.0" "hv-5.1"
    > do
    > mvn -P $i clean package chronos-jmeter:jmeter ; mvn chronos-jmeter:jmeteroutput chronos-report:report; open target/site/performancetest.html;
    > done

## Existing tests

At the moment the following tests are defined

### [SimpleValidationTest](https://github.com/hibernate/hibernate-validator/blob/master/performance/src/main/java/org/hibernate/validator/performance/simple/SimpleValidationTest.java)

A simple bean with a random number of failing constraints gets initalized and validated. The test is once executed with
a shared _ValidatorFactory_ and once the factory is recreated on each invocation.

### [CascadedValidationTest](https://github.com/hibernate/hibernate-validator/blob/master/performance/src/main/java/org/hibernate/validator/performance/cascaded/CascadedValidationTest.java)

Simple bean with cascaded validation which gets executed over and over.

### [StatisticalValidationTest](https://github.com/hibernate/hibernate-validator/blob/master/performance/src/main/java/org/hibernate/validator/performance/statistical/StatisticalValidationTest.java)

A number of _TestEntity_s is created where each entity contains a property for each built-in constraint type and also a reference
to another _TestEntity_. All constraints are evaluated by a single ConstraintValidator implementation which fails a specified
percentage of the validations.



