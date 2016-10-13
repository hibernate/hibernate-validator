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

It will generate a set of reports from each test execution. Currently, all test results information is inside the following generated file:

    > JmhResults.json

#### Profiling

List of available profilers:

| Parameter Name | Description |
| :---: | :--- |
| CL | Classloader profiling via standard MBeans |
| COMP | JIT compiler profiling via standard MBeans |
| GC | GC profiling via standard MBeans |
| HS_CL | HotSpot ™ classloader profiling via implementation-specific MBeans |
| HS_COMP | HotSpot ™ JIT compiler profiling via implementation-specific MBeans |
| HS_GC | HotSpot ™ memory manager (GC) profiling via implementation-specific MBeans |
| HS_RT | HotSpot ™ runtime profiling via implementation-specific MBeans |
| HS_THR | HotSpot ™ threading subsystem via implementation-specific MBeans |
| STACK | Simple and naive Java stack profiler |

If you want to run one of those profilers - pass it as parameter when running a jar file. For example:

    > java -jar target/hibernate-validator-performance.jar STACK

#### Creating reports for all major Validator versions

    > mkdir reports
    > for i in "hv-4.3" "hv-4.2" "hv-4.1" "hv-5.0" "hv-5.1" "hv-5.2" "hv-5.3" "hv-current"
    > do
    > mvn -P $i clean package ; java -jar target/hibernate-validator-performance.jar ; cp target/JmhResults.json reports/$iJmhResults.json;
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



