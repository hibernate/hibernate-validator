/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance;

import org.hibernate.validator.performance.cascaded.CascadedValidation;
import org.hibernate.validator.performance.simple.SimpleValidation;
import org.hibernate.validator.performance.statistical.StatisticalValidation;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Class containing main method to run all performance tests
 *
 * @author Marko Bekhta
 */
public final class TestRunner {

	private TestRunner() {
	}

	public static void main( String[] args ) throws RunnerException {
		runTest( SimpleValidation.class.getSimpleName(), 10, 10, 100, 100, 1 );
		runTest( CascadedValidation.class.getSimpleName(), 10, 10, 100, 100, 1 );
		runTest( StatisticalValidation.class.getSimpleName(), 10, 5, 200, 10, 1 );
	}

	/**
	 * @param className             simple class name of a class that contains benchmarks to run
	 * @param warmupIterations      number of warmup iterations
	 * @param warmupTime            warmup time in seconds
	 * @param measurementIterations number measurement iterations
	 * @param threads               number of threads
	 * @param forks                 numer of forks
	 * @throws RunnerException if there's a problem running benchmarks
	 */
	public static void runTest( String className, int warmupIterations, int warmupTime,
								int measurementIterations, int threads, int forks ) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include( className )
				.warmupIterations( warmupIterations )
				.warmupTime( TimeValue.seconds( warmupTime ) )
				.measurementIterations( measurementIterations )
				.threads( threads )
				.forks( forks )
				.resultFormat( ResultFormatType.JSON )
				.result( String.format( "%sJmhResult.json", className ) )
				.build();

		new Runner( opt ).run();
	}

}
