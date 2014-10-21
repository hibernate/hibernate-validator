/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.cdi;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Min;

import org.junit.Before;
import org.junit.Test;

import org.hibernate.validator.internal.cdi.InjectingConstraintValidatorFactory;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
public class InjectingConstraintValidatorFactoryTest {
	private InjectingConstraintValidatorFactory constraintValidatorFactory;
	private BeanManager beanManagerMock;
	private AnnotatedType<MyValidator> annotatedTypeMock;
	private InjectionTarget<MyValidator> injectionTargetMock;
	private CreationalContext<MyValidator> creationalContextMock;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		beanManagerMock = createMock( BeanManager.class );
		constraintValidatorFactory = new InjectingConstraintValidatorFactory( beanManagerMock );
		annotatedTypeMock = createMock( AnnotatedType.class );
		injectionTargetMock = createMock( InjectionTarget.class );
		creationalContextMock = createMock( CreationalContext.class );
	}

	@Test
	public void testNullBeanManager() {
		try {
			new InjectingConstraintValidatorFactory( null );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			// success
		}
	}

	@Test
	public void testCreateInstance() {
		// setup the mocks

		expect( beanManagerMock.createAnnotatedType( MyValidator.class ) ).andReturn( annotatedTypeMock );
		expect( beanManagerMock.createInjectionTarget( annotatedTypeMock ) ).andReturn( injectionTargetMock );
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
		replay( beanManagerMock, annotatedTypeMock, injectionTargetMock );

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
