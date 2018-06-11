/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * An  {@code AnnotationProcessingOptions} instance keeps track of annotations which should be ignored as configuration source.
 * The main validation source for Bean Validation is annotation and alternate configuration sources use this class
 * to override/ignore existing annotations.
 *
 * @author Hardy Ferentschik
 */
public class AnnotationProcessingOptionsImpl implements AnnotationProcessingOptions {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * Keeps track whether the 'ignore-annotations' flag is set on bean level in the xml configuration. If 'ignore-annotations'
	 * is not specified {@code true} is the default.
	 */
	private final Map<Class<?>, Boolean> ignoreAnnotationDefaults = newHashMap();

	/**
	 * Keeps track of explicitly excluded class level constraints.
	 */
	private final Map<Class<?>, Boolean> annotationIgnoresForClasses = newHashMap();

	/**
	 * Keeps track of explicitly excluded members (fields and properties).
	 */
	private final Map<Constrainable, Boolean> annotationIgnoredForMembers = newHashMap();

	/**
	 * Keeps track of explicitly excluded return value constraints for methods/constructors.
	 */
	private final Map<Constrainable, Boolean> annotationIgnoresForReturnValues = newHashMap();

	/**
	 * Keeps track of explicitly excluded cross parameter constraints for methods/constructors.
	 */
	private final Map<Constrainable, Boolean> annotationIgnoresForCrossParameter = newHashMap();

	/**
	 * Keeps track whether the 'ignore-annotations' flag is set on a method/constructor parameter
	 */
	private final Map<ExecutableParameterKey, Boolean> annotationIgnoresForMethodParameter = newHashMap();

	@Override
	public boolean areMemberConstraintsIgnoredFor(Constrainable constrainable) {
		Class<?> clazz = constrainable.getDeclaringClass();
		Boolean annotationIgnoredForMember = annotationIgnoredForMembers.get( constrainable );
		if ( annotationIgnoredForMember != null ) {
			return annotationIgnoredForMember;
		}
		else {
			return areAllConstraintAnnotationsIgnoredFor( clazz );
		}
	}

	@Override
	public boolean areReturnValueConstraintsIgnoredFor(Constrainable constrainable) {
		Boolean annotationIgnoreForReturnValue = annotationIgnoresForReturnValues.get( constrainable );
		if ( annotationIgnoreForReturnValue != null ) {
			return annotationIgnoreForReturnValue;
		}
		else {
			return areMemberConstraintsIgnoredFor( constrainable );
		}
	}

	@Override
	public boolean areCrossParameterConstraintsIgnoredFor(Constrainable constrainable) {
		Boolean annotationIgnoreForCrossParameter = annotationIgnoresForCrossParameter.get( constrainable );
		if ( annotationIgnoreForCrossParameter != null ) {
			return annotationIgnoreForCrossParameter;
		}
		else {
			return areMemberConstraintsIgnoredFor( constrainable );
		}
	}

	@Override
	public boolean areParameterConstraintsIgnoredFor(Constrainable constrainable, int index) {
		ExecutableParameterKey key = new ExecutableParameterKey( constrainable, index );
		Boolean annotationIgnoreForMethodParameter = annotationIgnoresForMethodParameter.get( key );
		if ( annotationIgnoreForMethodParameter != null ) {
			return annotationIgnoreForMethodParameter;
		}
		else {
			return areMemberConstraintsIgnoredFor( constrainable );
		}
	}

	@Override
	public boolean areClassLevelConstraintsIgnoredFor(Class<?> clazz) {
		boolean ignoreAnnotation;
		Boolean annotationIgnoreForClass = annotationIgnoresForClasses.get( clazz );
		if ( annotationIgnoreForClass != null ) {
			ignoreAnnotation = annotationIgnoreForClass;
		}
		else {
			ignoreAnnotation = areAllConstraintAnnotationsIgnoredFor( clazz );
		}
		if ( LOG.isDebugEnabled() && ignoreAnnotation ) {
			LOG.debugf( "Class level annotation are getting ignored for %s.", clazz.getName() );
		}
		return ignoreAnnotation;
	}

	@Override
	public void merge(AnnotationProcessingOptions annotationProcessingOptions) {
		AnnotationProcessingOptionsImpl annotationProcessingOptionsImpl = (AnnotationProcessingOptionsImpl) annotationProcessingOptions;

		// TODO rethink the "merging" of these options. It will depend on the order of merging (HF)
		this.ignoreAnnotationDefaults.putAll( annotationProcessingOptionsImpl.ignoreAnnotationDefaults );
		this.annotationIgnoresForClasses.putAll( annotationProcessingOptionsImpl.annotationIgnoresForClasses );
		this.annotationIgnoredForMembers.putAll( annotationProcessingOptionsImpl.annotationIgnoredForMembers );
		this.annotationIgnoresForReturnValues
				.putAll( annotationProcessingOptionsImpl.annotationIgnoresForReturnValues );
		this.annotationIgnoresForCrossParameter
				.putAll( annotationProcessingOptionsImpl.annotationIgnoresForCrossParameter );
		this.annotationIgnoresForMethodParameter.putAll( annotationProcessingOptionsImpl.annotationIgnoresForMethodParameter );
	}

	public void ignoreAnnotationConstraintForClass(Class<?> clazz, Boolean b) {
		if ( b == null ) {
			ignoreAnnotationDefaults.put( clazz, Boolean.TRUE );
		}
		else {
			ignoreAnnotationDefaults.put( clazz, b );
		}
	}

	public void ignoreConstraintAnnotationsOnMember(Constrainable member, Boolean b) {
		annotationIgnoredForMembers.put( member, b );
	}

	public void ignoreConstraintAnnotationsForReturnValue(Constrainable member, Boolean b) {
		annotationIgnoresForReturnValues.put( member, b );
	}

	public void ignoreConstraintAnnotationsForCrossParameterConstraint(Constrainable member, Boolean b) {
		annotationIgnoresForCrossParameter.put( member, b );
	}

	public void ignoreConstraintAnnotationsOnParameter(Constrainable member, int index, Boolean b) {
		ExecutableParameterKey key = new ExecutableParameterKey( member, index );
		annotationIgnoresForMethodParameter.put( key, b );
	}

	public void ignoreClassLevelConstraintAnnotations(Class<?> clazz, boolean b) {
		annotationIgnoresForClasses.put( clazz, b );
	}

	private boolean areAllConstraintAnnotationsIgnoredFor(Class<?> clazz) {
		return ignoreAnnotationDefaults.containsKey( clazz ) && ignoreAnnotationDefaults.get( clazz );
	}

	public class ExecutableParameterKey {
		private final Constrainable constrainable;
		private final int index;

		public ExecutableParameterKey(Constrainable constrainable, int index) {
			this.constrainable = constrainable;
			this.index = index;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			ExecutableParameterKey that = (ExecutableParameterKey) o;

			if ( index != that.index ) {
				return false;
			}
			if ( constrainable != null ? !constrainable.equals( that.constrainable ) : that.constrainable != null ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = constrainable != null ? constrainable.hashCode() : 0;
			result = 31 * result + index;
			return result;
		}
	}
}
