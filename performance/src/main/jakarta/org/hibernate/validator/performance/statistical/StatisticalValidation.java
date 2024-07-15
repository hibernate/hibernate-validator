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
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import jakarta.validation.Configuration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
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
			try ( InputStream mappingStream = StatisticalValidation.class.getResourceAsStream( "mapping.xml" ) ) {
				configuration.addMapping( mappingStream );
				factory = configuration.buildValidatorFactory();
				assertThat( factory ).isNotNull();
			}
			catch (IOException e) {
				throw new IllegalStateException( "Mappings cannot be read. Validation factory cannot be configured correctly.", e );
			}

			validator = factory.getValidator();

			entitiesUnderTest = IntStream.rangeClosed( 0, NUMBER_OF_TEST_ENTITIES )
					.mapToObj( index -> new TestEntity( index % 10 ) )
					.collect( Collectors.toList() );
		}

	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Fork(value = 1)
	@Threads(100)
	@Warmup(iterations = 10)
	@Measurement(iterations = 10)
	public void testValidationWithStatisticalGraphDepthAndConstraintValidator(StatisticalValidationState state, Blackhole bh) throws Exception {
		state.entitiesUnderTest.forEach(
				testEntity -> {
					Set<ConstraintViolation<TestEntity>> violations = state.validator.validate( testEntity );
					assertThat( violations ).hasSize( StatisticalConstraintValidator.threadLocalCounter.get().getFailures() );
					bh.consume( violations );
					StatisticalConstraintValidator.threadLocalCounter.get().reset();
				}
		);
	}
}
