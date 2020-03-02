/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.traversableresolver;

import static org.testng.Assert.fail;

import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.Configuration;
import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.groups.Default;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidationXmlTestHelper;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Emmanuel Bernard
 * @author Guillaume Smet
 */
public class CachedTraversableResolverTest {

	private static ValidationXmlTestHelper validationXmlTestHelper;

	@BeforeClass
	public static void setupValidationXmlTestHelper() {
		validationXmlTestHelper = new ValidationXmlTestHelper( CachedTraversableResolverTest.class );
	}

	@Test
	public void testCache() {
		TraversableResolver resolver = new AskOnceTR();
		Configuration<?> config = Validation.byDefaultProvider()
				.configure()
				.traversableResolver( resolver );
		ValidatorFactory factory = config.buildValidatorFactory();

		Suit suit = createSuit();

		Validator v = factory.getValidator();
		try {
			v.validate( suit, Default.class, Cloth.class );
		}
		catch (ValidationException e) {
			fail( "TraversableResolver called several times for a given object", e );
		}

		v = factory.usingContext().traversableResolver( new AskOnceTR() ).getValidator();
		try {
			v.validateProperty( suit, "size", Default.class, Cloth.class );
		}
		catch (ValidationException e) {
			fail( "TraversableResolver called several times for a given object", e );
		}

		v = factory.usingContext().traversableResolver( new AskOnceTR() ).getValidator();
		try {
			v.validateValue( Suit.class, "size", 2, Default.class, Cloth.class );
		}
		catch (ValidationException e) {
			fail( "TraversableResolver called several times for a given object", e );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1487")
	public void testCacheDisabled() {
		Configuration<?> config = Validation.byProvider( HibernateValidator.class )
				.configure()
				.traversableResolver( new AskOnceTR() )
				.enableTraversableResolverResultCache( false );
		ValidatorFactory factory = config.buildValidatorFactory();

		Suit suit = createSuit();

		// Cache disabled at the factory level
		Validator v = factory.getValidator();
		try {
			v.validate( suit, Default.class, Cloth.class );
			fail( "TraversableResolver calls are apparently cached and shouldn't be" );
		}
		catch (ValidationException e) {
		}

		// Cache disabled at the factory level but enabled in the context
		v = ((HibernateValidatorContext) factory.usingContext())
				.traversableResolver( new AskOnceTR() )
				.enableTraversableResolverResultCache( true )
				.getValidator();
		try {
			v.validate( suit, Default.class, Cloth.class );
		}
		catch (ValidationException e) {
			fail( "TraversableResolver called several times for a given object", e );
		}

		// Cache enabled at the factory level
		config = Validation.byProvider( HibernateValidator.class )
				.configure()
				.traversableResolver( new AskOnceTR() );
		factory = config.buildValidatorFactory();

		v = factory.getValidator();
		try {
			v.validate( suit, Default.class, Cloth.class );
		}
		catch (ValidationException e) {
			fail( "TraversableResolver called several times for a given object", e );
		}

		// Cache enabled at the factory level but disabled in the context
		v = ((HibernateValidatorContext) factory.usingContext())
				.traversableResolver( new AskOnceTR() )
				.enableTraversableResolverResultCache( false )
				.getValidator();

		v = factory.getValidator();
		try {
			v.validate( suit, Default.class, Cloth.class );
			fail( "TraversableResolver calls are apparently cached and shouldn't be" );
		}
		catch (ValidationException e) {
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1487")
	public void testCacheDisabledInXmlConfiguration() {
		validationXmlTestHelper.runWithCustomValidationXml(
				"validation-CachedTraversableResolverTest.xml", new Runnable() {

					@Override
					public void run() {
						Validator v = ValidatorUtil.getConfiguration()
								.traversableResolver( new AskOnceTR() )
								.buildValidatorFactory()
								.getValidator();

						Suit suit = createSuit();

						try {
							v.validate( suit, Default.class, Cloth.class );
							fail( "TraversableResolver calls are apparently cached and shouldn't be" );
						}
						catch (ValidationException e) {
						}
					}
				} );
	}

	private Suit createSuit() {
		Suit suit = new Suit();
		suit.setTrousers( new Trousers() );
		suit.setJacket( new Jacket() );
		suit.setSize( 3333 );
		suit.getTrousers().setLength( 32321 );
		suit.getJacket().setWidth( 432432 );

		return suit;
	}

	private static class AskOnceTR implements TraversableResolver {
		private Set<Holder> askedReach = new HashSet<Holder>();
		private Set<Holder> askedCascade = new HashSet<Holder>();

		private boolean isTraversable(Set<Holder> asked, Object traversableObject, Path.Node traversableProperty) {
			Holder h = new Holder( traversableObject, traversableProperty );
			if ( asked.contains( h ) ) {
				throw new IllegalStateException( "Called twice" );
			}
			asked.add( h );
			return true;
		}

		@Override
		public boolean isReachable(Object traversableObject,
								   Path.Node traversableProperty,
								   Class<?> rootBeanType,
								   Path pathToTraversableObject,
								   ElementType elementType) {
			return isTraversable(
					askedReach,
					traversableObject,
					traversableProperty
			);
		}

		@Override
		public boolean isCascadable(Object traversableObject,
									Path.Node traversableProperty,
									Class<?> rootBeanType,
									Path pathToTraversableObject,
									ElementType elementType) {
			return isTraversable(
					askedCascade,
					traversableObject,
					traversableProperty
			);
		}

		public static class Holder {
			Object NULL = new Object();
			Object to;
			Path.Node tp;

			public Holder(Object traversableObject, Path.Node traversableProperty) {
				to = traversableObject == null ? NULL : traversableObject;
				tp = traversableProperty;
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
