/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Constraint;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.jandex.ClassConstraintsJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.ConstrainedFieldJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.ConstrainedMethodJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.util.GroupSequenceJandexHelper;
import org.hibernate.validator.internal.metadata.jandex.util.JandexHelper;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

/**
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class JandexMetaDataProvider implements MetaDataProvider {

	private static final DotName CONSTRAINT_ANNOTATION = DotName.createSimple( Constraint.class.getName() );

	private final Map<DotName, BeanConfiguration<?>> configuredBeans;

	private final AnnotationProcessingOptions annotationProcessingOptions;

	public JandexMetaDataProvider(
			ConstraintHelper constraintHelper,
			JandexHelper jandexHelper,
			IndexView indexView,
			AnnotationProcessingOptions annotationProcessingOptions,
			ExecutableParameterNameProvider parameterNameProvider) {
		this.annotationProcessingOptions = annotationProcessingOptions;

		List<DotName> constraintAnnotations = Collections.unmodifiableList( extractConstraintAnnotations( indexView ) );

		this.configuredBeans = Collections.unmodifiableMap( extractConfiguredBeans( indexView,
				constraintHelper, jandexHelper,
				annotationProcessingOptions, parameterNameProvider,
				constraintAnnotations ) );
	}

	private static List<DotName> extractConstraintAnnotations(IndexView indexView) {
		return indexView.getAnnotations( CONSTRAINT_ANNOTATION ).stream()
				.filter( ai -> Kind.CLASS.equals( ai.target().kind() ) )
				.map( ai -> ai.target().asClass().name() )
				.collect( Collectors.toList() );
	}

	private static Map<DotName, BeanConfiguration<?>> extractConfiguredBeans(IndexView indexView, ConstraintHelper constraintHelper, JandexHelper jandexHelper,
			AnnotationProcessingOptions annotationProcessingOptions, ExecutableParameterNameProvider parameterNameProvider, List<DotName> constraintAnnotations) {
		return indexView.getKnownClasses().stream()
				.collect( Collectors.toMap(
						classInfo -> classInfo.name(),
						classInfo -> getBeanConfiguration(
								constraintHelper,
								jandexHelper,
								classInfo,
								annotationProcessingOptions,
								parameterNameProvider,
								constraintAnnotations
						)
				) );
	}

	private static BeanConfiguration<?> getBeanConfiguration(ConstraintHelper constraintHelper, JandexHelper jandexHelper,
			ClassInfo classInfo, AnnotationProcessingOptions annotationProcessingOptions, ExecutableParameterNameProvider parameterNameProvider,
			List<DotName> constraintAnnotations) {
		GroupSequenceJandexHelper groupSequenceJandexHelper = GroupSequenceJandexHelper.getInstance( jandexHelper );
		Class<?> bean = jandexHelper.getClassForName( classInfo.name().toString() );
		return new BeanConfiguration<>(
				ConfigurationSource.JANDEX,
				bean,
				getConstrainedElements( constraintHelper, jandexHelper, classInfo, bean, annotationProcessingOptions, parameterNameProvider, constraintAnnotations )
						.collect( Collectors.toSet() ),
				groupSequenceJandexHelper.getGroupSequence( classInfo ).collect( Collectors.toList() ),
				groupSequenceJandexHelper.getGroupSequenceProvider( classInfo )
		);
	}

	private static Stream<? extends ConstrainedElement> getConstrainedElements(
			ConstraintHelper constraintHelper,
			JandexHelper jandexHelper,
			ClassInfo classInfo,
			Class<?> bean,
			AnnotationProcessingOptions annotationProcessingOptions,
			ExecutableParameterNameProvider parameterNameProvider,
			List<DotName> constraintAnnotations
	) {
		//get constrained fields
		Stream<ConstrainedElement> constrainedElementStream = new ConstrainedFieldJandexBuilder(
				constraintHelper,
				jandexHelper,
				annotationProcessingOptions,
				constraintAnnotations
		).getConstrainedFields( classInfo, bean );
		//get constrained methods/constructors ?
		Stream.concat( constrainedElementStream, new ConstrainedMethodJandexBuilder(
				constraintHelper,
				jandexHelper,
				annotationProcessingOptions,
				parameterNameProvider,
				constraintAnnotations
		).getConstrainedExecutables( classInfo, bean ) );
		//get class level constraints
		Stream.concat( constrainedElementStream, new ClassConstraintsJandexBuilder(
				constraintHelper,
				jandexHelper,
				annotationProcessingOptions,
				constraintAnnotations
		).getClassConstraints( classInfo, bean ) );

		return constrainedElementStream;
	}

	@Override
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}

	@Override
	public <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(Class<T> beanClass) {
		List<BeanConfiguration<? super T>> configurations = newArrayList();

		for ( Class<? super T> clazz : ClassHierarchyHelper.getHierarchy( beanClass ) ) {
			BeanConfiguration<? super T> configuration = getBeanConfiguration( clazz );
			if ( configuration != null ) {
				configurations.add( configuration );
			}
		}

		return configurations;
	}

	@SuppressWarnings("unchecked")
	protected <T> BeanConfiguration<T> getBeanConfiguration(Class<T> beanClass) {
		Contracts.assertNotNull( beanClass );
		return (BeanConfiguration<T>) configuredBeans.get( DotName.createSimple( beanClass.getName() ) );
	}
}
