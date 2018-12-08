/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.xml.AbstractStaxBuilder;

/**
 * Top level builder for constraint mappings. Reads the whole mapping file and builds the constraint definitions defined
 * in it as well as the list of constrained elements per bean.
 *
 * @author Marko Bekhta
 */
class ConstraintMappingsStaxBuilder extends AbstractStaxBuilder {

	private static final String CONSTRAINT_MAPPINGS_QNAME = "constraint-mappings";

	private final ClassLoadingHelper classLoadingHelper;
	private final ConstraintCreationContext constraintCreationContext;
	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;
	private final JavaBeanHelper javaBeanHelper;
	private final Map<Class<?>, List<Class<?>>> defaultSequences;

	private final DefaultPackageStaxBuilder defaultPackageStaxBuilder;
	private final List<BeanStaxBuilder> beanStaxBuilders;
	private final List<ConstraintDefinitionStaxBuilder> constraintDefinitionStaxBuilders;

	public ConstraintMappingsStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintCreationContext constraintCreationContext,
			AnnotationProcessingOptionsImpl annotationProcessingOptions, JavaBeanHelper javaBeanHelper, Map<Class<?>, List<Class<?>>> defaultSequences) {
		this.classLoadingHelper = classLoadingHelper;
		this.constraintCreationContext = constraintCreationContext;
		this.annotationProcessingOptions = annotationProcessingOptions;
		this.javaBeanHelper = javaBeanHelper;
		this.defaultSequences = defaultSequences;

		this.defaultPackageStaxBuilder = new DefaultPackageStaxBuilder();
		this.beanStaxBuilders = new ArrayList<>();
		this.constraintDefinitionStaxBuilders = new ArrayList<>();
	}

	@Override
	protected String getAcceptableQName() {
		return CONSTRAINT_MAPPINGS_QNAME;
	}

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
		BeanStaxBuilder beanStaxBuilder = getNewBeanStaxBuilder();
		ConstraintDefinitionStaxBuilder constraintDefinitionStaxBuilder = getNewConstraintDefinitionStaxBuilder();

		while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( getAcceptableQName() ) ) ) {
			xmlEvent = xmlEventReader.nextEvent();
			if ( beanStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				beanStaxBuilders.add( beanStaxBuilder );
				beanStaxBuilder = getNewBeanStaxBuilder();
			}
			else if ( constraintDefinitionStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				constraintDefinitionStaxBuilders.add( constraintDefinitionStaxBuilder );
				constraintDefinitionStaxBuilder = getNewConstraintDefinitionStaxBuilder();
			}
			defaultPackageStaxBuilder.process( xmlEventReader, xmlEvent );
		}
	}

	private BeanStaxBuilder getNewBeanStaxBuilder() {
		return new BeanStaxBuilder( classLoadingHelper, constraintCreationContext,
				defaultPackageStaxBuilder, annotationProcessingOptions, defaultSequences );
	}

	private ConstraintDefinitionStaxBuilder getNewConstraintDefinitionStaxBuilder() {
		return new ConstraintDefinitionStaxBuilder( classLoadingHelper, constraintCreationContext.getConstraintHelper(), defaultPackageStaxBuilder );
	}

	public void build(Set<Class<?>> processedClasses, Map<Class<?>, Set<ConstrainedElement>> constrainedElementsByType, Set<String> alreadyProcessedConstraintDefinitions) {
		constraintDefinitionStaxBuilders.forEach( builder -> builder.build( alreadyProcessedConstraintDefinitions ) );
		beanStaxBuilders.forEach( builder -> builder.build( javaBeanHelper, processedClasses, constrainedElementsByType ) );
	}
}
