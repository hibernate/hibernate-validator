/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.statistical;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 * @author Hardy Ferentschik
 */
public class StatisticalValidation {

	private static final int NUMBER_OF_TEST_ENTITIES = 100;

	@State(Scope.Benchmark)
	public static class StatisticalValidationState {

		private volatile Validator validator;

		private volatile List<TestEntity> entitiesUnderTest;

		public StatisticalValidationState() {
			ValidatorFactory factory = null;
			final Configuration<?> configuration = Validation.byDefaultProvider().configure();
			InputStream mappingStream = null;
			try {
				mappingStream = StatisticalValidation.class.getResourceAsStream( "mapping.xml" );
				configuration.addMapping( mappingStream );
				factory = configuration.buildValidatorFactory();
				assertThat( factory ).isNotNull();
			}
			catch (Exception e1) {
				e1.printStackTrace();
				if ( mappingStream != null ) {
					try {
						mappingStream.close();
					}
					catch (IOException e2) {
						e2.printStackTrace();
					}
				}
			}

			validator = factory.getValidator();

			entitiesUnderTest = new ArrayList<TestEntity>();
			for ( int i = 0; i < NUMBER_OF_TEST_ENTITIES; i++ ) {
				entitiesUnderTest.add( new TestEntity( i % 10 ) );
			}
		}

	}

	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Fork(value = 1)
	@Threads(100)
	@Warmup(iterations = 10)
	@Measurement(iterations = 10)
	public void testValidationWithStatisticalGraphDepthAndConstraintValidator(StatisticalValidationState state) throws Exception {
		for ( TestEntity testEntity : state.entitiesUnderTest ) {
			Set<ConstraintViolation<TestEntity>> violations = state.validator.validate( testEntity );
			assertThat( violations ).hasSize( StatisticalConstraintValidator.threadLocalCounter.get().getFailures() );
			StatisticalConstraintValidator.threadLocalCounter.get().reset();
		}
	}
}
