# Performance test for Hibernate Validator

The test are written as JUnit test and executed via Apache JMeter and the
[chronos-jmeter-maven-plugin](http://mojo.codehaus.org/chronos/chronos-jmeter-maven-plugin).


## To start the JMeter GUI

    mvn chronos-jmeter:jmetergui -P hv-4.3 &

Then open a JMeter test file from _src/jmeter_
For JMeter to execute JUnit tests they have to be copied to <jmeter_install_dir>/lib/junit. This happens automatically
via _chronos-jmeter:jmetergui_ or _chronos-jmeter:jmeter_.

## To run the performance tests

    mvn clean package chronos-jmeter:jmeter -P hv-4.3

## To generate a report

    mvn chronos-jmeter:jmeteroutput chronos-report:report

## Profiles

There are currently two profiles defined in order to run the tests against different Hibernate Validator versions.
You specify the profile version via the _-P_ command line switch.
The available profiles are:

* hv-4.3
* hv-4.2