/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;

import org.hibernate.accessor.AccessorFactory;
import org.hibernate.accessor.Instantiator;
import org.hibernate.accessor.MultiValueReader;
import org.hibernate.accessor.MultiValueWriter;
import org.hibernate.accessor.ValueReader;
import org.hibernate.accessor.ValueWriter;
import org.hibernate.validator.BaseHibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.PredefinedScopeHibernateValidator;
import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.bean.BeanRetrieval;
import org.hibernate.validator.spi.bean.BeanProvider;

import com.acme.accessor.internal.TestAccessorFactory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AccessorFactoryConfigurationTest {

	@DataProvider
	public Object[][] scopes() {
		return new Object[][] { { false }, { true } };
	}

	@Test(dataProvider = "scopes")
	public void defaultFactoryIsAvailableThroughResolver(boolean predefinedScope) {
		try ( ValidatorFactory factory = configuration( predefinedScope ).buildValidatorFactory() ) {
			HibernateValidatorFactory hibernateFactory = factory.unwrap( HibernateValidatorFactory.class );
			assertThat( hibernateFactory.getBeanResolver().namedConfiguredForRole( AccessorFactory.class ) ).containsKey( "default" );
			try ( BeanHolder<AccessorFactory> defaultFactory = hibernateFactory.getBeanResolver()
					.resolve( AccessorFactory.class, "default", BeanRetrieval.BUILTIN ) ) {
				assertThat( defaultFactory.get().getClass() ).isEqualTo( hibernateFactory.getAccessorFactory().getClass() );
			}
			assertThat( factory.getValidator().validate( new Bean() ) ).hasSize( 1 );
		}
	}

	@Test(dataProvider = "scopes")
	public void explicitInstanceTakesPrecedenceOverProperty(boolean predefinedScope) {
		TrackingAccessorFactory accessorFactory = new TrackingAccessorFactory();
		try ( ValidatorFactory factory = configuration( predefinedScope )
				.accessorFactory( accessorFactory )
				.addProperty( HibernateValidatorConfiguration.ACCESSOR_FACTORY_CLASSNAME, "missing.AccessorFactory" )
				.buildValidatorFactory() ) {
			assertThat( factory.unwrap( HibernateValidatorFactory.class ).getAccessorFactory() ).isSameAs( accessorFactory );
			assertThat( factory.getValidator().validate( new Bean() ) ).hasSize( 1 );
			assertThat( accessorFactory.fieldReaders ).isPositive();
		}
	}

	@Test(dataProvider = "scopes")
	public void propertyUsesConstructorAndExternalClassLoader(boolean predefinedScope) {
		AtomicInteger classLoads = new AtomicInteger();
		ClassLoader externalClassLoader = new ClassLoader( getClass().getClassLoader() ) {
			@Override
			public Class<?> loadClass(String name) throws ClassNotFoundException {
				if ( name.equals( TestAccessorFactory.class.getName() ) ) {
					classLoads.incrementAndGet();
				}
				return super.loadClass( name );
			}
		};
		try ( ValidatorFactory factory = configuration( predefinedScope )
				.externalClassLoader( externalClassLoader )
				.beanProvider( new RejectingBeanProvider() )
				.addProperty( HibernateValidatorConfiguration.ACCESSOR_FACTORY_CLASSNAME, TestAccessorFactory.class.getName() )
				.buildValidatorFactory() ) {
			AccessorFactory accessorFactory = factory.unwrap( HibernateValidatorFactory.class ).getAccessorFactory();
			assertThat( accessorFactory ).isInstanceOf( TestAccessorFactory.class );
			assertThat( classLoads ).hasPositiveValue();
			assertThat( factory.getValidator().validate( new Bean() ) ).hasSize( 1 );
			assertThat( ( (TrackingAccessorFactory) accessorFactory ).fieldReaders ).isPositive();
		}
	}

	@Test(dataProvider = "scopes")
	public void invalidPropertyPreservesAccessorFactoryError(boolean predefinedScope) {
		assertThatThrownBy( () -> configuration( predefinedScope )
				.addProperty( HibernateValidatorConfiguration.ACCESSOR_FACTORY_CLASSNAME, "missing.AccessorFactory" )
				.buildValidatorFactory() )
				.isInstanceOf( ValidationException.class )
				.hasMessageStartingWith( "HV000277" );
	}

	private BaseHibernateValidatorConfiguration<?> configuration(boolean predefinedScope) {
		if ( predefinedScope ) {
			return Validation.byProvider( PredefinedScopeHibernateValidator.class ).configure()
					.ignoreXmlConfiguration()
					.builtinConstraints( Set.of( NotNull.class.getName() ) )
					.initializeBeanMetaData( Set.of( Bean.class ) );
		}
		return Validation.byProvider( HibernateValidator.class ).configure().ignoreXmlConfiguration();
	}

	private static class Bean {
		@NotNull
		private String name;
	}

	public static class TrackingAccessorFactory implements AccessorFactory {
		private final AccessorFactory delegate = AccessorFactory.reflection( MethodHandles.lookup() );
		private int fieldReaders;

		@Override
		public <T> Instantiator<T> instantiator(Constructor<T> constructor) {
			return delegate.instantiator( constructor );
		}

		@Override
		public ValueReader<?> valueReader(Field field) {
			fieldReaders++;
			return delegate.valueReader( field );
		}

		@Override
		public ValueReader<?> valueReader(Method method) {
			return delegate.valueReader( method );
		}

		@Override
		public ValueWriter valueWriter(Field field) {
			return delegate.valueWriter( field );
		}

		@Override
		public ValueWriter valueWriter(Method method) {
			return delegate.valueWriter( method );
		}

		@Override
		public MultiValueReader multiValueReader(Class<?> type, Member... members) {
			return delegate.multiValueReader( type, members );
		}

		@Override
		public MultiValueWriter multiValueWriter(Class<?> type, Member... members) {
			return delegate.multiValueWriter( type, members );
		}
	}

	private static class RejectingBeanProvider implements BeanProvider {
		@Override
		public <T> BeanHolder<T> forType(Class<T> typeReference) {
			throw new AssertionError( "Eager infrastructure must not query the bean provider" );
		}

		@Override
		public <T> BeanHolder<T> forTypeAndName(Class<T> typeReference, String nameReference) {
			throw new AssertionError( "Eager infrastructure must not query the bean provider" );
		}

		@Override
		public void close() {
		}
	}
}
