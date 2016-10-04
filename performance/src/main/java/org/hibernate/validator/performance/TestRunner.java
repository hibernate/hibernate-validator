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
import org.openjdk.jmh.profile.ClassloaderProfiler;
import org.openjdk.jmh.profile.CompilerProfiler;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.HotspotClassloadingProfiler;
import org.openjdk.jmh.profile.HotspotCompilationProfiler;
import org.openjdk.jmh.profile.HotspotMemoryProfiler;
import org.openjdk.jmh.profile.HotspotRuntimeProfiler;
import org.openjdk.jmh.profile.HotspotThreadProfiler;
import org.openjdk.jmh.profile.Profiler;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class containing main method to run all performance tests
 *
 * @author Marko Bekhta
 */
public final class TestRunner {

	private static final Map<String, Class<? extends Profiler>> PROFILERS;

	static {
		PROFILERS = new HashMap<>();
		PROFILERS.put( "STACK", StackProfiler.class );
		PROFILERS.put( "HS_THR", HotspotThreadProfiler.class );
		PROFILERS.put( "HS_RT", HotspotRuntimeProfiler.class );
		PROFILERS.put( "HS_GC", HotspotMemoryProfiler.class );
		PROFILERS.put( "HS_COMP", HotspotCompilationProfiler.class );
		PROFILERS.put( "HS_CL", HotspotClassloadingProfiler.class );
		PROFILERS.put( "GC", GCProfiler.class );
		PROFILERS.put( "COMP", CompilerProfiler.class );
		PROFILERS.put( "CL", ClassloaderProfiler.class );
	}

	private TestRunner() {
	}

	public static void main(String[] args) throws RunnerException {
		runTest( SimpleValidation.class.getSimpleName(), 10, 10, 100, 100, 1, args );
		runTest( CascadedValidation.class.getSimpleName(), 10, 10, 100, 100, 1, args );
		runTest( StatisticalValidation.class.getSimpleName(), 10, 5, 200, 10, 1, args );
	}

	/**
	 * @param className             simple class name of a class that contains benchmarks to run
	 * @param warmupIterations      number of warmup iterations
	 * @param warmupTime            warmup time in seconds
	 * @param measurementIterations number measurement iterations
	 * @param threads               number of threads
	 * @param forks                 numer of forks
	 * @param profilers             array of profiler keys
	 * @throws RunnerException if there's a problem running benchmarks
	 */
	public static void runTest(String className, int warmupIterations, int warmupTime,
							   int measurementIterations, int threads, int forks,
							   String[] profilers) throws RunnerException {
		ChainedOptionsBuilder builder =
				new OptionsBuilder()
						.include( className )
						.warmupIterations( warmupIterations )
						.warmupTime( TimeValue.seconds( warmupTime ) )
						.measurementIterations( measurementIterations )
						.threads( threads )
						.forks( forks )
						.resultFormat( ResultFormatType.JSON )
						.result( String.format( "%sJmhResult.json", className ) );
		Arrays.stream( profilers )
				.filter( profileKey -> PROFILERS.containsKey( profileKey ) )
				.forEach( profileKey -> builder.addProfiler( PROFILERS.get( profileKey ) ) );

		Options opt = builder.build();
		new Runner( opt ).run();
	}

}
