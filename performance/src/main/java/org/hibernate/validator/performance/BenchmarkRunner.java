/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance;

import java.util.Arrays;
import java.util.List;

import org.hibernate.validator.performance.cascaded.CascadedValidation;
import org.hibernate.validator.performance.simple.SimpleValidation;
import org.hibernate.validator.performance.statistical.StatisticalValidation;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.CommandLineOptionException;
import org.openjdk.jmh.runner.options.CommandLineOptions;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Class containing main method to run all performance tests.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public final class BenchmarkRunner {

	private static final List<Class<?>> DEFAULT_TEST_CLASSES = Arrays.asList(
			SimpleValidation.class,
			CascadedValidation.class,
			StatisticalValidation.class
	);

	private BenchmarkRunner() {
	}

	public static void main(String[] args) throws RunnerException, CommandLineOptionException {
		Options commandLineOptions = new CommandLineOptions( args );
		ChainedOptionsBuilder builder = new OptionsBuilder().parent( commandLineOptions );

		if ( !commandLineOptions.getResult().hasValue() ) {
			builder.result( "target/jmh-results.json" );
		}
		if ( !commandLineOptions.getResultFormat().hasValue() ) {
			builder.resultFormat( ResultFormatType.JSON );
		}
		if ( commandLineOptions.getIncludes().isEmpty() ) {
			DEFAULT_TEST_CLASSES.stream().forEach( testClass -> builder.include( testClass.getName() ) );
		}

		Options opt = builder.build();
		new Runner( opt ).run();
	}

}
