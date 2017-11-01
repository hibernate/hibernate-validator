/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.annotation;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.validation.ConstraintTarget;
import javax.validation.Payload;

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

	public Class<? extends Payload>[] getPayload() {
		return getMandatoryAttribute( ConstraintHelper.PAYLOAD, Class[].class );
	}

	public ConstraintTarget validationAppliesTo() {
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

		public Builder setMessage(String message) {
			setAttribute( ConstraintHelper.MESSAGE, message );
			return this;
		}

		public Builder setGroups(Class<?>[] groups) {
			setAttribute( ConstraintHelper.GROUPS, groups );
			return this;
		}

		public Builder setPayload(Class<?>[] payload) {
			setAttribute( ConstraintHelper.PAYLOAD, payload );
			return this;
		}

		@Override
		public ConstraintAnnotationDescriptor<S> build() {
			return new ConstraintAnnotationDescriptor<>( super.build() );
		}
	}
}
