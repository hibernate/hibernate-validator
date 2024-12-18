/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.cascaded;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.PredefinedScopeHibernateValidator;
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
public class PredefinedScopeCascadedWithLotsOfItemsValidation {

	private static final int NUMBER_OF_ARTICLES_PER_SHOP = 2000;

	@State(Scope.Benchmark)
	public static class PredefinedScopeCascadedWithLotsOfItemsValidationState {

		public volatile Validator validator;

		public volatile Shop shop;

		public PredefinedScopeCascadedWithLotsOfItemsValidationState() {
			ValidatorFactory factory = Validation.byProvider( PredefinedScopeHibernateValidator.class )
					.configure()
					.builtinConstraints( Collections.singleton( NotNull.class.getName() ) )
					.initializeBeanMetaData( new HashSet<>( Arrays.asList( Shop.class, Article.class ) ) )
					.buildValidatorFactory();
			validator = factory.getValidator();

			shop = createShop();
		}

		private Shop createShop() {
			Shop shop = new Shop( 1 );

			for ( int i = 0; i < NUMBER_OF_ARTICLES_PER_SHOP; i++ ) {
				shop.addArticle( new Article( i ) );
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
	public void testPredefinedScopeCascadedValidationWithLotsOfItems(PredefinedScopeCascadedWithLotsOfItemsValidationState state, Blackhole bh) {
		Set<ConstraintViolation<Shop>> violations = state.validator.validate( state.shop );
		assertThat( violations ).hasSize( 0 );

		bh.consume( violations );
	}

	public static class Shop {

		@NotNull
		private Integer id;

		@NotNull
		@Valid
		private List<Article> articles = new ArrayList<>();

		public Shop(Integer id) {
			this.id = id;
		}

		public void addArticle(Article article) {
			articles.add( article );
		}
	}

	public static class Article {

		@NotNull
		private Integer id;

		public Article(Integer id) {
			this.id = id;
		}
	}
}
