// $Id$
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
package org.hibernate.validator.engine;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ConfigurationState;

import org.hibernate.validator.cfg.ConstraintDefinition;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.metadata.AnnotationIgnores;
import org.hibernate.validator.metadata.BeanMetaDataCache;
import org.hibernate.validator.metadata.BeanMetaDataImpl;
import org.hibernate.validator.metadata.ConstraintDescriptorImpl;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.metadata.ConstraintOrigin;
import org.hibernate.validator.metadata.MetaConstraint;
import org.hibernate.validator.util.ReflectionHelper;
import org.hibernate.validator.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.xml.XmlMappingParser;

/**
 * Factory returning initialized {@code Validator} instances. This is Hibernate Validator's default
 * implementation of the {@code ValidatorFactory} interface.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ValidatorFactoryImpl implements ValidatorFactory {

	private final MessageInterpolator messageInterpolator;
	private final TraversableResolver traversableResolver;
	private final ConstraintValidatorFactory constraintValidatorFactory;
	private final ConstraintHelper constraintHelper;

	/**
	 * Used to cache the constraint meta data for validated entities
	 */
	private final BeanMetaDataCache beanMetaDataCache;

	public ValidatorFactoryImpl(ConfigurationState configurationState) {

		this.messageInterpolator = configurationState.getMessageInterpolator();
		this.constraintValidatorFactory = configurationState.getConstraintValidatorFactory();
		this.traversableResolver = configurationState.getTraversableResolver();
		this.constraintHelper = new ConstraintHelper();
		this.beanMetaDataCache = new BeanMetaDataCache();

		// HV-302; don't load XmlMappingParser if not necessary
		if ( !configurationState.getMappingStreams().isEmpty() ) {
			initXmlConfiguration( configurationState.getMappingStreams() );
		}

		if ( configurationState instanceof ConfigurationImpl ) {
			ConfigurationImpl hibernateSpecificConfig = ( ConfigurationImpl ) configurationState;
			if ( hibernateSpecificConfig.getMapping() != null ) {
				initProgrammaticConfiguration( hibernateSpecificConfig.getMapping() );
			}
		}
	}

	public Validator getValidator() {
		return usingContext().getValidator();
	}

	public MessageInterpolator getMessageInterpolator() {
		return messageInterpolator;
	}

	public TraversableResolver getTraversableResolver() {
		return traversableResolver;
	}

	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return constraintValidatorFactory;
	}

	public <T> T unwrap(Class<T> type) {
		throw new ValidationException( "Type " + type + " not supported" );
	}

	public ValidatorContext usingContext() {
		return new ValidatorContextImpl(
				constraintValidatorFactory,
				messageInterpolator,
				traversableResolver,
				constraintHelper,
				beanMetaDataCache
		);
	}

	/**
	 * Reads the configuration from {@code mapping} and creates the appropriate meta-data structures.
	 *
	 * @param mapping The constraint configuration created via the programmatic API.
	 */
	private <A extends Annotation, T> void initProgrammaticConfiguration(ConstraintMapping mapping) {
		Map<Class<?>, List<ConstraintDefinition<?>>> configData = mapping.getConfigData();

		for ( Class<?> clazz : mapping.getConfigData().keySet() ) {
			@SuppressWarnings("unchecked")
			Class<T> beanClass = ( Class<T> ) clazz;

			// for each configured entity we have to check whether any of the interfaces or super classes is configured
			// vua the programmatic api as well
			List<Class<?>> classes = ReflectionHelper.computeClassHierarchy( beanClass );

			Map<Class<?>, List<MetaConstraint<T, ?>>> constraints = createEmptyConstraintMap();
			List<Member> cascadedMembers = new ArrayList<Member>();

			for ( Class<?> classInHierarchy : classes ) {
				// if the programmatic config contains constraints for the class in the hierarchy create a meta constraint
				if ( mapping.getConfigData().keySet().contains( classInHierarchy ) ) {
					addProgrammaticConfiguredConstraints(
							mapping.getConfigData().get( classInHierarchy ), beanClass, classInHierarchy, constraints
					);
				}
			}

			BeanMetaDataImpl<T> metaData = new BeanMetaDataImpl<T>(
					beanClass,
					constraintHelper,
					new ArrayList<Class<?>>(),
					constraints,
					new ArrayList<Member>(),
					new AnnotationIgnores(),
					beanMetaDataCache
			);

			beanMetaDataCache.addBeanMetaData( beanClass, metaData );
		}
	}

	private <T> void initXmlConfiguration(Set<InputStream> mappingStreams) {

		XmlMappingParser mappingParser = new XmlMappingParser( constraintHelper );
		mappingParser.parse( mappingStreams );

		Set<Class<?>> xmlConfiguredClasses = mappingParser.getXmlConfiguredClasses();
		AnnotationIgnores annotationIgnores = mappingParser.getAnnotationIgnores();
		for ( Class<?> clazz : xmlConfiguredClasses ) {
			@SuppressWarnings("unchecked")
			Class<T> beanClass = ( Class<T> ) clazz;

			List<Class<?>> classes = ReflectionHelper.computeClassHierarchy( beanClass );
			Map<Class<?>, List<MetaConstraint<T, ?>>> constraints = createEmptyConstraintMap();
			List<Member> cascadedMembers = new ArrayList<Member>();
			// we need to collect all constraints which apply for a single class. Due to constraint inheritance
			// some constraints might be configured in super classes or interfaces. The xml configuration does not
			// imply any order so we have to check whether any of the super classes or interfaces of a given bean has
			// as well been configured via xml
			for ( Class<?> classInHierarchy : classes ) {
				if ( xmlConfiguredClasses.contains( classInHierarchy ) ) {
					addXmlConfiguredConstraints( mappingParser, beanClass, classInHierarchy, constraints );
					addXmlCascadedMember( mappingParser, classInHierarchy, cascadedMembers );
				}
			}

			BeanMetaDataImpl<T> metaData = new BeanMetaDataImpl<T>(
					beanClass,
					constraintHelper,
					mappingParser.getDefaultSequenceForClass( beanClass ),
					constraints,
					cascadedMembers,
					annotationIgnores,
					beanMetaDataCache
			);

			beanMetaDataCache.addBeanMetaData( beanClass, metaData );
		}
	}

	@SuppressWarnings("unchecked")
	private <T, A extends Annotation> void addXmlConfiguredConstraints(XmlMappingParser mappingParser,
																	   Class<T> rootClass,
																	   Class<?> hierarchyClass, Map<Class<?>, List<MetaConstraint<T, ?>>> constraints) {
		for ( MetaConstraint<?, ? extends Annotation> constraint : mappingParser.getConstraintsForClass( hierarchyClass ) ) {
			ConstraintOrigin definedIn = definedIn( rootClass, hierarchyClass );
			ConstraintDescriptorImpl<A> descriptor = new ConstraintDescriptorImpl<A>(
					( A ) constraint.getDescriptor().getAnnotation(),
					constraintHelper,
					constraint.getElementType(),
					definedIn
			);
			MetaConstraint<T, A> newMetaConstraint = new MetaConstraint<T, A>(
					rootClass, constraint.getMember(), descriptor
			);

			addConstraintToMap( hierarchyClass, newMetaConstraint, constraints );
		}
	}

	@SuppressWarnings("unchecked")
	private <T, A extends Annotation> void addProgrammaticConfiguredConstraints(List<ConstraintDefinition<?>> definitions,
																				Class<T> rootClass, Class<?> hierarchyClass,
																				Map<Class<?>, List<MetaConstraint<T, ?>>> constraints) {
		for ( ConstraintDefinition<?> config : definitions ) {
			A annotation = (A) createAnnotationProxy( config );
			ConstraintOrigin definedIn = definedIn( rootClass, hierarchyClass );
			ConstraintDescriptorImpl<A> constraintDescriptor = new ConstraintDescriptorImpl<A>(
					annotation, constraintHelper, config.getElementType(), definedIn
			);

			Member member = ReflectionHelper.getMember(
					config.getBeanType(), config.getProperty(), config.getElementType()
			);

			MetaConstraint<T, ?> metaConstraint = new MetaConstraint(
					config.getBeanType(), member, constraintDescriptor
			);
			addConstraintToMap( hierarchyClass, metaConstraint, constraints );
		}
	}

	@SuppressWarnings("unchecked")
	private <T, A extends Annotation> void addConstraintToMap(Class<?> hierarchyClass,
															  MetaConstraint<T, A> constraint,
															  Map<Class<?>, List<MetaConstraint<T, ?>>> constraints) {
		List<MetaConstraint<T, ?>> constraintList = constraints.get( hierarchyClass );
		if ( constraintList == null ) {
			constraintList = new ArrayList<MetaConstraint<T, ?>>();
			constraints.put( hierarchyClass, constraintList );
		}
		constraintList.add( constraint );
	}

	private void addXmlCascadedMember(XmlMappingParser mappingParser,
									  Class<?> hierarchyClass,
									  List<Member> cascadedMembers) {
		for ( Member m : mappingParser.getCascadedMembersForClass( hierarchyClass ) ) {
			cascadedMembers.add( m );
		}
	}

	/**
	 * @param rootClass The root class. That is the class for which we currently create a  {@code BeanMetaData}
	 * @param hierarchyClass The class on which the current constraint is defined on
	 *
	 * @return Returns {@code ConstraintOrigin.DEFINED_LOCALLY} if the constraint was defined on the root bean,
	 *         {@code ConstraintOrigin.DEFINED_IN_HIERARCHY} otherwise.
	 */
	private ConstraintOrigin definedIn(Class<?> rootClass, Class<?> hierarchyClass) {
		if ( hierarchyClass.equals( rootClass ) ) {
			return ConstraintOrigin.DEFINED_LOCALLY;
		}
		else {
			return ConstraintOrigin.DEFINED_IN_HIERARCHY;
		}
	}

	@SuppressWarnings("unchecked")
	private <A extends Annotation> Annotation createAnnotationProxy(ConstraintDefinition<?> config) {
		Class<A> constraintType = (Class<A>) config.getConstraintType();
		AnnotationDescriptor<A> annotationDescriptor = new AnnotationDescriptor<A>( constraintType );
		for ( Map.Entry<String, Object> parameter : config.getParameters().entrySet() ) {
			annotationDescriptor.setValue( parameter.getKey(), parameter.getValue() );
		}

		A annotation;
		try {
			annotation = AnnotationFactory.create( annotationDescriptor );
		}
		catch ( RuntimeException e ) {
			throw new ValidationException(
					"Unable to create annotation for configured constraint: " + e.getMessage(), e
			);
		}
		return annotation;
	}

	private <T> Map<Class<?>, List<MetaConstraint<T, ?>>> createEmptyConstraintMap() {
		return new HashMap<Class<?>, List<MetaConstraint<T, ?>>>();
	}
}
