/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.ValidationContext;
import org.hibernate.validator.internal.engine.ValueContext;
import org.hibernate.validator.internal.engine.cascading.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintTree;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.StringHelper;

/**
 * Instances of this class abstract the constraint type  (class, method or field constraint) and give access to
 * meta data about the constraint. This allows a unified handling of constraints in the validator implementation.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class MetaConstraint<A extends Annotation> {

	/**
	 * The constraint tree created from the constraint annotation.
	 */
	private final ConstraintTree<A> constraintTree;

	/**
	 * The constraint descriptor.
	 */
	private final ConstraintDescriptorImpl<A> constraintDescriptor;

	/**
	 * The location at which this constraint is defined.
	 */
	private final ConstraintLocation location;

	/**
	 * The sequence of {@link ValueExtractor}s used to navigate from the outermost container to the innermost container
	 * and extract the value for validation.
	 */
	private final List<ValueExtractorDescriptor> valueExtractorDescriptors;

	/**
	 * @param constraintDescriptor The constraint descriptor for this constraint
	 * @param location meta data about constraint placement
	 * @param valueExtractorDescriptors the potential {@link ValueExtractor}s used to extract the value to validate
	 * @param validatedValueType the type of the validated element
	 */
	MetaConstraint(ConstraintDescriptorImpl<A> constraintDescriptor, ConstraintLocation location, List<ValueExtractorDescriptor> valueExtractorDescriptors,
			Type validatedValueType) {
		this.constraintTree = new ConstraintTree<>( constraintDescriptor, validatedValueType );
		this.constraintDescriptor = constraintDescriptor;
		this.location = location;
		this.valueExtractorDescriptors = CollectionHelper.toImmutableList( valueExtractorDescriptors );
	}

	/**
	 * @return Returns the list of groups this constraint is part of. This might include the default group even when
	 *         it is not explicitly specified, but part of the redefined default group list of the hosting bean.
	 */
	public final Set<Class<?>> getGroupList() {
		return constraintDescriptor.getGroups();
	}

	public final ConstraintDescriptorImpl<A> getDescriptor() {
		return constraintDescriptor;
	}

	public final ElementType getElementType() {
		return constraintDescriptor.getElementType();
	}

	public boolean validateConstraint(ValidationContext<?> executionContext, ValueContext<?, ?> valueContext) {
		valueContext.setElementType( getElementType() );
		boolean validationResult = constraintTree.validateConstraints( executionContext, valueContext );

		return validationResult;
	}

	public ConstraintLocation getLocation() {
		return location;
	}

	public List<ValueExtractorDescriptor> getValueExtractorDescriptors() {
		return valueExtractorDescriptors;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		MetaConstraint<?> that = (MetaConstraint<?>) o;

		if ( constraintDescriptor != null ? !constraintDescriptor.equals( that.constraintDescriptor ) : that.constraintDescriptor != null ) {
			return false;
		}
		if ( location != null ? !location.equals( that.location ) : that.location != null ) {
			return false;
		}
		if ( valueExtractorDescriptors != null ? !valueExtractorDescriptors.equals( that.valueExtractorDescriptors ) : that.valueExtractorDescriptors != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = constraintDescriptor != null ? constraintDescriptor.hashCode() : 0;
		result = 31 * result + ( location != null ? location.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "MetaConstraint" );
		sb.append( "{constraintType=" ).append( StringHelper.toShortString( constraintDescriptor.getAnnotation().annotationType() ) );
		sb.append( ", location=" ).append( location );
		sb.append( ", valueExtractorDescriptors=" ).append( valueExtractorDescriptors );
		sb.append( "}" );
		return sb.toString();
	}
}
