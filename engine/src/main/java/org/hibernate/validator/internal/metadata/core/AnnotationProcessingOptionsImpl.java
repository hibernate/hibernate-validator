/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.metadata.core;

import java.lang.reflect.Member;
import java.util.Map;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * An  {@code AnnotationProcessingOptions} instance keeps track of annotations which should be ignored as configuration source.
 * The main validation source for Bean Validation is annotation and alternate configuration sources use this class
 * to override/ignore existing annotations.
 *
 * @author Hardy Ferentschik
 */
public class AnnotationProcessingOptionsImpl implements AnnotationProcessingOptions {

	private static final Log log = LoggerFactory.make();

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
	private final Map<Member, Boolean> annotationIgnoredForMembers = newHashMap();

	/**
	 * Keeps track of explicitly excluded return value constraints for methods/constructors.
	 */
	private final Map<Member, Boolean> annotationIgnoresForReturnValues = newHashMap();

	/**
	 * Keeps track of explicitly excluded cross parameter constraints for methods/constructors.
	 */
	private final Map<Member, Boolean> annotationIgnoresForCrossParameter = newHashMap();

	/**
	 * Keeps track whether the 'ignore-annotations' flag is set on a method/constructor parameter
	 */
	private final Map<ExecutableParameterKey, Boolean> annotationIgnoresForMethodParameter = newHashMap();

	@Override
	public boolean areMemberConstraintsIgnoredFor(Member member) {
		Class<?> clazz = member.getDeclaringClass();
		if ( annotationIgnoredForMembers.containsKey( member ) ) {
			return annotationIgnoredForMembers.get( member );
		}
		else {
			return areAllConstraintAnnotationsIgnoredFor( clazz );
		}
	}

	@Override
	public boolean areReturnValueConstraintsIgnoredFor(Member member) {
		if ( annotationIgnoresForReturnValues.containsKey( member ) ) {
			return annotationIgnoresForReturnValues.get( member );
		}
		else {
			return areMemberConstraintsIgnoredFor( member );
		}
	}

	@Override
	public boolean areCrossParameterConstraintsIgnoredFor(Member member) {
		if ( annotationIgnoresForCrossParameter.containsKey( member ) ) {
			return annotationIgnoresForCrossParameter.get( member );
		}
		else {
			return areMemberConstraintsIgnoredFor( member );
		}
	}

	@Override
	public boolean areParameterConstraintsIgnoredFor(Member member, int index) {
		ExecutableParameterKey key = new ExecutableParameterKey( member, index );
		if ( annotationIgnoresForMethodParameter.containsKey( key ) ) {
			return annotationIgnoresForMethodParameter.get( key );
		}
		else {
			return areMemberConstraintsIgnoredFor( member );
		}
	}

	@Override
	public boolean areClassLevelConstraintsIgnoredFor(Class<?> clazz) {
		boolean ignoreAnnotation;
		if ( annotationIgnoresForClasses.containsKey( clazz ) ) {
			ignoreAnnotation = annotationIgnoresForClasses.get( clazz );
		}
		else {
			ignoreAnnotation = areAllConstraintAnnotationsIgnoredFor( clazz );
		}
		if ( log.isDebugEnabled() && ignoreAnnotation ) {
			log.debugf( "Class level annotation are getting ignored for %s.", clazz.getName() );
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

	public void ignoreConstraintAnnotationsOnMember(Member member, Boolean b) {
		annotationIgnoredForMembers.put( member, b );
	}

	public void ignoreConstraintAnnotationsForReturnValue(Member member, Boolean b) {
		annotationIgnoresForReturnValues.put( member, b );
	}

	public void ignoreConstraintAnnotationsForCrossParameterConstraint(Member member, Boolean b) {
		annotationIgnoresForCrossParameter.put( member, b );
	}

	public void ignoreConstraintAnnotationsOnParameter(Member member, int index, Boolean b) {
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
		private final Member member;
		private final int index;

		public ExecutableParameterKey(Member member, int index) {
			this.member = member;
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
			if ( member != null ? !member.equals( that.member ) : that.member != null ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = member != null ? member.hashCode() : 0;
			result = 31 * result + index;
			return result;
		}
	}
}
