/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cfg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.BaseHibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.PredefinedScopeHibernateValidator;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;
import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProviderContext;
import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MappingMaterializationTest {

	@DataProvider
	public Object[][] scopes() {
		return new Object[][] { { false }, { true } };
	}

	@Test(dataProvider = "scopes")
	public void declarationsUseDestinationGetterStrategyAndNames(boolean predefinedScope) {
		AtomicInteger namingCalls = new AtomicInteger();
		BaseHibernateValidatorConfiguration<?> source = configuration( predefinedScope )
				.propertyNodeNameProvider( names( property -> {
					namingCalls.incrementAndGet();
					return property.getName();
				} ) );
		ConstraintMapping mapping = source.createConstraintMapping();
		mapping.type( Bean.class ).field( "values" ).ignoreAnnotations( true )
				.getter( "value" ).constraint( new NotNullDef() );
		assertThat( namingCalls ).hasValue( 0 );

		try ( ValidatorFactory first = configuration( predefinedScope ).addMapping( mapping )
				.getterPropertySelectionStrategy( new SelectedGetter( "first" ) )
				.propertyNodeNameProvider( names( property -> "firstPath" ) ).buildValidatorFactory();
				ValidatorFactory second = configuration( predefinedScope ).addMapping( mapping )
						.getterPropertySelectionStrategy( new SelectedGetter( "second" ) )
						.propertyNodeNameProvider( names( property -> "secondPath" ) ).buildValidatorFactory() ) {
			assertThat( first.getValidator().validate( new Bean() ) ).singleElement()
					.satisfies( violation -> assertThat( violation.getPropertyPath().toString() ).isEqualTo( "firstPath" ) );
			assertThat( second.getValidator().validate( new Bean() ) ).isEmpty();
			assertThat( first.getValidator().validate( new Bean() ) ).hasSize( 1 );
		}
		assertThat( namingCalls ).hasValue( 0 );
	}

	@Test(dataProvider = "scopes")
	public void getterFailuresAreDeferred(boolean predefinedScope) {
		BaseHibernateValidatorConfiguration<?> config = configuration( predefinedScope );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Bean.class ).getter( "missing" ).constraint( new NotNullDef() );
		assertThatThrownBy( () -> config.addMapping( mapping ).buildValidatorFactory() )
				.isInstanceOf( ValidationException.class ).hasMessageStartingWith( "HV000013" );
	}

	@Test(dataProvider = "scopes")
	public void getterContainerFailuresAreDeferred(boolean predefinedScope) {
		BaseHibernateValidatorConfiguration<?> config = configuration( predefinedScope );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Bean.class ).getter( "value" ).containerElementType().constraint( new NotNullDef() );
		assertThatThrownBy( () -> config.addMapping( mapping )
				.getterPropertySelectionStrategy( new SelectedGetter( "first" ) ).buildValidatorFactory() )
				.isInstanceOf( ValidationException.class ).hasMessageContaining( "parameterized" );
	}

	@Test(dataProvider = "scopes")
	public void getterMethodCollisionsAreDeferred(boolean predefinedScope) {
		BaseHibernateValidatorConfiguration<?> config = configuration( predefinedScope );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Bean.class ).getter( "value" ).constraint( new NotNullDef() )
				.method( "first" ).returnValue().constraint( new NotNullDef() );
		assertThatThrownBy( () -> config.addMapping( mapping )
				.getterPropertySelectionStrategy( new SelectedGetter( "first" ) ).buildValidatorFactory() )
				.isInstanceOf( ValidationException.class ).hasMessageStartingWith( "HV000173" );
	}

	@Test(dataProvider = "scopes")
	public void contributorsUseDestinationConfiguration(boolean predefinedScope) {
		try ( ValidatorFactory factory = configuration( predefinedScope )
				.addProperty( BaseHibernateValidatorConfiguration.CONSTRAINT_MAPPING_CONTRIBUTORS, Contributor.class.getName() )
				.getterPropertySelectionStrategy( new SelectedGetter( "first" ) )
				.buildValidatorFactory() ) {
			assertThat( factory.getValidator().validate( new Bean() ) ).hasSize( 1 );
		}
	}

	@Test
	public void structuralErrorsRemainImmediate() {
		TypeConstraintMappingContext<Bean> type = configuration( false ).createConstraintMapping().type( Bean.class );
		assertThatThrownBy( () -> type.field( "missing" ) ).isInstanceOf( ValidationException.class );
		assertThatThrownBy( () -> type.method( "missing" ) ).isInstanceOf( ValidationException.class );
		assertThatThrownBy( () -> type.constructor( String.class ) ).isInstanceOf( ValidationException.class );
		assertThatThrownBy( () -> type.method( "first" ).parameter( 0 ) ).isInstanceOf( IllegalArgumentException.class );
		assertThatThrownBy( () -> type.field( "values" ).containerElementType() )
				.isInstanceOf( ValidationException.class ).hasMessageContaining( "index" );
	}

	@Test(dataProvider = "scopes")
	public void signatureAndContainerPathsAreCopied(boolean predefinedScope) {
		BaseHibernateValidatorConfiguration<?> config = configuration( predefinedScope );
		ConstraintMapping mapping = config.createConstraintMapping();
		Class<?>[] signature = { List.class };
		int[] nested = { 0 };
		mapping.type( Bean.class ).method( "accept", signature ).parameter( 0 )
				.containerElementType( 0, nested ).constraint( new NotNullDef() );
		signature[0] = String.class;
		nested[0] = 9;
		try ( ValidatorFactory factory = config.addMapping( mapping ).buildValidatorFactory() ) {
			assertThat( factory.getValidator().getConstraintsForClass( Bean.class )
					.getConstraintsForMethod( "accept", List.class ).getParameterDescriptors().get( 0 ).hasConstraints() ).isFalse();
			try {
				assertThat( factory.getValidator().forExecutables().validateParameters( new Bean(),
						Bean.class.getMethod( "accept", List.class ), new Object[] { List.of( java.util.Arrays.asList( (String) null ) ) } ) ).hasSize( 1 );
			}
			catch (NoSuchMethodException e) {
				throw new AssertionError( e );
			}
		}
	}

	@Test(dataProvider = "scopes")
	public void laterDeclarationsDoNotChangeAnExistingFactory(boolean predefinedScope) {
		ConstraintMapping mapping = configuration( predefinedScope ).createConstraintMapping();
		PropertyConstraintMappingContext property = mapping.type( Bean.class ).field( "values" )
				.constraint( new NotNullDef().message( "first" ) );
		try ( ValidatorFactory first = configuration( predefinedScope ).addMapping( mapping ).buildValidatorFactory() ) {
			property.constraint( new NotNullDef().message( "second" ) );
			try ( ValidatorFactory second = configuration( predefinedScope ).addMapping( mapping ).buildValidatorFactory() ) {
				// Delay validation until both factories exist, including for the regular factory's lazy metadata provider.
				assertThat( first.getValidator().validate( new Bean() ) ).extracting( violation -> violation.getMessage() )
						.containsExactly( "first" );
				assertThat( second.getValidator().validate( new Bean() ) ).extracting( violation -> violation.getMessage() )
						.containsExactlyInAnyOrder( "first", "second" );
			}
		}
	}

	@Test(dataProvider = "scopes")
	public void laterDefinitionMutationsDoNotChangeAnExistingFactory(boolean predefinedScope) {
		ConstraintMapping mapping = configuration( predefinedScope ).createConstraintMapping();
		NotNullDef definition = new NotNullDef().message( "first" );
		mapping.type( Bean.class ).field( "values" ).constraint( definition );
		try ( ValidatorFactory factory = configuration( predefinedScope ).addMapping( mapping ).buildValidatorFactory() ) {
			definition.message( "mutated" );
			assertThat( factory.getValidator().validate( new Bean() ) ).extracting( violation -> violation.getMessage() )
					.containsExactly( "first" );
		}
	}

	@Test(dataProvider = "scopes")
	public void implicitGetterContainerSelectionIsDistinctFromExplicitZero(boolean predefinedScope) {
		BaseHibernateValidatorConfiguration<?> config = configuration( predefinedScope );
		ConstraintMapping implicit = config.createConstraintMapping();
		implicit.type( Bean.class ).getter( "values" ).containerElementType().constraint( new NotNullDef() );
		assertThatThrownBy( () -> config.addMapping( implicit ).buildValidatorFactory() )
				.isInstanceOf( ValidationException.class ).hasMessageStartingWith( "HV000213" );

		BaseHibernateValidatorConfiguration<?> explicitConfig = configuration( predefinedScope );
		ConstraintMapping explicit = explicitConfig.createConstraintMapping();
		explicit.type( Bean.class ).getter( "values" ).containerElementType( 0 ).constraint( new NotNullDef() );
		Bean bean = new Bean();
		bean.values = new HashMap<>();
		bean.values.put( null, "value" );
		try ( ValidatorFactory factory = explicitConfig.addMapping( explicit ).buildValidatorFactory() ) {
			assertThat( factory.getValidator().validate( bean ) ).hasSize( 1 );
		}
	}

	@Test(dataProvider = "scopes")
	public void duplicateGetterContainerElementPathReportsTheContainerType(boolean predefinedScope) {
		BaseHibernateValidatorConfiguration<?> config = configuration( predefinedScope );
		ConstraintMapping mapping = config.createConstraintMapping();
		PropertyConstraintMappingContext property = mapping.type( Bean.class ).getter( "values" );
		property.containerElementType( 0 ).constraint( new NotNullDef() );
		property.containerElementType( 0 ).constraint( new NotNullDef() );

		// The getter type is only known once the destination factory has resolved it, so the error names the
		// container type rather than the bean it is declared on.
		assertThatThrownBy( () -> config.addMapping( mapping ).buildValidatorFactory() )
				.isInstanceOf( ValidationException.class )
				.hasMessageStartingWith( "HV000214" )
				.hasMessageContaining( "java.util.Map<java.lang.String, java.lang.String>" );
	}

	private PropertyNodeNameProvider names(
			Function<Property, String> function) {
		return new PropertyNodeNameProvider() {
			@Override
			public String getName(Property property,
					PropertyNodeNameProviderContext context) {
				return function.apply( property );
			}
		};
	}

	private BaseHibernateValidatorConfiguration<?> configuration(boolean predefinedScope) {
		if ( predefinedScope ) {
			return Validation.byProvider( PredefinedScopeHibernateValidator.class ).configure().ignoreXmlConfiguration()
					.builtinConstraints( Set.of( NotNull.class.getName() ) ).initializeBeanMetaData( Set.of( Bean.class ) );
		}
		return Validation.byProvider( HibernateValidator.class ).configure().ignoreXmlConfiguration();
	}

	public static class Contributor implements ConstraintMappingContributor {
		@Override
		public void createConstraintMappings(ConstraintMappingBuilder builder) {
			builder.addConstraintMapping().type( Bean.class ).getter( "value" ).constraint( new NotNullDef() );
		}
	}

	private static class SelectedGetter implements GetterPropertySelectionStrategy {
		private final String method;

		private SelectedGetter(String method) {
			this.method = method;
		}

		@Override
		public Optional<String> getProperty(ConstrainableExecutable executable) {
			return executable.getName().equals( method ) ? Optional.of( "value" ) : Optional.empty();
		}

		@Override
		public List<String> getGetterMethodNameCandidates(String propertyName) {
			return List.of( method );
		}
	}

	public static class Bean {
		private Map<String, String> values;

		public Map<String, String> getValues() {
			return values;
		}

		public String first() {
			return null;
		}

		public String second() {
			return "present";
		}

		public void accept(List<List<String>> values) {
		}
	}
}
