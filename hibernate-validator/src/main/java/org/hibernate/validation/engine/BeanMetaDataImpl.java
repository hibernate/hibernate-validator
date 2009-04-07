// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.BeanDescriptor;
import javax.validation.GroupSequence;
import javax.validation.PropertyDescriptor;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.groups.Default;

import org.slf4j.Logger;

import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.ReflectionHelper;


/**
 * This class encapsulates all meta data needed for validation. Implementations of <code>Validator</code> interface can
 * instantiate an instance of this class and delegate the metadata extraction to it.
 *
 * @author Hardy Ferentschik
 * @todo check the unchecked assignment warnings in this class
 */

public class BeanMetaDataImpl<T> implements BeanMetaData<T> {

	private static final Logger log = LoggerFactory.make();

	/**
	 * The root bean class for this validator.
	 */
	private final Class<T> beanClass;

	/**
	 * The main element descriptor for <code>beanClass</code>.
	 */
	private BeanDescriptorImpl<T> beanDescriptor;

	/**
	 * List of constraints.
	 */
	private List<MetaConstraint<T, ?>> metaConstraintList = new ArrayList<MetaConstraint<T, ?>>();

	/**
	 * List of cascaded members.
	 */
	private List<Member> cascadedMembers = new ArrayList<Member>();

	/**
	 * Maps field and method names to their <code>ElementDescriptorImpl</code>.
	 */
	private Map<String, PropertyDescriptor> propertyDescriptors = new HashMap<String, PropertyDescriptor>();

	/**
	 * Maps group sequences to the list of group/sequences.
	 */
	private List<Class<?>> defaultGroupSequence = new ArrayList<Class<?>>();

	/**
	 * Object keeping track of all builtin constraints.
	 */
	private final ConstraintHelper constraintHelper;

	private Map<Class<?>, Boolean> ignoreAnnotationDefaults;
	private Map<Class<?>, List<Member>> ignoreAnnotationOnMember;
	private List<Class<?>> ignoreAnnotationOnClass;

	public BeanMetaDataImpl(Class<T> beanClass, ConstraintHelper constraintHelper) {
		this(
				beanClass,
				constraintHelper,
				new HashMap<Class<?>, Boolean>(),
				new ArrayList<Class<?>>(),
				new HashMap<Class<?>, List<Member>>()
		);
	}

	public BeanMetaDataImpl(Class<T> beanClass, ConstraintHelper constraintHelper, Map<Class<?>, Boolean> ignoreAnnotationDefaults, List<Class<?>> ignoreAnnotationOnClass, Map<Class<?>, List<Member>> ignoreAnnotationOnMember) {
		this.beanClass = beanClass;
		this.constraintHelper = constraintHelper;
		this.ignoreAnnotationDefaults = ignoreAnnotationDefaults;
		this.ignoreAnnotationOnClass = ignoreAnnotationOnClass;
		this.ignoreAnnotationOnMember = ignoreAnnotationOnMember;

		createMetaData();

		this.ignoreAnnotationDefaults = null;
		this.ignoreAnnotationOnClass = null;
		this.ignoreAnnotationOnMember = null;
	}

	public Class<T> getBeanClass() {
		return beanClass;
	}

	public BeanDescriptor getBeanDescriptor() {
		return beanDescriptor;
	}

	public List<Member> getCascadedMembers() {
		return cascadedMembers;
	}

	public List<MetaConstraint<T, ?>> geMetaConstraintList() {
		return metaConstraintList;
	}

	public void addMetaConstraint(MetaConstraint<?, ?> metaConstraint) {
		metaConstraintList.add( ( MetaConstraint<T, ?> ) metaConstraint );
	}

	public PropertyDescriptor getPropertyDescriptor(String property) {
		return propertyDescriptors.get( property );
	}

	public List<Class<?>> getDefaultGroupSequence() {
		return defaultGroupSequence;
	}

	public Set<PropertyDescriptor> getConstrainedProperties() {
		return Collections.unmodifiableSet( new HashSet<PropertyDescriptor>( propertyDescriptors.values() ) );
	}

	/**
	 * Create bean desciptor, find all classes/subclasses/interfaces which have to be taken in consideration
	 * for this validator and create meta data.
	 */
	private void createMetaData() {
		beanDescriptor = new BeanDescriptorImpl<T>( this );
		initDefaultGroupSequence( beanClass );
		List<Class> classes = new ArrayList<Class>();
		computeClassHierarchy( beanClass, classes );
		for ( Class current : classes ) {
			initClass( current );
		}
	}

	/**
	 * Get all superclasses and interfaces recursively.
	 *
	 * @param clazz The class to start the search with.
	 * @param classes List of classes to which to add all found super classes and interfaces.
	 */
	private void computeClassHierarchy(Class clazz, List<Class> classes) {
		if ( log.isTraceEnabled() ) {
			log.trace( "Processing: {}", clazz );
		}
		for ( Class current = clazz; current != null; current = current.getSuperclass() ) {
			if ( classes.contains( current ) ) {
				return;
			}
			classes.add( current );
			for ( Class currentInterface : current.getInterfaces() ) {
				computeClassHierarchy( currentInterface, classes );
			}
		}
	}

	private void initClass(Class clazz) {
		initClassConstraints( clazz );
		initMethodConstraints( clazz );
		initFieldConstraints( clazz );
	}

	/**
	 * Checks whether there is a default group sequence defined for this class.
	 * See HV-113.
	 *
	 * @param clazz The class to check for the <code>GroupSequence</code> annotation.
	 */
	private void initDefaultGroupSequence(Class<?> clazz) {
		GroupSequence groupSequenceAnnotation = clazz.getAnnotation( GroupSequence.class );
		if ( groupSequenceAnnotation == null ) {
			defaultGroupSequence.add( Default.class );
		}
		else {
			List<Class<?>> groupSequenceList = Arrays.asList( groupSequenceAnnotation.value() );
			for ( Class<?> group : groupSequenceList ) {
				if ( group.getName().equals( clazz.getName() ) ) {
					defaultGroupSequence.add( Default.class );
				}
				else if ( group.getName().equals( Default.class.getName() ) ) {
					throw new ValidationException( "'Default.class' cannot appear in default group sequence list." );
				}
				else {
					defaultGroupSequence.add( group );
				}
			}
			if ( log.isDebugEnabled() ) {
				log.debug(
						"Bean {} redefines the Default group. Members of the default group sequence are: {}",
						clazz.getName(),
						defaultGroupSequence
				);
			}
		}
	}

	private <A extends Annotation> void initFieldConstraints(Class clazz) {
		for ( Field field : clazz.getDeclaredFields() ) {
			List<ConstraintDescriptorImpl> fieldMetadata = findConstraints( field );
			for ( ConstraintDescriptorImpl constraintDescription : fieldMetadata ) {
				if ( checkSkipAnnotationProcessing( clazz, field ) ) {
					break;
				}
				ReflectionHelper.setAccessibility( field );
				MetaConstraint<T, A> metaConstraint = new MetaConstraint<T, A>(
						field, beanClass, constraintDescription
				);
				metaConstraintList.add( metaConstraint );
			}
			if ( field.isAnnotationPresent( Valid.class ) ) {
				ReflectionHelper.setAccessibility( field );
				cascadedMembers.add( field );
				addPropertyDescriptorForMember( field );
			}
		}
	}

	private <A extends Annotation> void initMethodConstraints(Class clazz) {
		for ( Method method : clazz.getDeclaredMethods() ) {
			List<ConstraintDescriptorImpl> methodMetadata = findConstraints( method );
			for ( ConstraintDescriptorImpl constraintDescription : methodMetadata ) {
				if ( checkSkipAnnotationProcessing( clazz, method ) ) {
					break;
				}
				ReflectionHelper.setAccessibility( method );
				MetaConstraint<T, A> metaConstraint = new MetaConstraint<T, A>(
						method, beanClass, constraintDescription
				);
				metaConstraintList.add( metaConstraint );
			}
			if ( method.isAnnotationPresent( Valid.class ) ) {
				ReflectionHelper.setAccessibility( method );
				cascadedMembers.add( method );
				addPropertyDescriptorForMember( method );
			}
		}
	}

	private boolean checkSkipAnnotationProcessing(Class clazz, Member member) {
		List<Member> ignoreAnnotationForFields = ignoreAnnotationOnMember.get( clazz );
		boolean ignoreAnnotation;
		if ( ignoreAnnotationForFields == null || !ignoreAnnotationForFields.contains( member ) ) {
			ignoreAnnotation = ignoreAnnotationDefaults.containsKey( clazz ) && ignoreAnnotationDefaults.get( clazz );
		}
		else {
			ignoreAnnotation = ignoreAnnotationForFields.contains( member );
		}
		if ( ignoreAnnotation ) {
			String type;
			if ( member instanceof Field ) {
				type = "Field";
			}
			else {
				type = "Property";
			}
			log.debug( type + " level annotations are getting ignored for " + clazz.getName() + "." + member.getName() );
			return true;
		}
		return false;
	}

	private PropertyDescriptorImpl addPropertyDescriptorForMember(Member member) {
		String name = ReflectionHelper.getPropertyName( member );
		PropertyDescriptorImpl propertyDescriptor = ( PropertyDescriptorImpl ) propertyDescriptors.get(
				name
		);
		if ( propertyDescriptor == null ) {
			propertyDescriptor = new PropertyDescriptorImpl(
					ReflectionHelper.getType( member ),
					( ( AnnotatedElement ) member ).isAnnotationPresent( Valid.class ),
					name
			);
			propertyDescriptors.put( name, propertyDescriptor );
		}
		return propertyDescriptor;
	}

	private <A extends Annotation> void initClassConstraints(Class clazz) {
		if ( ignoreAnnotationOnClass.contains( clazz ) ||
				( ignoreAnnotationDefaults.containsKey( clazz ) && ignoreAnnotationDefaults.get( clazz ) ) ) {
			log.debug( "Class level annotation are getting ignored for " + clazz.getName() );
			return;
		}
		List<ConstraintDescriptorImpl> classMetadata = findClassLevelConstraints( clazz );
		for ( ConstraintDescriptorImpl constraintDescription : classMetadata ) {
			MetaConstraint<T, A> metaConstraint = new MetaConstraint<T, A>( clazz, constraintDescription );
			metaConstraintList.add( metaConstraint );
		}
	}

	/**
	 * Examines the given annotation to see whether it is a single or multi valued constraint annotation.
	 *
	 * @param clazz the class we are currently processing.
	 * @param annotation The annotation to examine.
	 *
	 * @return A list of constraint descriptors or the empty list in case <code>annotation</code> is neither a
	 *         single nor multi value annotation.
	 */
	private <A extends Annotation> List<ConstraintDescriptorImpl> findConstraintAnnotations(Class<?> clazz, A annotation) {
		List<ConstraintDescriptorImpl> constraintDescriptors = new ArrayList<ConstraintDescriptorImpl>();

		List<Annotation> constraints = new ArrayList<Annotation>();
		if ( constraintHelper.isConstraintAnnotation( annotation ) ||
				constraintHelper.isBuiltinConstraint( annotation ) ) {
			constraints.add( annotation );
		}

		// check if we have a multi value constraint
		constraints.addAll( constraintHelper.getMultiValueConstraints( annotation ) );

		for ( Annotation constraint : constraints ) {
			final ConstraintDescriptorImpl constraintDescriptor = buildConstraintDescriptor( clazz, constraint );
			constraintDescriptors.add( constraintDescriptor );
		}
		return constraintDescriptors;
	}

	@SuppressWarnings("unchecked")
	private <A extends Annotation> ConstraintDescriptorImpl buildConstraintDescriptor(Class<?> clazz, A annotation) {
		Class<?>[] groups = ReflectionHelper.getAnnotationParameter( annotation, "groups", Class[].class );
		ConstraintDescriptorImpl constraintDescriptor;
		if ( clazz.isInterface() ) {
			constraintDescriptor = new ConstraintDescriptorImpl( annotation, groups, constraintHelper, clazz );
		}
		else {
			constraintDescriptor = new ConstraintDescriptorImpl( annotation, groups, constraintHelper );
		}
		return constraintDescriptor;
	}

	/**
	 * Finds all constraint annotations defined for the given class and returns them in a list of
	 * constraint descriptors.
	 *
	 * @param beanClass The class to check for constraints annotations.
	 *
	 * @return A list of constraint descriptors for all constraint specified on the given class.
	 */
	private List<ConstraintDescriptorImpl> findClassLevelConstraints(Class<?> beanClass) {
		List<ConstraintDescriptorImpl> metadata = new ArrayList<ConstraintDescriptorImpl>();
		for ( Annotation annotation : beanClass.getAnnotations() ) {
			metadata.addAll( findConstraintAnnotations( beanClass, annotation ) );
		}
		for ( ConstraintDescriptorImpl constraintDescriptor : metadata ) {
			beanDescriptor.addConstraintDescriptor( constraintDescriptor );
		}
		return metadata;
	}


	/**
	 * Finds all constraint annotations defined for the given field/method and returns them in a list of
	 * constraint descriptors.
	 *
	 * @param member The fiels or method to check for constraints annotations.
	 *
	 * @return A list of constraint descriptors for all constraint specified for the given field or method.
	 */
	private List<ConstraintDescriptorImpl> findConstraints(Member member) {
		assert member instanceof Field || member instanceof Method;

		List<ConstraintDescriptorImpl> metadata = new ArrayList<ConstraintDescriptorImpl>();
		for ( Annotation annotation : ( ( AnnotatedElement ) member ).getAnnotations() ) {
			metadata.addAll( findConstraintAnnotations( member.getDeclaringClass(), annotation ) );
		}

		String name = ReflectionHelper.getPropertyName( member );
		for ( ConstraintDescriptorImpl constraintDescriptor : metadata ) {
			if ( member instanceof Method && name == null ) { // can happen if member is a Method which does not follow the bean convention
				throw new ValidationException(
						"Annotated methods must follow the JavaBeans naming convention. " + member.getName() + "() does not."
				);
			}
			PropertyDescriptorImpl propertyDescriptor = ( PropertyDescriptorImpl ) propertyDescriptors.get( name );
			if ( propertyDescriptor == null ) {
				propertyDescriptor = addPropertyDescriptorForMember( member );
			}
			propertyDescriptor.addConstraintDescriptor( constraintDescriptor );
		}
		return metadata;
	}
}