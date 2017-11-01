/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;

import javax.validation.ValidationException;

import org.hibernate.validator.cfg.AnnotationDef;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Marko Bekhta
 */
public final class CreateConstraintAnnotationDescriptor<A extends Annotation> implements PrivilegedAction<ConstraintAnnotationDescriptor<A>> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final MethodHandle CREATE_ANNOTATION_DESCRIPTOR_METHOD_HANDLE;

	static {
		try {
			Method createAnnotationDescriptorMethod = AnnotationDef.class.getDeclaredMethod( "createAnnotationDescriptor" );
			createAnnotationDescriptorMethod.setAccessible( true );
			CREATE_ANNOTATION_DESCRIPTOR_METHOD_HANDLE = MethodHandles.lookup().unreflect( createAnnotationDescriptorMethod );
			createAnnotationDescriptorMethod.setAccessible( false );
		}
		catch (NoSuchMethodException | IllegalAccessException e) {
			throw LOG.getUnableToFindAnnotationDefDeclaredMethods( e );
		}
	}

	private final ConstraintDef<?, A> constraintDef;

	public static <A extends Annotation> CreateConstraintAnnotationDescriptor<A> action(ConstraintDef<?, A> constraintDef) {
		return new CreateConstraintAnnotationDescriptor<>( constraintDef );
	}

	private CreateConstraintAnnotationDescriptor(ConstraintDef<?, A> constraintDef) {
		this.constraintDef = constraintDef;
	}

	@Override
	public ConstraintAnnotationDescriptor<A> run() {
		try {
			AnnotationDescriptor<A> annotationDescriptor = (AnnotationDescriptor<A>) CREATE_ANNOTATION_DESCRIPTOR_METHOD_HANDLE.invoke( constraintDef );
			return new ConstraintAnnotationDescriptor<>( annotationDescriptor );
		}
		catch (Throwable e) {
			if ( e instanceof ValidationException ) {
				throw (ValidationException) e;
			}
			throw LOG.getUnableToCreateAnnotationDescriptor( constraintDef.getClass(), e );
		}
	}
}
