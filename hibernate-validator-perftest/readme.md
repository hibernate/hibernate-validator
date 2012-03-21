# Performance test for Hibernate Validator

The test are written as JUnit test and executed via Apache JMeter and the
[chronos-jmeter-maven-plugin](http://mojo.codehaus.org/chronos/chronos-jmeter-maven-plugin).


## To start the JMeter GUI

    mvn chronos-jmeter:jmetergui

Then open the JMeter test file _src/main/resources/hv.jmx_

## To run the performance tests

    mvn chronos-jmeter:jmeter

## To generate a report

    mvn chronos-jmeter:jmeteroutput
    mvn site