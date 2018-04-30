/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.validation.ClockProvider;
import javax.validation.ElementKind;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateCrossParameterConstraintValidatorContext;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Marko Bekhta
 */
public class CrossParameterConstraintValidatorContext extends ConstraintValidatorContextImpl implements HibernateCrossParameterConstraintValidatorContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final List<String> methodParameterNames;

	public CrossParameterConstraintValidatorContext(List<String> methodParameterNames, ClockProvider clockProvider, PathImpl propertyPath, ConstraintDescriptor<?> constraintDescriptor, Object constraintValidatorPayload) {
		super( clockProvider, propertyPath, constraintDescriptor, constraintValidatorPayload );
		Contracts.assertTrue( propertyPath.getLeafNode().getKind() == ElementKind.CROSS_PARAMETER, "Context can only be used for corss parameter validation" );
		this.methodParameterNames = methodParameterNames;
	}

	@Override
	public final ConstraintViolationBuilder buildConstraintViolationWithTemplate(String messageTemplate) {
		return new CrossParameterConstraintViolationBuilderImpl(
				methodParameterNames,
				messageTemplate,
				getCopyOfBasePath()
		);
	}

	@Override
	public List<String> getMethodParameterNames() {
		return methodParameterNames;
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		//allow unwrapping into public super types
		if ( type.isAssignableFrom( HibernateCrossParameterConstraintValidatorContext.class ) ) {
			return type.cast( this );
		}
		return super.unwrap( type );
	}

	private class CrossParameterConstraintViolationBuilderImpl extends ConstraintViolationBuilderImpl {

		private final List<String> methodParameterNames;

		private CrossParameterConstraintViolationBuilderImpl(List<String> methodParameterNames, String template, PathImpl path) {
			super( template, path );
			this.methodParameterNames = methodParameterNames;
		}

		@Override
		public NodeBuilderDefinedContext addParameterNode(int index) {
			if ( propertyPath.getLeafNode().getKind() != ElementKind.CROSS_PARAMETER ) {
				throw LOG.getParameterNodeAddedForNonCrossParameterConstraintException( propertyPath );
			}

			dropLeafNodeIfRequired();
			propertyPath.addParameterNode( methodParameterNames.get( index ), index );

			return new NodeBuilder( messageTemplate, propertyPath );
		}

	}
}
