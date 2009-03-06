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
	 * List of cascaded fields.
	 */
	private List<Field> cascadedFields = new ArrayList<Field>();

	/**
	 * List of cascaded methods.
	 */
	private List<Method> cascadedMethods = new ArrayList<Method>();

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

	public BeanMetaDataImpl(Class<T> beanClass, ConstraintHelper constraintHelper) {
		this.beanClass = beanClass;
		this.constraintHelper = constraintHelper;
		createMetaData();
	}

	/**
	 * Create bean desciptor, find all classes/subclasses/interfaces which have to be taken in consideration
	 * for this validator and create meta data.
	 */
	private void createMetaData() {
		beanDescriptor = new BeanDescriptorImpl<T>( beanClass, this );
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
			List<ConstraintDescriptorImpl> fieldMetadata = findFieldLevelConstraints( field );
			for ( ConstraintDescriptorImpl constraintDescription : fieldMetadata ) {
				ReflectionHelper.setAccessibility( field );
				MetaConstraint<T, A> metaConstraint = new MetaConstraint<T, A>(
						field, beanClass, constraintDescription
				);
				metaConstraintList.add( metaConstraint );
			}
			if ( field.isAnnotationPresent( Valid.class ) ) {
				ReflectionHelper.setAccessibility( field );
				String name = field.getName();
				cascadedFields.add( field );
				addPropertyDescriptorForMember( field );
			}
		}
	}

	private <A extends Annotation> void initMethodConstraints(Class clazz) {
		for ( Method method : clazz.getDeclaredMethods() ) {
			List<ConstraintDescriptorImpl> methodMetadata = findMethodLevelConstraints( method );
			for ( ConstraintDescriptorImpl constraintDescription : methodMetadata ) {
				ReflectionHelper.setAccessibility( method );
				MetaConstraint<T, A> metaConstraint = new MetaConstraint<T, A>(
						method, beanClass, constraintDescription
				);
				metaConstraintList.add( metaConstraint );
			}
			if ( method.isAnnotationPresent( Valid.class ) ) {
				ReflectionHelper.setAccessibility( method );
				cascadedMethods.add( method );
				addPropertyDescriptorForMember( method );
			}
		}
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
		List<ConstraintDescriptorImpl> classMetadata = findClassLevelConstraints( clazz );
		for ( ConstraintDescriptorImpl constraintDescription : classMetadata ) {
			MetaConstraint<T, A> metaConstraint = new MetaConstraint<T, A>( clazz, constraintDescription );
			metaConstraintList.add( metaConstraint );
		}
	}

	/**
	 * Examines the given annotation to see whether it is a single or multi valued constraint annotation.
	 *
	 * @param annotation The annotation to examine.
	 *
	 * @return A list of constraint descriptors or the empty list in case <code>annotation</code> is neither a
	 *         single nor multi value annotation.
	 */
	private <A extends Annotation> List<ConstraintDescriptorImpl> findConstraintAnnotations(A annotation) {
		List<ConstraintDescriptorImpl> constraintDescriptors = new ArrayList<ConstraintDescriptorImpl>();

		List<Annotation> constraints = new ArrayList<Annotation>();
		if ( constraintHelper.isConstraintAnnotation( annotation ) ||
				constraintHelper.isBuiltinConstraint( annotation ) ) {
			constraints.add( annotation );
		}

		// check if we have a multi value constraint
		constraints.addAll( constraintHelper.getMultiValueConstraints( annotation ) );

		for ( Annotation constraint : constraints ) {
			final ConstraintDescriptorImpl constraintDescriptor = buildConstraintDescriptor( constraint );
			constraintDescriptors.add( constraintDescriptor );
		}
		return constraintDescriptors;
	}

	@SuppressWarnings("unchecked")
	private <A extends Annotation> ConstraintDescriptorImpl buildConstraintDescriptor(A annotation) {
		Class<?>[] groups = ReflectionHelper.getAnnotationParameter( annotation, "groups", Class[].class );
		return new ConstraintDescriptorImpl( annotation, groups, constraintHelper );
	}

	/**
	 * Finds all constraint annotations defined for the given class and returns them in a list of
	 * constraint descriptors.
	 *
	 * @param beanClass The class to check for constraints annotations.
	 *
	 * @return A list of constraint descriptors for all constraint specified on the given class.
	 */
	private List<ConstraintDescriptorImpl> findClassLevelConstraints(Class beanClass) {
		List<ConstraintDescriptorImpl> metadata = new ArrayList<ConstraintDescriptorImpl>();
		for ( Annotation annotation : beanClass.getAnnotations() ) {
			metadata.addAll( findConstraintAnnotations( annotation ) );
		}
		for ( ConstraintDescriptorImpl constraintDescriptor : metadata ) {
			beanDescriptor.addConstraintDescriptor( constraintDescriptor );
		}
		return metadata;
	}

	/**
	 * Finds all constraint annotations defined for the given methods and returns them in a list of
	 * constraint descriptors.
	 *
	 * @param method The method to check for constraints annotations.
	 *
	 * @return A list of constraint descriptors for all constraint specified for the given method.
	 */
	private List<ConstraintDescriptorImpl> findMethodLevelConstraints(Method method) {
		List<ConstraintDescriptorImpl> metadata = new ArrayList<ConstraintDescriptorImpl>();
		for ( Annotation annotation : method.getAnnotations() ) {
			metadata.addAll( findConstraintAnnotations( annotation ) );
		}

		String methodName = ReflectionHelper.getPropertyName( method );
		for ( ConstraintDescriptorImpl constraintDescriptor : metadata ) {
			if ( methodName == null ) {
				throw new ValidationException(
						"Annotated methods must follow the JavaBeans naming convention. " + method.getName() + "() does not."
				);
			}
			PropertyDescriptorImpl propertyDescriptor = ( PropertyDescriptorImpl ) propertyDescriptors.get( methodName );
			if ( propertyDescriptor == null ) {
				propertyDescriptor = addPropertyDescriptorForMember( method );
			}
			propertyDescriptor.addConstraintDescriptor( constraintDescriptor );
		}
		return metadata;
	}

	/**
	 * Finds all constraint annotations defined for the given field and returns them in a list of
	 * constraint descriptors.
	 *
	 * @param field The field to check for constraints annotations.
	 *
	 * @return A list of constraint descriptors for all constraint specified on the given field.
	 */
	private List<ConstraintDescriptorImpl> findFieldLevelConstraints(Field field) {
		List<ConstraintDescriptorImpl> metadata = new ArrayList<ConstraintDescriptorImpl>();
		for ( Annotation annotation : field.getAnnotations() ) {
			metadata.addAll( findConstraintAnnotations( annotation ) );
		}

		String fieldName = field.getName();
		for ( ConstraintDescriptorImpl constraintDescriptor : metadata ) {
			PropertyDescriptorImpl propertyDescriptor = ( PropertyDescriptorImpl ) propertyDescriptors.get( fieldName );
			if ( propertyDescriptor == null ) {
				propertyDescriptor = addPropertyDescriptorForMember( field );
			}
			propertyDescriptor.addConstraintDescriptor( constraintDescriptor );
		}
		return metadata;
	}

	public Class<T> getBeanClass() {
		return beanClass;
	}

	public BeanDescriptor getBeanDescriptor() {
		return beanDescriptor;
	}

	public List<Field> getCascadedFields() {
		return cascadedFields;
	}

	public List<Method> getCascadedMethods() {
		return cascadedMethods;
	}

	public List<Member> getCascadedMembers() {
		List<Member> cascadedMembers = new ArrayList<Member>();
		cascadedMembers.addAll( getCascadedFields() );
		cascadedMembers.addAll( getCascadedMethods() );
		return cascadedMembers;
	}

	public List<MetaConstraint<T, ?>> geMetaConstraintList() {
		return metaConstraintList;
	}

	public PropertyDescriptor getPropertyDescriptor(String property) {
		return propertyDescriptors.get( property );
	}

	public List<Class<?>> getDefaultGroupSequence() {
		return defaultGroupSequence;
	}

	public Set<String> getConstrainedProperties() {
		return Collections.unmodifiableSet( propertyDescriptors.keySet() );
	}
}