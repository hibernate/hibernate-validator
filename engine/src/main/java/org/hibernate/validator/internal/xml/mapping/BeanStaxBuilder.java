/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.properties.javabean.JavaBeanConstructor;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.properties.javabean.JavaBeanMethod;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.xml.AbstractStaxBuilder;

/**
 * Builder for definition of all bean constraints.
 *
 * @author Marko Bekhta
 */
class BeanStaxBuilder extends AbstractStaxBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final QName IGNORE_ANNOTATIONS_QNAME = new QName( "ignore-annotations" );
	private static final QName CLASS_QNAME = new QName( "class" );
	private static final String BEAN_QNAME_LOCAL_PART = "bean";

	private final ClassLoadingHelper classLoadingHelper;
	private final ConstraintCreationContext constraintCreationContext;
	private final DefaultPackageStaxBuilder defaultPackageStaxBuilder;
	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;
	private final Map<Class<?>, List<Class<?>>> defaultSequences;

	protected String className;
	protected Optional<Boolean> ignoreAnnotations;
	private ClassConstraintTypeStaxBuilder classConstraintTypeStaxBuilder;
	private final List<ConstrainedFieldStaxBuilder> constrainedFieldStaxBuilders;
	private final List<ConstrainedGetterStaxBuilder> constrainedGetterStaxBuilders;
	private final List<ConstrainedMethodStaxBuilder> constrainedMethodStaxBuilders;
	private final List<ConstrainedConstructorStaxBuilder> constrainedConstructorStaxBuilders;

	BeanStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintCreationContext constraintCreationContext,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder, AnnotationProcessingOptionsImpl annotationProcessingOptions,
			Map<Class<?>, List<Class<?>>> defaultSequences) {
		this.classLoadingHelper = classLoadingHelper;
		this.defaultPackageStaxBuilder = defaultPackageStaxBuilder;
		this.constraintCreationContext = constraintCreationContext;

		this.annotationProcessingOptions = annotationProcessingOptions;
		this.defaultSequences = defaultSequences;

		this.constrainedFieldStaxBuilders = new ArrayList<>();
		this.constrainedGetterStaxBuilders = new ArrayList<>();
		this.constrainedMethodStaxBuilders = new ArrayList<>();
		this.constrainedConstructorStaxBuilders = new ArrayList<>();
	}

	@Override
	protected String getAcceptableQName() {
		return BEAN_QNAME_LOCAL_PART;
	}

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
		this.className = readAttribute( xmlEvent.asStartElement(), CLASS_QNAME ).get();
		this.ignoreAnnotations = readAttribute( xmlEvent.asStartElement(), IGNORE_ANNOTATIONS_QNAME ).map( Boolean::parseBoolean );
		ConstrainedFieldStaxBuilder fieldStaxBuilder = getNewConstrainedFieldStaxBuilder();
		ConstrainedGetterStaxBuilder getterStaxBuilder = getNewConstrainedGetterStaxBuilder();
		ConstrainedMethodStaxBuilder methodStaxBuilder = getNewConstrainedMethodStaxBuilder();
		ConstrainedConstructorStaxBuilder constructorStaxBuilder = getNewConstrainedConstructorStaxBuilder();

		ClassConstraintTypeStaxBuilder localClassConstraintTypeStaxBuilder = new ClassConstraintTypeStaxBuilder(
				classLoadingHelper, constraintCreationContext,
				defaultPackageStaxBuilder, annotationProcessingOptions, defaultSequences
		);
		while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( getAcceptableQName() ) ) ) {
			xmlEvent = xmlEventReader.nextEvent();
			if ( fieldStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				constrainedFieldStaxBuilders.add( fieldStaxBuilder );
				fieldStaxBuilder = getNewConstrainedFieldStaxBuilder();
			}
			else if ( getterStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				constrainedGetterStaxBuilders.add( getterStaxBuilder );
				getterStaxBuilder = getNewConstrainedGetterStaxBuilder();
			}
			else if ( methodStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				constrainedMethodStaxBuilders.add( methodStaxBuilder );
				methodStaxBuilder = getNewConstrainedMethodStaxBuilder();
			}
			else if ( constructorStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				constrainedConstructorStaxBuilders.add( constructorStaxBuilder );
				constructorStaxBuilder = getNewConstrainedConstructorStaxBuilder();
			}
			else if ( localClassConstraintTypeStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				classConstraintTypeStaxBuilder = localClassConstraintTypeStaxBuilder;
			}
		}
	}

	private ConstrainedFieldStaxBuilder getNewConstrainedFieldStaxBuilder() {
		return new ConstrainedFieldStaxBuilder( classLoadingHelper, constraintCreationContext,
				defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	private ConstrainedGetterStaxBuilder getNewConstrainedGetterStaxBuilder() {
		return new ConstrainedGetterStaxBuilder( classLoadingHelper, constraintCreationContext,
				defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	private ConstrainedMethodStaxBuilder getNewConstrainedMethodStaxBuilder() {
		return new ConstrainedMethodStaxBuilder( classLoadingHelper, constraintCreationContext,
				defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	private ConstrainedConstructorStaxBuilder getNewConstrainedConstructorStaxBuilder() {
		return new ConstrainedConstructorStaxBuilder( classLoadingHelper, constraintCreationContext,
				defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	void build(JavaBeanHelper javaBeanHelper, Set<Class<?>> processedClasses, Map<Class<?>, Set<ConstrainedElement>> constrainedElementsByType) {
		Class<?> beanClass = classLoadingHelper.loadClass( className, defaultPackageStaxBuilder.build().orElse( "" ) );

		checkClassHasNotBeenProcessed( processedClasses, beanClass );

		// update annotation ignores
		// NOTE: if there was no ignoreAnnotations attribute specified on a bean
		// we use `true` as a default
		annotationProcessingOptions.ignoreAnnotationConstraintForClass(
				beanClass,
				ignoreAnnotations.orElse( true )
		);

		if ( classConstraintTypeStaxBuilder != null ) {
			addConstrainedElements(
					constrainedElementsByType,
					beanClass,
					Collections.singleton( classConstraintTypeStaxBuilder.build( beanClass ) )
			);
		}

		List<String> alreadyProcessedFieldNames = new ArrayList<>( constrainedFieldStaxBuilders.size() );
		addConstrainedElements(
				constrainedElementsByType,
				beanClass, constrainedFieldStaxBuilders.stream()
						.map( builder -> builder.build( javaBeanHelper, beanClass, alreadyProcessedFieldNames ) )
						.collect( Collectors.toList() )
		);

		List<String> alreadyProcessedGetterNames = new ArrayList<>( constrainedGetterStaxBuilders.size() );
		addConstrainedElements(
				constrainedElementsByType,
				beanClass,
				constrainedGetterStaxBuilders.stream()
						.map( builder -> builder.build( javaBeanHelper, beanClass, alreadyProcessedGetterNames ) )
						.collect( Collectors.toList() )
		);

		List<JavaBeanMethod> alreadyProcessedMethods = new ArrayList<>( constrainedMethodStaxBuilders.size() );
		addConstrainedElements(
				constrainedElementsByType,
				beanClass,
				constrainedMethodStaxBuilders.stream()
						.map( builder -> builder.build( javaBeanHelper, beanClass, alreadyProcessedMethods ) )
						.collect( Collectors.toList() )
		);

		List<JavaBeanConstructor> alreadyProcessedConstructors = new ArrayList<>( constrainedConstructorStaxBuilders.size() );
		addConstrainedElements(
				constrainedElementsByType,
				beanClass,
				constrainedConstructorStaxBuilders.stream()
						.map( builder -> builder.build( javaBeanHelper, beanClass, alreadyProcessedConstructors ) )
						.collect( Collectors.toList() )
		);
	}

	private void addConstrainedElements(Map<Class<?>, Set<ConstrainedElement>> constrainedElementsbyType, Class<?> beanClass, Collection<? extends ConstrainedElement> newConstrainedElements) {
		if ( constrainedElementsbyType.containsKey( beanClass ) ) {

			Set<ConstrainedElement> existingConstrainedElements = constrainedElementsbyType.get( beanClass );

			for ( ConstrainedElement constrainedElement : newConstrainedElements ) {
				if ( existingConstrainedElements.contains( constrainedElement ) ) {
					throw LOG.getConstrainedElementConfiguredMultipleTimesException(
							constrainedElement.toString()
					);
				}
			}

			existingConstrainedElements.addAll( newConstrainedElements );
		}
		else {
			Set<ConstrainedElement> tmpSet = newHashSet();
			tmpSet.addAll( newConstrainedElements );
			constrainedElementsbyType.put( beanClass, tmpSet );
		}
	}

	private void checkClassHasNotBeenProcessed(Set<Class<?>> processedClasses, Class<?> beanClass) {
		if ( processedClasses.contains( beanClass ) ) {
			throw LOG.getBeanClassHasAlreadyBeenConfiguredInXmlException( beanClass );
		}
		processedClasses.add( beanClass );
	}
}
