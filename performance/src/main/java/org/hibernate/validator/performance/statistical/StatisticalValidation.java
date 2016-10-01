/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.statistical;

import static org.assertj.core.api.Assertions.assertThat;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
			try (InputStream mappingStream = StatisticalValidation.class.getResourceAsStream( "mapping.xml" )) {
				configuration.addMapping( mappingStream );
				factory = configuration.buildValidatorFactory();
				assertThat( factory ).isNotNull();
			} catch (IOException e) {
				e.printStackTrace();
			}

			validator = factory.getValidator();

			entitiesUnderTest = IntStream.rangeClosed( 0, NUMBER_OF_TEST_ENTITIES )
					.mapToObj( index -> new TestEntity( index % 10 ) )
					.collect( Collectors.toList() );
		}

	}

	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void testValidationWithStatisticalGraphDepthAndConstraintValidator( StatisticalValidationState state ) throws Exception {
		state.entitiesUnderTest.forEach(
				testEntity -> {
					Set<ConstraintViolation<TestEntity>> violations = state.validator.validate( testEntity );
					assertThat( violations ).hasSize( StatisticalConstraintValidator.threadLocalCounter.get().getFailures() );
					StatisticalConstraintValidator.threadLocalCounter.get().reset();
				}
		);
	}
}



