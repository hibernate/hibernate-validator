/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance;

import java.util.HashMap;
import java.util.Map;

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

/**
 * Class containing main method to run all performance tests.
 *
 * @author Marko Bekhta
 */
public final class TestRunner {

	private static final Map<String, Class<? extends Profiler>> PROFILERS;

	static {
		PROFILERS = new HashMap<String, Class<? extends Profiler>>();
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
		runTest(
				args,
				SimpleValidation.class.getSimpleName(),
				CascadedValidation.class.getSimpleName(),
				StatisticalValidation.class.getSimpleName()
		);
	}

	/**
	 * @param classNames simple class name of a class that contains benchmarks to run
	 * @param profilers array of profiler keys
	 *
	 * @throws RunnerException if there's a problem running benchmarks
	 */
	public static void runTest(String[] profilers, String... classNames) throws RunnerException {
		ChainedOptionsBuilder builder =
				new OptionsBuilder()
						.resultFormat( ResultFormatType.JSON )
						.result( "target/JmhResults.json" );
		for ( String profilerKey : profilers ) {
			if ( PROFILERS.containsKey( profilerKey ) ) {
				builder.addProfiler( PROFILERS.get( profilerKey ) );
			}
		}
		for ( String className : classNames ) {
			builder.include( className );
		}

		Options opt = builder.build();
		new Runner( opt ).run();
	}

}
