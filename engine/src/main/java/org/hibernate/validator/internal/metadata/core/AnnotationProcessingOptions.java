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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
public class AnnotationProcessingOptions {

	private static final Log log = LoggerFactory.make();

	/**
	 * Keeps track whether the 'ignore-annotations' flag is set on bean level in the xml configuration. If 'ignore-annotations'
	 * is not specified {@code true} is the default.
	 */
	private final Map<Class<?>, Boolean> ignoreAnnotationDefaults = newHashMap();

	/**
	 * Keeps track of explicitly excluded members (fields and properties) for a given class. If a member appears in
	 * the list mapped to a given class 'ignore-annotations' was explicitly set to {@code true} in the configuration
	 * for this class.
	 */
	private final Map<Class<?>, List<Member>> propertyConstraintAnnotationIgnores = newHashMap();

	/**
	 * Keeps track of explicitly excluded class level constraints.
	 */
	private final Map<Class<?>, Boolean> classConstraintAnnotationIgnores = newHashMap();

	/**
	 * Keeps track of explicitly excluded constructor constraints
	 */
	private final Map<Class<?>, List<Constructor>> constructorAnnotationIgnores = newHashMap();

	/**
	 * Keeps track of explicitly excluded method constraints
	 */
	private final Map<Class<?>, List<Method>> methodAnnotationIgnores = newHashMap();

	public void ignoreAnnotationConstraintForClass(Class<?> clazz, Boolean b) {
		if ( b == null ) {
			ignoreAnnotationDefaults.put( clazz, Boolean.TRUE );
		}
		else {
			ignoreAnnotationDefaults.put( clazz, b );
		}
	}

	public boolean areConstraintAnnotationsIgnored(Class<?> clazz) {
		return ignoreAnnotationDefaults.containsKey( clazz ) && ignoreAnnotationDefaults.get( clazz );
	}

	public void ignorePropertyLevelConstraintAnnotationsOnMember(Member member) {
		Class<?> beanClass = member.getDeclaringClass();
		if ( propertyConstraintAnnotationIgnores.get( beanClass ) == null ) {
			List<Member> tmpList = new ArrayList<Member>();
			tmpList.add( member );
			propertyConstraintAnnotationIgnores.put( beanClass, tmpList );
		}
		else {
			propertyConstraintAnnotationIgnores.get( beanClass ).add( member );
		}
	}

	public void ignoreConstraintAnnotationsOnConstructor(Constructor constructor) {
		Class<?> beanClass = constructor.getDeclaringClass();
		if ( constructorAnnotationIgnores.get( beanClass ) == null ) {
			List<Constructor> tmpList = newArrayList();
			tmpList.add( constructor );
			constructorAnnotationIgnores.put( beanClass, tmpList );
		}
		else {
			constructorAnnotationIgnores.get( beanClass ).add( constructor );
		}
	}

	public void ignoreConstraintAnnotationsOnMethod(Method method) {
		Class<?> beanClass = method.getDeclaringClass();
		if ( methodAnnotationIgnores.get( beanClass ) == null ) {
			List<Method> tmpList = newArrayList();
			tmpList.add( method );
			methodAnnotationIgnores.put( beanClass, tmpList );
		}
		else {
			methodAnnotationIgnores.get( beanClass ).add( method );
		}
	}

	public boolean arePropertyLevelConstraintAnnotationsIgnored(Member member) {
		boolean ignoreAnnotation;
		Class<?> clazz = member.getDeclaringClass();
		List<Member> ignoreAnnotationForMembers = propertyConstraintAnnotationIgnores.get( clazz );
		if ( ignoreAnnotationForMembers == null || !ignoreAnnotationForMembers.contains( member ) ) {
			ignoreAnnotation = areConstraintAnnotationsIgnored( clazz );
		}
		else {
			ignoreAnnotation = ignoreAnnotationForMembers.contains( member );
		}
		if ( ignoreAnnotation ) {
			logMessage( member, clazz );
		}
		return ignoreAnnotation;
	}

	public void ignoreClassLevelConstraintAnnotations(Class<?> clazz, boolean b) {
		classConstraintAnnotationIgnores.put( clazz, b );
	}

	public boolean areClassLevelConstraintAnnotationsIgnored(Class<?> clazz) {
		boolean ignoreAnnotation;
		if ( classConstraintAnnotationIgnores.containsKey( clazz ) ) {
			ignoreAnnotation = classConstraintAnnotationIgnores.get( clazz );
		}
		else {
			ignoreAnnotation = areConstraintAnnotationsIgnored( clazz );
		}
		if ( log.isDebugEnabled() && ignoreAnnotation ) {
			log.debugf( "Class level annotation are getting ignored for %s.", clazz.getName() );
		}
		return ignoreAnnotation;
	}

	public void merge(AnnotationProcessingOptions annotationProcessingOptions) {
		this.ignoreAnnotationDefaults.putAll( annotationProcessingOptions.ignoreAnnotationDefaults );
		this.propertyConstraintAnnotationIgnores
				.putAll( annotationProcessingOptions.propertyConstraintAnnotationIgnores );
		this.classConstraintAnnotationIgnores
				.putAll( annotationProcessingOptions.classConstraintAnnotationIgnores );
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
