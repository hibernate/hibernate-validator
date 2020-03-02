/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.annotation;

import java.lang.annotation.Annotation;
import java.util.Map;

import jakarta.validation.ConstraintTarget;
import jakarta.validation.Payload;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;

/**
 * @author Marko Bekhta
 */
public class ConstraintAnnotationDescriptor<A extends Annotation> extends AnnotationDescriptor<A> {

	public ConstraintAnnotationDescriptor(A annotation) {
		super( annotation );
	}

	public ConstraintAnnotationDescriptor(AnnotationDescriptor<A> descriptor) {
		super( descriptor );
	}

	public String getMessage() {
		return getMandatoryAttribute( ConstraintHelper.MESSAGE, String.class );
	}

	public Class<?>[] getGroups() {
		return getMandatoryAttribute( ConstraintHelper.GROUPS, Class[].class );
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Payload>[] getPayload() {
		return getMandatoryAttribute( ConstraintHelper.PAYLOAD, Class[].class );
	}

	public ConstraintTarget getValidationAppliesTo() {
		return getAttribute( ConstraintHelper.VALIDATION_APPLIES_TO, ConstraintTarget.class );
	}

	public static class Builder<S extends Annotation> extends AnnotationDescriptor.Builder<S> {

		public Builder(Class<S> type) {
			super( type );
		}

		public Builder(Class<S> type, Map<String, Object> attributes) {
			super( type, attributes );
		}

		public Builder(S annotation) {
			super( annotation );
		}

		public Builder<S> setMessage(String message) {
			setAttribute( ConstraintHelper.MESSAGE, message );
			return this;
		}

		public Builder<S> setGroups(Class<?>[] groups) {
			setAttribute( ConstraintHelper.GROUPS, groups );
			return this;
		}

		public Builder<S> setPayload(Class<?>[] payload) {
			setAttribute( ConstraintHelper.PAYLOAD, payload );
			return this;
		}

		public Builder<S> setValidationAppliesTo(ConstraintTarget validationAppliesTo) {
			setAttribute( ConstraintHelper.VALIDATION_APPLIES_TO, validationAppliesTo );
			return this;
		}

		@Override
		public ConstraintAnnotationDescriptor<S> build() {
			return new ConstraintAnnotationDescriptor<>( super.build() );
		}
	}
}
