# Performance test for Hibernate Validator

The tests are written as [JMH](http://openjdk.java.net/projects/code-tools/jmh/) benchmarks. They can be developed and
executed as usual in your IDE. However, they are not executed during regular build as unit tests when a _mvn test_ is
running. Instead, to execute the performance tests, one should build a jar file with one of the profiles mentioned
below and run it.

## Profiles

To allow performance testing of different Hibernate Validator versions there are multiple profiles configured.
Choosing a profile executes the tests against the specified Hibernate Validator or BVal version, respectively. The
defined profiles are:

* hv-current (Hibernate Validator 6.1.0-SNAPSHOT)
* hv-6.0 (Hibernate Validator 6.0.15.Final)
* hv-5.4 (Hibernate Validator 5.4.3.Final)
* hv-5.3 (Hibernate Validator 5.3.4.Final)
* hv-5.2 (Hibernate Validator 5.2.4.Final)
* hv-5.1 (Hibernate Validator 5.1.3.Final)
* hv-5.0 (Hibernate Validator 5.0.1.Final)
* hv-4.3 (Hibernate Validator 4.3.0.Final)
* hv-4.2 (Hibernate Validator 4.2.0.Final)
* hv-4.1 (Hibernate Validator 4.1.0.Final)
* bval-1.1 (Apache BVal 1.1.2)

## Executing the performance tests

Some tips before you start:

* **These tests are not there to measure absolute execution times! Instead their value lies in the ability to compare against different
versions of Hibernate Validator using the same tests. This allows to detect and address performance regressions.**
* You need to specify a profile!

### Using the maven build

The following command line will run all performance tests listed in the main method of TestRunner class:

    mvn clean package -Dvalidator=hv-current
    java -jar target/hibernate-validator-performance-hv-current.jar

It will generate a set of reports from each test execution. Currently, all test results information are inside the following generated file:

    target/jmh-results.json

#### Profiling

List of available profilers:

| Profiler | Description |
| :--- | :--- |
| org.openjdk.jmh.profile.ClassloaderProfiler | Classloader profiling via standard MBeans |
| org.openjdk.jmh.profile.CompilerProfiler | JIT compiler profiling via standard MBeans |
| org.openjdk.jmh.profile.GCProfiler | GC profiling via standard MBeans |
| org.openjdk.jmh.profile.HotspotClassloadingProfiler | HotSpot ™ classloader profiling via implementation-specific MBeans |
| org.openjdk.jmh.profile.HotspotCompilationProfiler | HotSpot ™ JIT compiler profiling via implementation-specific MBeans |
| org.openjdk.jmh.profile.HotspotMemoryProfiler | HotSpot ™ memory manager (GC) profiling via implementation-specific MBeans |
| org.openjdk.jmh.profile.HotspotRuntimeProfiler | HotSpot ™ runtime profiling via implementation-specific MBeans |
| org.openjdk.jmh.profile.HotspotThreadProfiler | HotSpot ™ threading subsystem via implementation-specific MBeans |
| org.openjdk.jmh.profile.StackProfiler | Simple and naive Java stack profiler |

If you want to run one of those profilers - pass it as parameter when running a jar file. For example:

    java -jar target/hibernate-validator-performance-hv-current.jar -prof org.openjdk.jmh.profile.StackProfiler

To run a specific benchmark:

    java -jar target/hibernate-validator-performance.jar CascadedValidation

#### Creating reports for all major Hibernate Validator versions

    mkdir reports
    for impl in "bval-1.1.2" "hv-5.4" "hv-current"; do
        mvn -Dvalidator=${impl} package ; java -jar target/hibernate-validator-performance-${impl}.jar -rff reports/${impl}-jmh-results.json
    done

## Existing tests

At the moment the following benchmarks are defined:

### [SimpleValidation](https://github.com/hibernate/hibernate-validator/blob/master/performance/src/main/java/org/hibernate/validator/performance/simple/SimpleValidation.java)

A simple bean with a random number of failing constraints gets initialized and validated. The test is once executed with
a shared _ValidatorFactory_ and once the factory is recreated on each invocation.

### [CascadedValidation](https://github.com/hibernate/hibernate-validator/blob/master/performance/src/main/java/org/hibernate/validator/performance/cascaded/CascadedValidation.java)

Simple bean with cascaded validation which gets executed over and over.

### [CascadedWithLotsOfItemsValidation](https://github.com/hibernate/hibernate-validator/blob/master/performance/src/main/java/org/hibernate/validator/performance/cascaded/CascadedWithLotsOfItemsValidation.java)

Validation of a bean containing a lot of beans to cascade to.

### [CascadedWithLotsOfItemsAndMoreConstraintsValidation](https://github.com/hibernate/hibernate-validator/blob/master/performance/src/main/java/org/hibernate/validator/performance/cascaded/CascadedWithLotsOfItemsAndMoreConstraintsValidation.java)

This test has a few more constraints than the previous one, allowing to test our hypothesis in more realistic situation.

### [StatisticalValidation](https://github.com/hibernate/hibernate-validator/blob/master/performance/src/main/java/org/hibernate/validator/performance/statistical/StatisticalValidation.java)

A number of _TestEntity_s is created where each entity contains a property for each built-in constraint type and also a reference
to another _TestEntity_. All constraints are evaluated by a single ConstraintValidator implementation which fails a specified
percentage of the validations.
