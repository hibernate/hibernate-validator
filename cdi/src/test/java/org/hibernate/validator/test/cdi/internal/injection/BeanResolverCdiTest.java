/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.injection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.bean.BeanResolver;
import org.hibernate.validator.bean.BeanRetrieval;
import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.cdi.internal.CdiBeanProvider;
import org.hibernate.validator.cdi.internal.DestructibleBeanInstance;
import org.hibernate.validator.constraints.PasswordStrength;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.testng.annotations.Test;

/**
 * Tests that {@link BeanResolver} correctly resolves CDI beans via {@code Instance.Handle},
 * including lazy resolution and proper lifecycle through {@link BeanHolder#close()}.
 */
public class BeanResolverCdiTest extends Arquillian {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addAsManifestResource( "beans.xml" );
	}

	@HibernateValidator
	@Inject
	ValidatorFactory validatorFactory;

	@Inject
	BeanManager beanManager;

	@Test
	public void testDependentHolderDestroysInjectedChildren() {
		int childrenBefore = DependentChild.destroyed.get();
		int parentsBefore = DependentParent.destroyed.get();
		try ( BeanHolder<DependentParent> holder = new CdiBeanProvider( beanManager ).forType( DependentParent.class ) ) {
			assertThat( holder.get().child ).isNotNull();
		}
		assertThat( DependentParent.destroyed.get() ).isEqualTo( parentsBefore + 1 );
		assertThat( DependentChild.destroyed.get() ).isEqualTo( childrenBefore + 1 );
	}

	@Test
	public void testUnmanagedInstanceDestroysInjectedChildren() {
		int childrenBefore = DependentChild.destroyed.get();
		int parentsBefore = DependentParent.destroyed.get();
		DestructibleBeanInstance<DependentParent> instance = new DestructibleBeanInstance<>( beanManager, DependentParent.class );
		assertThat( instance.getInstance().child ).isNotNull();
		instance.destroy();
		assertThat( DependentParent.destroyed.get() ).isEqualTo( parentsBefore + 1 );
		assertThat( DependentChild.destroyed.get() ).isEqualTo( childrenBefore + 1 );
	}

	@Test
	public void testExistingUnmanagedInstanceDestroysInjectedChildren() {
		int before = DependentChild.destroyed.get();
		DestructibleBeanInstance<DependentParent> instance = new DestructibleBeanInstance<>( beanManager, new DependentParent() );
		instance.destroy();
		assertThat( DependentChild.destroyed.get() ).isEqualTo( before + 1 );
	}

	@Test
	public void testAmbiguityDoesNotFallBackToConstruction() {
		try ( HibernateValidatorFactory factory = newFactory() ) {
			assertThatThrownBy( () -> factory.getBeanResolver().resolve( AmbiguousService.class, BeanRetrieval.ANY ) )
					.isInstanceOf( AmbiguousResolutionException.class );
			try ( BeanHolder<MissingService> holder = factory.getBeanResolver().resolve( MissingService.class, BeanRetrieval.ANY ) ) {
				assertThat( holder.get() ).isInstanceOf( MissingService.class );
			}
		}
	}

	@Test
	public void testValidatorFactoryReleasesValidatorResources() {
		int before = DependentChild.destroyed.get();
		try ( HibernateValidatorFactory factory = newFactory() ) {
			assertThat( factory.getValidator().validate( new ResourceBean() ) ).isEmpty();
			assertThat( DependentChild.destroyed.get() ).isEqualTo( before );
		}
		assertThat( DependentChild.destroyed.get() ).isEqualTo( before + 1 );
	}

	@Test
	public void testInitializationFailureReleasesValidatorResources() {
		int before = DependentChild.destroyed.get();
		try ( HibernateValidatorFactory factory = newFactory() ) {
			assertThatThrownBy( () -> factory.getValidator().validate( new FailingResourceBean() ) )
					.isInstanceOf( ValidationException.class );
			assertThat( DependentChild.destroyed.get() ).isEqualTo( before + 1 );
		}
		assertThat( DependentChild.destroyed.get() ).isEqualTo( before + 1 );
	}

	private HibernateValidatorFactory newFactory() {
		return Validation.byProvider( org.hibernate.validator.HibernateValidator.class ).configure()
				.beanProvider( new CdiBeanProvider( beanManager ) ).buildValidatorFactory().unwrap( HibernateValidatorFactory.class );
	}

	@Test
	public void testResolveCdiBean() {
		int destroyedBefore = MyApplicationScopedService.destroyed.get();
		BeanResolver beanResolver = validatorFactory.unwrap( HibernateValidatorFactory.class )
				.getBeanResolver();

		try ( BeanHolder<MyApplicationScopedService> holder = beanResolver.resolve(
				MyApplicationScopedService.class, BeanRetrieval.BEAN ) ) {
			MyApplicationScopedService service = holder.get();
			assertThat( service ).isNotNull();
			assertThat( service.greet() ).isEqualTo( "hello" );

			assertThat( holder.get() ).isSameAs( service );
		}
		assertThat( MyApplicationScopedService.destroyed.get() ).isEqualTo( destroyedBefore );
	}

	@Test
	public void testResolveDependentScopedBean() {
		BeanResolver beanResolver = validatorFactory.unwrap( HibernateValidatorFactory.class )
				.getBeanResolver();

		MyDependentService first;
		try ( BeanHolder<MyDependentService> holder = beanResolver.resolve(
				MyDependentService.class, BeanRetrieval.BEAN ) ) {
			first = holder.get();
			assertThat( first ).isNotNull();
		}

		try ( BeanHolder<MyDependentService> holder = beanResolver.resolve(
				MyDependentService.class, BeanRetrieval.BEAN ) ) {
			assertThat( holder.get() ).isNotSameAs( first );
		}
	}

	@Test
	public void testPasswordStrengthValidationWithCdiEstimator() {
		Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new PasswordBean( "weak" ) ) ).hasSize( 1 );
		assertThat( validator.validate( new PasswordBean( "strong-enough-password" ) ) ).isEmpty();
	}

	@ApplicationScoped
	public static class MyApplicationScopedService {
		static final AtomicInteger destroyed = new AtomicInteger();

		@PreDestroy
		void destroy() {
			destroyed.incrementAndGet();
		}

		public String greet() {
			return "hello";
		}
	}

	@Dependent
	public static class MyDependentService {
	}

	@ApplicationScoped
	public static class LengthBasedEstimator implements PasswordStrengthEstimator {
		@Override
		public PasswordStrengthResult estimate(char[] password) {
			int score = password.length >= 10 ? 4 : 1;
			return PasswordStrengthResult.simple( score, null );
		}
	}

	public static class PasswordBean {
		@PasswordStrength(min = 3)
		private final String password;

		PasswordBean(String password) {
			this.password = password;
		}
	}

	@Dependent
	public static class DependentChild {
		static final AtomicInteger destroyed = new AtomicInteger();

		@PreDestroy
		void destroy() {
			destroyed.incrementAndGet();
		}
	}

	@Dependent
	public static class DependentParent {
		static final AtomicInteger destroyed = new AtomicInteger();

		@Inject
		DependentChild child;

		@PreDestroy
		void destroy() {
			destroyed.incrementAndGet();
		}
	}

	@Vetoed
	public static class AmbiguousService {
	}

	@Dependent
	public static class FirstService extends AmbiguousService {
	}

	@Dependent
	public static class SecondService extends AmbiguousService {
	}

	@Vetoed
	public static class MissingService {
	}

	@Constraint(validatedBy = ResourceValidator.class)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ResourceConstraint {
		String message() default "invalid";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

		boolean fail() default false;
	}

	public static class ResourceValidator implements HibernateConstraintValidator<ResourceConstraint, String> {
		private BeanHolder<DependentChild> holder;

		@Override
		public void initialize(ConstraintDescriptor<ResourceConstraint> descriptor, HibernateConstraintValidatorInitializationContext context) {
			holder = context.getBeanResolver().resolve( DependentChild.class, BeanRetrieval.BEAN );
			holder.get();
			if ( descriptor.getAnnotation().fail() ) {
				throw new IllegalStateException( "initialization failed" );
			}
		}

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			return true;
		}

		@Override
		public void close() {
			holder.close();
		}
	}

	public static class ResourceBean {
		@ResourceConstraint
		String value = "value";
	}

	public static class FailingResourceBean {
		@ResourceConstraint(fail = true)
		String value = "value";
	}

}
