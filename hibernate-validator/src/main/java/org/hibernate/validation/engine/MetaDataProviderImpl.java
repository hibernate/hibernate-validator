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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Constraint;
import javax.validation.ConstraintFactory;
import javax.validation.ConstraintValidator;
import javax.validation.ElementDescriptor;
import javax.validation.GroupSequence;
import javax.validation.GroupSequences;
import javax.validation.Valid;
import javax.validation.ValidationException;

import org.slf4j.Logger;

import org.hibernate.validation.impl.ConstraintDescriptorImpl;
import org.hibernate.validation.impl.ConstraintFactoryImpl;
import org.hibernate.validation.impl.ElementDescriptorImpl;
import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.ReflectionHelper;


/**
 * This class encapsulates all meta data needed for validation. Implementations of <code>Validator</code> interface can
 * instantiate an instance of this class and delegate the metadata extraction to it.
 *
 * @author Hardy Ferentschik
 */

public class MetaDataProviderImpl<T> implements MetaDataProvider<T> {

	private static final Logger log = LoggerFactory.make();

	/**
	 * The root bean class for this validator.
	 */
	private final Class<T> beanClass;

	/**
	 * The main element descriptor for <code>beanClass</code>.
	 */
	private ElementDescriptorImpl beanDescriptor;

	/**
	 * List of constraints.
	 */
	private List<MetaConstraint> metaConstraintList = new ArrayList<MetaConstraint>();

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
	 * FIXME This model is problematic as you can have conflicting names for fields and methods
	 */
	private Map<String, ElementDescriptor> propertyDescriptors = new HashMap<String, ElementDescriptor>();

	/**
	 * Factory to create acutal constraint instances from the annotated fields/method/class.
	 */
	private ConstraintFactory constraintFactory = new ConstraintFactoryImpl();

	/**
	 * Maps group sequence names to the list of group/sequence names.
	 */
	private Map<String, List<String>> groupSequences = new HashMap<String, List<String>>();

	public MetaDataProviderImpl(Class<T> beanClass, ConstraintFactory constraintFactory) {
		this.beanClass = beanClass;
		this.constraintFactory = constraintFactory;
		createMetaData();
	}

	/**
	 * Create bean desciptor, find all classes/subclasses/interfaces which have to be taken in consideration
	 * for this validator and create meta data.
	 */
	private void createMetaData() {
		beanDescriptor = new ElementDescriptorImpl( beanClass, false, "" );
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
		initGroupSequences( clazz );
		initClassConstraints( clazz );
		initMethodConstraints( clazz );
		initFieldConstraints( clazz );
	}

	private void initGroupSequences(Class<?> clazz) {
		GroupSequence groupSequenceAnnotation = clazz.getAnnotation( GroupSequence.class );
		if ( groupSequenceAnnotation != null ) {
			addGroupSequence( groupSequenceAnnotation );
		}

		GroupSequences groupSequencesAnnotation = clazz.getAnnotation( GroupSequences.class );
		if ( groupSequencesAnnotation != null ) {
			for ( GroupSequence group : groupSequencesAnnotation.value() ) {
				addGroupSequence( group );
			}
		}

		for ( Map.Entry<String, List<String>> mapEntry : groupSequences.entrySet() ) {
			List<String> groupNames = mapEntry.getValue();
			List<String> expandedGroupNames = new ArrayList<String>();
			for ( String groupName : groupNames ) {
				expandedGroupNames.addAll( expandGroupSequenceNames( groupName ) );
			}
			groupSequences.put( mapEntry.getKey(), expandedGroupNames );
		}
		if ( log.isDebugEnabled() && !groupSequences.isEmpty() ) {
			log.debug( "Expanded groups sequences: {}", groupSequences );
		}
	}

	private List<String> expandGroupSequenceNames(String group) {
		List<String> groupList = new ArrayList<String>();
		if ( groupSequences.containsKey( group ) ) {
			for ( String s : groupSequences.get( group ) ) {
				groupList.addAll( expandGroupSequenceNames( s ) );
			}
		}
		else {
			groupList.add( group );
		}
		if ( log.isTraceEnabled() ) {
			log.trace( "Expanded {} to {}", group, groupList.toString() );
		}
		return groupList;
	}

	private void addGroupSequence(GroupSequence groupSequence) {
		if ( groupSequences.containsKey( groupSequence.name() ) ) {
			throw new ValidationException( "Encountered duplicate sequence name: " + groupSequence.name() );
		}
		groupSequences.put( groupSequence.name(), Arrays.asList( groupSequence.sequence() ) );
	}

	private void initFieldConstraints(Class clazz) {
		for ( Field field : clazz.getDeclaredFields() ) {
			List<ConstraintDescriptorImpl> fieldMetadata = findFieldLevelConstraints( field );
			for ( ConstraintDescriptorImpl constraintDescription : fieldMetadata ) {
				ReflectionHelper.setAccessibility( field );
				MetaConstraint metaConstraint = new MetaConstraint( field, constraintDescription );
				metaConstraintList.add( metaConstraint );
			}
			if ( field.isAnnotationPresent( Valid.class ) ) {
				cascadedFields.add( field );
			}
		}
	}

	private void initMethodConstraints(Class clazz) {
		for ( Method method : clazz.getDeclaredMethods() ) {
			List<ConstraintDescriptorImpl> methodMetadata = findMethodLevelConstraints( method );
			for ( ConstraintDescriptorImpl constraintDescription : methodMetadata ) {
				ReflectionHelper.setAccessibility( method );
				MetaConstraint metaConstraint = new MetaConstraint( method, constraintDescription );
				metaConstraintList.add( metaConstraint );
			}
			if ( method.isAnnotationPresent( Valid.class ) ) {
				cascadedMethods.add( method );
			}
		}
	}

	private void initClassConstraints(Class clazz) {
		List<ConstraintDescriptorImpl> classMetadata = findClassLevelConstraints( clazz );
		for ( ConstraintDescriptorImpl constraintDescription : classMetadata ) {
			MetaConstraint metaConstraint = new MetaConstraint( clazz, constraintDescription );
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
		if ( ReflectionHelper.isConstraintAnnotation( annotation ) ||
				ReflectionHelper.isBuiltInConstraintAnnotation( annotation) ) {
			constraints.add( annotation );
		}

		// check if we have a multi value constraint
		constraints.addAll( ReflectionHelper.getMultiValueConstraints( annotation ) );

		for ( Annotation constraint : constraints ) {
			if ( ReflectionHelper.isBuiltInConstraintAnnotation( constraint ) ) {
				Class constraintClass = ReflectionHelper.getBuiltInConstraint( constraint );
				final ConstraintDescriptorImpl constraintDescriptor = buildConstraintDescriptor(
						constraint, constraintClass
				);
				constraintDescriptors.add( constraintDescriptor );
				continue;
			}

			if ( ReflectionHelper.isConstraintAnnotation( constraint ) ) {
				ConstraintValidator constraintValidator = constraint.annotationType()
						.getAnnotation( ConstraintValidator.class );
				final ConstraintDescriptorImpl constraintDescriptor = buildConstraintDescriptor(
						constraint, constraintValidator.value()
				);
				constraintDescriptors.add( constraintDescriptor );
			}
		}
		return constraintDescriptors;
	}

	@SuppressWarnings("unchecked")
	private <A extends Annotation> ConstraintDescriptorImpl buildConstraintDescriptor(A annotation, Class constraintClass) {
		String[] groups = ReflectionHelper.getAnnotationParameter( annotation, "groups", String[].class );
		for ( String groupName : groups ) {
			if ( groupSequences.containsKey( groupName ) ) {
				throw new ValidationException( groupName + " is illegally used as group and sequence name." );
			}
		}

		Constraint<A> constraint;
		try {
			//unchecked
			constraint = constraintFactory.getInstance( constraintClass );
		}
		catch ( RuntimeException e ) {
			throw new ValidationException( "Unable to instantiate " + constraintClass, e );
		}

		try {
			constraint.initialize( annotation );
		}
		catch ( RuntimeException e ) {
			throw new ValidationException( "Unable to intialize " + constraint.getClass().getName(), e );
		}

		return new ConstraintDescriptorImpl( annotation, groups, constraint, constraintClass );
	}

	/**
	 * Finds all constraint annotations defined for the given class and returns them in a list of
	 * constraint descriptors.
	 *
	 * @param beanClass The class to check for constraints annotations.
	 *
	 * @return A list of constraint descriptors for all constraint specified on the given class.
	 *
	 * @todo inject XML data here, probably externalizing the process
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
	 *
	 * @todo inject XML data here, probably externalizing the process
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
						"Annoated methods must follow the JavaBeans naming convention. " + method.getName() + "() does not."
				);
			}
			ElementDescriptorImpl elementDescriptor = ( ElementDescriptorImpl ) propertyDescriptors.get( methodName );
			if ( elementDescriptor == null ) {
				elementDescriptor = new ElementDescriptorImpl(
						method.getReturnType(),
						method.isAnnotationPresent( Valid.class ),
						methodName
				);
				propertyDescriptors.put( methodName, elementDescriptor );
			}
			elementDescriptor.addConstraintDescriptor( constraintDescriptor );
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
	 *
	 * @todo inject XML data here, probably externalizing the process
	 */
	private List<ConstraintDescriptorImpl> findFieldLevelConstraints(Field field) {
		List<ConstraintDescriptorImpl> metadata = new ArrayList<ConstraintDescriptorImpl>();
		for ( Annotation annotation : field.getAnnotations() ) {
			metadata.addAll( findConstraintAnnotations( annotation ) );
		}

		String fieldName = field.getName();
		for ( ConstraintDescriptorImpl constraintDescriptor : metadata ) {
			ElementDescriptorImpl elementDescriptor = ( ElementDescriptorImpl ) propertyDescriptors.get( fieldName );
			if ( elementDescriptor == null ) {
				elementDescriptor = new ElementDescriptorImpl(
						field.getType(),
						field.isAnnotationPresent( Valid.class ),
						fieldName
				);
				propertyDescriptors.put( field.getName(), elementDescriptor );
			}
			elementDescriptor.addConstraintDescriptor( constraintDescriptor );
		}
		return metadata;
	}

	public Class<T> getBeanClass() {
		return beanClass;
	}

	public ElementDescriptor getBeanDescriptor() {
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

	public Map<String, List<String>> getGroupSequences() {
		return groupSequences;
	}

	public List<MetaConstraint> getConstraintMetaDataList() {
		return metaConstraintList;
	}

	public Map<String, ElementDescriptor> getPropertyDescriptors() {
		return propertyDescriptors;
	}

	public ElementDescriptor getPropertyDescriptors(String property) {
		return propertyDescriptors.get( property );
	}
}