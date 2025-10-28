/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.traversableresolver;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.fail;

import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.Configuration;
import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

@TestForIssue(jiraKey = "HV-2151")
public class CachedTraversableResolverCacheHitsTest {

	@Test
	public void testHits() {
		TraversableResolver resolver = new TestResolver( Set.of( "string1", "string2", "bean1" ), Set.of( "bean1" ) );
		Configuration<?> config = Validation.byDefaultProvider()
				.configure()
				.traversableResolver( resolver );
		ValidatorFactory factory = config.buildValidatorFactory();

		MyBean bean = new MyBean();
		bean.bean1 = new MyInnerBean();
		bean.bean2 = new MyInnerBean();

		Validator v = factory.getValidator();
		try {
			assertThat( v.validate( bean ) ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith().property( "string1" ) ),
					violationOf( NotNull.class )
							.withPropertyPath( pathWith().property( "string2" ) ),
					violationOf( NotNull.class )
							.withPropertyPath( pathWith().property( "bean1" )
									.property( "string1" ) ),
					violationOf( NotNull.class )
							.withPropertyPath( pathWith().property( "bean1" )
									.property( "string2" ) )
			);
		}
		catch (ValidationException e) {
			fail( "TraversableResolver called several times for a given object", e );
		}
	}

	private static class MyBean {
		@NotNull
		String string1;
		@NotNull
		String string2;
		@NotNull
		String string3;
		@NotNull
		String string4;

		@Valid
		MyInnerBean bean1;
		@Valid
		MyInnerBean bean2;
	}

	private static class MyInnerBean {
		@NotNull
		String string1;
		@NotNull
		String string2;
	}

	private static class TestResolver implements TraversableResolver {
		private final Set<String> reachableProperties;
		private final Set<String> cascadableProperties;
		private Set<Holder> askedReach = new HashSet<Holder>();
		private Set<Holder> askedCascade = new HashSet<Holder>();

		private TestResolver(Set<String> reachableProperties, Set<String> cascadableProperties) {
			this.reachableProperties = reachableProperties;
			this.cascadableProperties = cascadableProperties;
		}

		private boolean isTraversable(Set<Holder> asked, Object traversableObject, Path.Node traversableProperty) {
			Holder h = new Holder( traversableObject, traversableProperty );
			if ( asked.contains( h ) ) {
				throw new IllegalStateException( "Called twice" );
			}
			asked.add( h );
			return true;
		}

		@Override
		public boolean isReachable(
				Object traversableObject,
				Path.Node traversableProperty,
				Class<?> rootBeanType,
				Path pathToTraversableObject,
				ElementType elementType
		) {
			if ( reachableProperties.contains( traversableProperty.getName() ) ) {
				return isTraversable(
						askedReach,
						traversableObject,
						traversableProperty
				);
			}
			return false;
		}

		@Override
		public boolean isCascadable(
				Object traversableObject,
				Path.Node traversableProperty,
				Class<?> rootBeanType,
				Path pathToTraversableObject,
				ElementType elementType
		) {
			if ( cascadableProperties.contains( traversableProperty.getName() ) ) {
				return isTraversable(
						askedCascade,
						traversableObject,
						traversableProperty
				);
			}
			return false;
		}

		public static class Holder {
			Object NULL = new Object();
			Object to;
			String tp;

			public Holder(Object traversableObject, Path.Node traversableProperty) {
				to = traversableObject == null ? NULL : traversableObject;
				tp = traversableProperty.getName();
			}

			@Override
			public int hashCode() {
				return to.hashCode() + tp.hashCode();
			}

			@Override
			public boolean equals(Object obj) {
				if ( !( obj instanceof Holder ) ) {
					return false;
				}
				Holder that = (Holder) obj;

				return to != NULL && to == that.to && tp.equals( that.tp );
			}
		}
	}
}
