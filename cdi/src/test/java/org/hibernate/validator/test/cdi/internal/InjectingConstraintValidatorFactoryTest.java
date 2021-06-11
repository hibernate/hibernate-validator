/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.fail;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Min;

import org.hibernate.validator.cdi.internal.InjectingConstraintValidatorFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class InjectingConstraintValidatorFactoryTest {
	private InjectingConstraintValidatorFactory constraintValidatorFactory;
	private BeanManager beanManagerMock;
	private AnnotatedType<MyValidator> annotatedTypeMock;
	private InjectionTarget<MyValidator> injectionTargetMock;
	private InjectionTargetFactory<MyValidator> injectionTargetFactoryMock;
	private CreationalContext<MyValidator> creationalContextMock;

	@BeforeClass
	@SuppressWarnings("unchecked")
	public void setUp() {
		beanManagerMock = createMock( BeanManager.class );
		constraintValidatorFactory = new InjectingConstraintValidatorFactory( beanManagerMock );
		annotatedTypeMock = createMock( AnnotatedType.class );
		injectionTargetMock = createMock( InjectionTarget.class );
		injectionTargetFactoryMock = createMock( InjectionTargetFactory.class );
		creationalContextMock = createMock( CreationalContext.class );
	}

	@Test
	public void testNullBeanManager() {
		try {
			new InjectingConstraintValidatorFactory( null );
			fail();
		}
		catch (IllegalArgumentException e) {
			// success
		}
	}

	@Test
	public void testCreateInstance() {
		// setup the mocks

		expect( beanManagerMock.createAnnotatedType( MyValidator.class ) ).andReturn( annotatedTypeMock );
		expect( beanManagerMock.getInjectionTargetFactory( annotatedTypeMock ) ).andReturn( injectionTargetFactoryMock );
		expect( injectionTargetFactoryMock.createInjectionTarget( null ) ).andReturn( injectionTargetMock );
		expect( (CreationalContext) beanManagerMock.createCreationalContext( null ) ).andReturn(
				creationalContextMock
		);

		MyValidator validator = new MyValidator();
		expect( injectionTargetMock.produce( creationalContextMock ) ).andReturn( validator );
		injectionTargetMock.inject( validator, creationalContextMock );
		injectionTargetMock.postConstruct( validator );

		injectionTargetMock.preDestroy( validator );
		injectionTargetMock.dispose( validator );

		// get the mocks into replay mode
		replay( beanManagerMock, annotatedTypeMock, injectionTargetFactoryMock, injectionTargetMock );

		// run the tests
		MyValidator validatorInstance = constraintValidatorFactory.getInstance( MyValidator.class );
		constraintValidatorFactory.releaseInstance( validatorInstance );

		// verify the mocks
		verify( beanManagerMock, annotatedTypeMock, injectionTargetMock );
	}

	public class MyValidator implements ConstraintValidator<Min, Object> {
		@Override
		public void initialize(Min constraintAnnotation) {
		}

		@Override
		public boolean isValid(Object value, ConstraintValidatorContext context) {
			return false;
		}
	}
}
