/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.cascaded;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

/**
 * @author Guillaume Smet
 */
public class CascadedWithLotsOfItemsAndMoreConstraintsValidation {

	private static final int NUMBER_OF_ARTICLES_PER_SHOP = 2000;

	@State(Scope.Benchmark)
	public static class CascadedWithLotsOfItemsValidationState {
		public volatile Validator validator;

		public volatile Shop shop;

		public CascadedWithLotsOfItemsValidationState() {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
			validator = factory.getValidator();

			shop = createShop();
		}

		private Shop createShop() {
			Shop shop = new Shop( 1, "Shop" );

			for ( int i = 0; i < NUMBER_OF_ARTICLES_PER_SHOP; i++ ) {
				shop.addArticle( new Article( i, "Article " + i ) );
			}

			return shop;
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@OutputTimeUnit(TimeUnit.SECONDS)
	@Fork(value = 1)
	@Threads(20)
	@Warmup(iterations = 10)
	@Measurement(iterations = 20)
	public void testCascadedValidationWithLotsOfItems(CascadedWithLotsOfItemsValidationState state, Blackhole bh) {
		Set<ConstraintViolation<Shop>> violations = state.validator.validate( state.shop );
		assertThat( violations ).hasSize( 0 );

		bh.consume( violations );
	}

	public static class Shop {
		@NotNull
		private Integer id;

		@Size(min = 1)
		private String name;

		@NotNull
		@Valid
		private List<Article> articles = new ArrayList<>();

		public Shop(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		public void addArticle(Article article) {
			articles.add( article );
		}
	}

	public static class Article {
		@NotNull
		private Integer id;

		@Size(min = 1)
		private String name;

		public Article(Integer id, String name) {
			this.id = id;
			this.name = name;
		}
	}
}
