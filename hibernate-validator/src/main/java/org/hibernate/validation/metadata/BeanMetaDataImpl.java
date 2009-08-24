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
package org.hibernate.validation.metadata;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.GroupDefinitionException;
import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.slf4j.Logger;

import org.hibernate.validation.util.GetDeclaredFields;
import org.hibernate.validation.util.GetDeclaredMethods;
import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.ReflectionHelper;
import org.hibernate.validation.util.SetAccessibility;


/**
 * This class encapsulates all meta data needed for validation. Implementations of {@code Validator} interface can
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
	 * Map of all direct constraints which belong to the entity {@code beanClass}. The constraints are mapped to the class
	 * (eg super class or interface) in which they are defined.
	 */
	private Map<Class<?>, List<MetaConstraint<T, ? extends Annotation>>> metaConstraints = new HashMap<Class<?>, List<MetaConstraint<T, ? extends Annotation>>>();

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
	 * Object keeping track of all constraints.
	 */
	private final ConstraintHelper constraintHelper;

	//updated on the fly, needs to be thread safe
	//property name
	private final Set<String> propertyNames = new HashSet<String>( 30 );

	public BeanMetaDataImpl(Class<T> beanClass, ConstraintHelper constraintHelper) {
		this(
				beanClass,
				constraintHelper,
				new AnnotationIgnores()
		);
	}

	public BeanMetaDataImpl(Class<T> beanClass, ConstraintHelper constraintHelper, AnnotationIgnores annotationIgnores) {
		this.beanClass = beanClass;
		this.constraintHelper = constraintHelper;
		createMetaData( annotationIgnores );
	}

	public Class<T> getBeanClass() {
		return beanClass;
	}

	public BeanDescriptor getBeanDescriptor() {
		return beanDescriptor;
	}

	public List<Member> getCascadedMembers() {
		return Collections.unmodifiableList( cascadedMembers );
	}

	public Map<Class<?>, List<MetaConstraint<T, ? extends Annotation>>> geMetaConstraintsAsMap() {
		return Collections.unmodifiableMap( metaConstraints );
	}

	public List<MetaConstraint<T, ? extends Annotation>> geMetaConstraintsAsList() {
		List<MetaConstraint<T, ? extends Annotation>> constraintList = new ArrayList<MetaConstraint<T, ? extends Annotation>>();
		for ( List<MetaConstraint<T, ? extends Annotation>> list : metaConstraints.values() ) {
			constraintList.addAll( list );
		}
		return Collections.unmodifiableList( constraintList );
	}

	public void addMetaConstraint(Class<?> clazz, MetaConstraint<T, ? extends Annotation> metaConstraint) {
		// first we add the meta constraint to our meta constraint map
		List<MetaConstraint<T, ? extends Annotation>> constraintList;
		if ( !metaConstraints.containsKey( clazz ) ) {
			constraintList = new ArrayList<MetaConstraint<T, ? extends Annotation>>();
			metaConstraints.put( clazz, constraintList );
		}
		else {
			constraintList = metaConstraints.get( clazz );
		}
		constraintList.add( metaConstraint );

		// but we also have to update the descriptors exposing the BV metadata API
		if ( metaConstraint.getElementType() == ElementType.TYPE ) {
			beanDescriptor.addConstraintDescriptor( metaConstraint.getDescriptor() );
		}
		else {
			PropertyDescriptorImpl propertyDescriptor = ( PropertyDescriptorImpl ) propertyDescriptors.get(
					metaConstraint.getPropertyName()
			);
			if ( propertyDescriptor == null ) {
				Member member = metaConstraint.getMember();
				propertyDescriptor = addPropertyDescriptorForMember( member, isValidAnnotationPresent( member ) );
			}
			propertyDescriptor.addConstraintDescriptor( metaConstraint.getDescriptor() );
		}
	}

	public void addCascadedMember(Member member) {
		cascadedMembers.add( member );
		addPropertyDescriptorForMember( member, true );
	}

	public PropertyDescriptor getPropertyDescriptor(String property) {
		return propertyDescriptors.get( property );
	}

	public boolean isPropertyPresent(String name) {
		return propertyNames.contains( name );  //To change body of implemented methods use File | Settings | File Templates.
	}

	public List<Class<?>> getDefaultGroupSequence() {
		return Collections.unmodifiableList( defaultGroupSequence );
	}

	public boolean defaultGroupSequenceIsRedefined() {
		return defaultGroupSequence.size() > 1;
	}

	public void setDefaultGroupSequence(List<Class<?>> groupSequence) {
		defaultGroupSequence = new ArrayList<Class<?>>();
		boolean groupSequenceContainsDefault = false;
		for ( Class<?> group : groupSequence ) {
			if ( group.getName().equals( beanClass.getName() ) ) {
				defaultGroupSequence.add( Default.class );
				groupSequenceContainsDefault = true;
			}
			else if ( group.getName().equals( Default.class.getName() ) ) {
				throw new GroupDefinitionException( "'Default.class' cannot appear in default group sequence list." );
			}
			else {
				defaultGroupSequence.add( group );
			}
		}
		if ( !groupSequenceContainsDefault ) {
			throw new GroupDefinitionException( beanClass.getName() + " must be part of the redefined default group sequence." );
		}
		if ( log.isTraceEnabled() ) {
			log.trace(
					"Members of the default group sequence for bean {} are: {}",
					beanClass.getName(),
					defaultGroupSequence
			);
		}
	}

	public Set<PropertyDescriptor> getConstrainedProperties() {
		return Collections.unmodifiableSet( new HashSet<PropertyDescriptor>( propertyDescriptors.values() ) );
	}

	/**
	 * Create bean desciptor, find all classes/subclasses/interfaces which have to be taken in consideration
	 * for this validator and create meta data.
	 *
	 * @param annotationIgnores Datastructure keeping track on which annotation should be ignored.
	 */
	private void createMetaData(AnnotationIgnores annotationIgnores) {
		beanDescriptor = new BeanDescriptorImpl<T>( this );
		initDefaultGroupSequence();
		List<Class> classes = new ArrayList<Class>();
		computeClassHierarchy( beanClass, classes );
		for ( Class current : classes ) {
			initClass( current, annotationIgnores );
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

	private void initClass(Class clazz, AnnotationIgnores annotationIgnores) {
		initClassConstraints( clazz, annotationIgnores );
		initMethodConstraints( clazz, annotationIgnores );
		initFieldConstraints( clazz, annotationIgnores );
	}

	/**
	 * Checks whether there is a default group sequence defined for this class.
	 * See HV-113.
	 */
	private void initDefaultGroupSequence() {
		List<Class<?>> groupSequence = new ArrayList<Class<?>>();
		GroupSequence groupSequenceAnnotation = beanClass.getAnnotation( GroupSequence.class );
		if ( groupSequenceAnnotation == null ) {
			groupSequence.add( beanClass );
		}
		else {
			groupSequence.addAll( Arrays.asList( groupSequenceAnnotation.value() ) );
		}
		setDefaultGroupSequence( groupSequence );
	}

	private void initFieldConstraints(Class<?> clazz, AnnotationIgnores annotationIgnores) {
		GetDeclaredFields action = GetDeclaredFields.action( clazz );
		final Field[] fields;
		if ( System.getSecurityManager() != null ) {
			fields = AccessController.doPrivileged( action );
		}
		else {
			fields = action.run();
		}
		for ( Field field : fields ) {
			// HV-172
			if ( Modifier.isStatic( field.getModifiers() ) ) {
				continue;
			}

			if ( annotationIgnores.isIgnoreAnnotations( field ) ) {
				continue;
			}

			String name = ReflectionHelper.getPropertyName( field );
			if ( name != null ) {
				propertyNames.add( name );
			}

			List<ConstraintDescriptorImpl<?>> fieldMetadata = findConstraints( field );
			for ( ConstraintDescriptorImpl<?> constraintDescription : fieldMetadata ) {
				setAccessibility( field );
				MetaConstraint<T, ?> metaConstraint = createMetaConstraint( field, constraintDescription );
				addMetaConstraint( clazz, metaConstraint );
			}

			if ( field.isAnnotationPresent( Valid.class ) ) {
				setAccessibility( field );
				addCascadedMember( field );
			}
		}
	}

	private void setAccessibility(Member member) {
		SetAccessibility action = SetAccessibility.action( member );
		if ( System.getSecurityManager() != null ) {
			AccessController.doPrivileged( action );
		}
		else {
			action.run();
		}
	}

	private void initMethodConstraints(Class<?> clazz, AnnotationIgnores annotationIgnores) {
		GetDeclaredMethods action = GetDeclaredMethods.action( clazz );
		final Method[] declaredMethods;
		if ( System.getSecurityManager() != null ) {
			declaredMethods = AccessController.doPrivileged( action );
		}
		else {
			declaredMethods = action.run();
		}

		for ( Method method : declaredMethods ) {
			// HV-172
			if ( Modifier.isStatic( method.getModifiers() ) ) {
				continue;
			}

			if ( annotationIgnores.isIgnoreAnnotations( method ) ) {
				continue;
			}

			String name = ReflectionHelper.getPropertyName( method );
			if ( name != null ) {
				propertyNames.add( name );
			}

			List<ConstraintDescriptorImpl<?>> methodMetadata = findConstraints( method );
			for ( ConstraintDescriptorImpl<?> constraintDescription : methodMetadata ) {
				setAccessibility( method );
				MetaConstraint<T, ?> metaConstraint = createMetaConstraint( method, constraintDescription );
				addMetaConstraint( clazz, metaConstraint );
			}
			if ( method.isAnnotationPresent( Valid.class ) ) {
				setAccessibility( method );
				addCascadedMember( method );
			}
		}
	}

	private PropertyDescriptorImpl addPropertyDescriptorForMember(Member member, boolean isCascaded) {
		String name = ReflectionHelper.getPropertyName( member );
		PropertyDescriptorImpl propertyDescriptor = ( PropertyDescriptorImpl ) propertyDescriptors.get(
				name
		);
		if ( propertyDescriptor == null ) {
			propertyDescriptor = new PropertyDescriptorImpl(
					ReflectionHelper.getType( member ),
					isCascaded,
					name
			);
			propertyDescriptors.put( name, propertyDescriptor );
		}
		return propertyDescriptor;
	}

	private boolean isValidAnnotationPresent(Member member) {
		return ( ( AnnotatedElement ) member ).isAnnotationPresent( Valid.class );
	}

	private void initClassConstraints(Class<?> clazz, AnnotationIgnores annotationIgnores) {
		if ( annotationIgnores.isIgnoreAnnotations( clazz ) ) {
			return;
		}
		List<ConstraintDescriptorImpl<?>> classMetadata = findClassLevelConstraints( clazz );
		for ( ConstraintDescriptorImpl<?> constraintDescription : classMetadata ) {
			MetaConstraint<T, ?> metaConstraint = createMetaConstraint( constraintDescription );
			addMetaConstraint( clazz, metaConstraint );
		}
	}

	private <A extends Annotation> MetaConstraint<T, ?> createMetaConstraint(ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<T, A>( beanClass, descriptor );
	}

	private <A extends Annotation> MetaConstraint<T, ?> createMetaConstraint(Member m, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<T, A>( m, beanClass, descriptor );
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
	private <A extends Annotation> List<ConstraintDescriptorImpl<?>> findConstraintAnnotations(Class<?> clazz, A annotation) {
		List<ConstraintDescriptorImpl<?>> constraintDescriptors = new ArrayList<ConstraintDescriptorImpl<?>>();

		List<Annotation> constraints = new ArrayList<Annotation>();
		if ( constraintHelper.isConstraintAnnotation( annotation ) ||
				constraintHelper.isBuiltinConstraint( annotation.annotationType() ) ) {
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
		ConstraintDescriptorImpl constraintDescriptor;
		if ( clazz.isInterface() && !clazz.equals( beanClass ) ) {
			constraintDescriptor = new ConstraintDescriptorImpl( annotation, constraintHelper, clazz );
		}
		else {
			constraintDescriptor = new ConstraintDescriptorImpl( annotation, constraintHelper );
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
	private List<ConstraintDescriptorImpl<?>> findClassLevelConstraints(Class<?> beanClass) {
		List<ConstraintDescriptorImpl<?>> metadata = new ArrayList<ConstraintDescriptorImpl<?>>();
		for ( Annotation annotation : beanClass.getAnnotations() ) {
			metadata.addAll( findConstraintAnnotations( beanClass, annotation ) );
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
	private List<ConstraintDescriptorImpl<?>> findConstraints(Member member) {
		assert member instanceof Field || member instanceof Method;

		List<ConstraintDescriptorImpl<?>> metadata = new ArrayList<ConstraintDescriptorImpl<?>>();
		for ( Annotation annotation : ( ( AnnotatedElement ) member ).getAnnotations() ) {
			metadata.addAll( findConstraintAnnotations( member.getDeclaringClass(), annotation ) );
		}

		return metadata;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "BeanMetaDataImpl" );
		sb.append( "{beanClass=" ).append( beanClass );
		sb.append( ", beanDescriptor=" ).append( beanDescriptor );
		sb.append( ", metaConstraints=" ).append( metaConstraints );
		sb.append( ", cascadedMembers=" ).append( cascadedMembers );
		sb.append( ", propertyDescriptors=" ).append( propertyDescriptors );
		sb.append( ", defaultGroupSequence=" ).append( defaultGroupSequence );
		sb.append( ", constraintHelper=" ).append( constraintHelper );
		sb.append( '}' );
		return sb.toString();
	}
}