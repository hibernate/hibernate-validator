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

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
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
	 * Keeps track of explicitly excluded members (fields and properties). If a member appears in
	 * the list 'ignore-annotations' was explicitly set to {@code true} in the configuration
	 * for this class.
	 */
	private final List<Member> annotationIgnoredForMembers = newArrayList();

	/**
	 * Keeps track of explicitly excluded return value constraints for methods/constructors.
	 */
	private final List<Member> annotationIgnoresForReturnValues = newArrayList();

	/**
	 * Keeps track of explicitly excluded cross parameter constraints for methods/constructors.
	 */
	private final List<Member> annotationIgnoresForCrossParameter = newArrayList();

	/**
	 * Keeps track whether the 'ignore-annotations' flag is set on a method/constructor parameter
	 */
	private final Map<Member, List<Integer>> annotationIgnoresForMethodParameter = newHashMap();

	@Override
	public boolean areMemberConstraintsIgnoredFor(Member member) {
		boolean ignoreAnnotation;
		Class<?> clazz = member.getDeclaringClass();
		if ( !annotationIgnoredForMembers.contains( member ) ) {
			ignoreAnnotation = areAllConstraintAnnotationsIgnoredFor( clazz );
		}
		else {
			ignoreAnnotation = annotationIgnoredForMembers.contains( member );
		}
		if ( ignoreAnnotation ) {
			logMessage( member, clazz );
		}
		return ignoreAnnotation;
	}

	@Override
	public boolean areReturnValueConstraintsIgnoredFor(Member member) {
		if ( annotationIgnoresForReturnValues.contains( member ) ) {
			return true;
		}
		else {
			return areMemberConstraintsIgnoredFor( member );
		}
	}

	@Override
	public boolean areCrossParameterConstraintsIgnoredFor(Member member) {
		if ( annotationIgnoresForCrossParameter.contains( member ) ) {
			return true;
		}
		else {
			return areMemberConstraintsIgnoredFor( member );
		}
	}

	@Override
	public boolean areParameterConstraintsIgnoredFor(Member member, int index) {
		List<Integer> parameterIndexes = annotationIgnoresForMethodParameter.get( member );
		if ( parameterIndexes != null && parameterIndexes.contains( index ) ) {
			return true;
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
		this.ignoreAnnotationDefaults.putAll( annotationProcessingOptionsImpl.ignoreAnnotationDefaults );
		this.annotationIgnoresForClasses.putAll( annotationProcessingOptionsImpl.annotationIgnoresForClasses );
		this.annotationIgnoredForMembers.addAll( annotationProcessingOptionsImpl.annotationIgnoredForMembers );
		this.annotationIgnoresForReturnValues.addAll( annotationProcessingOptionsImpl.annotationIgnoresForReturnValues );
		this.annotationIgnoresForCrossParameter.addAll( annotationProcessingOptionsImpl.annotationIgnoresForCrossParameter );
		for ( Map.Entry<Member, List<Integer>> entry : annotationProcessingOptionsImpl.annotationIgnoresForMethodParameter
				.entrySet() ) {
			if ( this.annotationIgnoresForMethodParameter.containsKey( entry.getKey() ) ) {
				this.annotationIgnoresForMethodParameter.get( entry.getKey() ).addAll( entry.getValue() );
			}
			else {
				this.annotationIgnoresForMethodParameter.put( entry.getKey(), entry.getValue() );
			}
		}

	}

	public void ignoreAnnotationConstraintForClass(Class<?> clazz, Boolean b) {
		if ( b == null ) {
			ignoreAnnotationDefaults.put( clazz, Boolean.TRUE );
		}
		else {
			ignoreAnnotationDefaults.put( clazz, b );
		}
	}

	public void ignoreConstraintAnnotationsOnMember(Member member) {
		annotationIgnoredForMembers.add( member );
	}

	public void ignoreConstraintAnnotationsForReturnValue(Member member) {
		annotationIgnoresForReturnValues.add( member );
	}

	public void ignoreConstraintAnnotationsForCrossParameterConstraint(Member member) {
		annotationIgnoresForCrossParameter.add( member );
	}

	public void ignoreConstraintAnnotationsOnParameter(Member member, int index) {
		if ( annotationIgnoresForMethodParameter.get( member ) == null ) {
			List<Integer> tmpList = newArrayList();
			tmpList.add( index );
			annotationIgnoresForMethodParameter.put( member, tmpList );
		}
		else {
			annotationIgnoresForMethodParameter.get( member ).add( index );
		}
	}

	public void ignoreClassLevelConstraintAnnotations(Class<?> clazz, boolean b) {
		annotationIgnoresForClasses.put( clazz, b );
	}

	private boolean areAllConstraintAnnotationsIgnoredFor(Class<?> clazz) {
		return ignoreAnnotationDefaults.containsKey( clazz ) && ignoreAnnotationDefaults.get( clazz );
	}

	private void logMessage(Member member, Class<?> clazz) {
		if ( log.isTraceEnabled() ) {
			String type;
			if ( member instanceof Field ) {
				type = "Field";
			}
			else {
				type = "Property";
			}

			log.debugf(
					"%s level annotations are getting ignored for %s.%s.",
					type,
					clazz.getName(),
					member.getName()
			);
		}
	}
}
