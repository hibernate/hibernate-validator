/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.validation.ValidationException;

import org.hibernate.validator.cfg.AnnotationDef;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethodHandle;

/**
 * @author Marko Bekhta
 */
public class MetaConstraintBuilder<A extends Annotation> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final MethodHandle CREATE_ANNOTATION_DESCRIPTOR_METHOD_HANDLE =
			run( GetDeclaredMethodHandle.andMakeAccessible( MethodHandles.lookup(), AnnotationDef.class, "createAnnotationDescriptor" ) );

	private final ConstraintAnnotationDescriptor<A> annotationDescriptor;

	public MetaConstraintBuilder(ConstraintAnnotationDescriptor<A> annotationDescriptor) {
		this.annotationDescriptor = annotationDescriptor;
	}

	public MetaConstraintBuilder(ConstraintDef<?, A> constraintDef) {
		this( createAnnotationDescriptor( constraintDef ) );
	}

	public MetaConstraint<A> build(
			TypeResolutionHelper typeResolutionHelper,
			ConstraintHelper constraintHelper,
			ValueExtractorManager valueExtractorManager,
			ConstraintLocation constraintLocation) {
		return MetaConstraints.create(
				typeResolutionHelper,
				valueExtractorManager,
				new ConstraintDescriptorImpl<>(
						constraintHelper,
						constraintLocation.getConstrainable(),
						annotationDescriptor,
						constraintLocation.getKind()
				),
				constraintLocation );
	}

	private static <A extends Annotation> ConstraintAnnotationDescriptor<A> createAnnotationDescriptor(ConstraintDef<?, A> constraint) {
		try {
			@SuppressWarnings("unchecked")
			AnnotationDescriptor<A> annotationDescriptor = (AnnotationDescriptor<A>) CREATE_ANNOTATION_DESCRIPTOR_METHOD_HANDLE.invoke( constraint );
			return new ConstraintAnnotationDescriptor<>( annotationDescriptor );
		}
		catch (Throwable e) {
			if ( e instanceof ValidationException ) {
				throw (ValidationException) e;
			}
			throw LOG.getUnableToCreateAnnotationDescriptor( constraint.getClass(), e );
		}
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <V> V run(PrivilegedAction<V> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
