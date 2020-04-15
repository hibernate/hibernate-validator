/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.core;

import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.getDummyConstraintValidatorInitializationContext;
import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Collections;

import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManagerImpl;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraints;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.properties.javabean.JavaBeanGetter;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class MetaConstraintTest {
	private ConstraintHelper constraintHelper;
	private TypeResolutionHelper typeResolutionHelper;
	private ValueExtractorManager valueExtractorManager;
	private ConstraintValidatorManager constraintValidatorManager;
	private Method barMethod;
	private ConstraintAnnotationDescriptor<NotNull> constraintAnnotationDescriptor;

	@BeforeClass
	public void setUp() throws Exception {
		constraintHelper = ConstraintHelper.forAllBuiltinConstraints();
		typeResolutionHelper = new TypeResolutionHelper();
		valueExtractorManager = new ValueExtractorManager( Collections.emptySet() );
		constraintValidatorManager = new ConstraintValidatorManagerImpl( new ConstraintValidatorFactoryImpl(), getDummyConstraintValidatorInitializationContext() );
		barMethod = Foo.class.getMethod( "getBar" );
		constraintAnnotationDescriptor = new ConstraintAnnotationDescriptor.Builder<>( barMethod.getAnnotation( NotNull.class ) ).build();
	}

	@Test
	@TestForIssue(jiraKey = "HV-930")
	public void two_meta_constraints_for_the_same_constraint_should_be_equal() throws Exception {
		JavaBeanGetter javaBeanGetter = new JavaBeanGetter( Foo.class, Foo.class.getDeclaredMethod( "getBar" ), "bar", "bar" );
		ConstraintDescriptorImpl<NotNull> constraintDescriptor1 = new ConstraintDescriptorImpl<>(
				constraintHelper, javaBeanGetter, constraintAnnotationDescriptor, ConstraintLocationKind.METHOD
		);
		ConstraintLocation location1 = ConstraintLocation.forClass( Foo.class );
		MetaConstraint<NotNull> metaConstraint1 = MetaConstraints.create( typeResolutionHelper, valueExtractorManager, constraintValidatorManager,
				constraintDescriptor1, location1 );

		ConstraintDescriptorImpl<NotNull> constraintDescriptor2 = new ConstraintDescriptorImpl<>(
				constraintHelper, javaBeanGetter, constraintAnnotationDescriptor, ConstraintLocationKind.METHOD
		);
		ConstraintLocation location2 = ConstraintLocation.forClass( Foo.class );
		MetaConstraint<NotNull> metaConstraint2 = MetaConstraints.create( typeResolutionHelper, valueExtractorManager, constraintValidatorManager,
				constraintDescriptor2, location2 );

		assertEquals(
				metaConstraint1, metaConstraint2, "Two MetaConstraint instances for the same constraint should be equal"
		);
	}

	public static class Foo {
		@NotNull
		public String getBar() {
			return null;
		}
	}
}


