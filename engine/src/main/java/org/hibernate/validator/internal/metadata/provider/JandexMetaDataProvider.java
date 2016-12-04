/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.jandex.ClassConstrainsJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.ConstrainedFieldJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.ConstrainedMethodJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.GroupSequenceJandexHelper;
import org.hibernate.validator.internal.metadata.jandex.util.JandexHelper;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;

/**
 * @author Marko Bekhta
 */
public class JandexMetaDataProvider extends MetaDataProviderKeyedByClassName {

	private static final Log log = LoggerFactory.make();

	private AnnotationProcessingOptions annotationProcessingOptions;
	protected final ExecutableParameterNameProvider parameterNameProvider;

	public JandexMetaDataProvider(
			ConstraintHelper constraintHelper,
			JandexHelper jandexHelper,
			InputStream jandexIndexStreamResource,
			AnnotationProcessingOptions annotationProcessingOptions,
			ExecutableParameterNameProvider parameterNameProvider) {
		super( constraintHelper,
				readJandexIndex( constraintHelper, jandexHelper, jandexIndexStreamResource, annotationProcessingOptions, parameterNameProvider )
		);
		this.annotationProcessingOptions = annotationProcessingOptions;
		this.parameterNameProvider = parameterNameProvider;
	}

	private static Map<String, BeanConfiguration<?>> readJandexIndex(ConstraintHelper constraintHelper, JandexHelper jandexHelper,
			InputStream jandexIndexStreamResource, AnnotationProcessingOptions annotationProcessingOptions,
			ExecutableParameterNameProvider parameterNameProvider) {
		IndexReader jandexReader = new IndexReader( jandexIndexStreamResource );
		Index index;
		try {
			index = jandexReader.read();
		}
		catch (IOException e) {
			throw log.getParsingJandexIndexException( e );
		}

		// go through all classes (and interfaces ?) to build configuration map
		return index.getKnownClasses().stream()
				.collect( Collectors.toMap(
						classInfo -> classInfo.name().toString(),
						classInfo -> getBeanConfiguration(
								constraintHelper,
								jandexHelper,
								classInfo,
								annotationProcessingOptions,
								parameterNameProvider
						)
				) );

	}

	private static BeanConfiguration getBeanConfiguration(ConstraintHelper constraintHelper, JandexHelper jandexHelper,
			ClassInfo classInfo, AnnotationProcessingOptions annotationProcessingOptions, ExecutableParameterNameProvider parameterNameProvider) {
		GroupSequenceJandexHelper groupSequenceJandexHelper = GroupSequenceJandexHelper.getInstance( jandexHelper );
		Class<?> bean = jandexHelper.getClassForName( classInfo.name().toString() );
		return new BeanConfiguration(
				ConfigurationSource.JANDEX,
				bean,
				getConstrainedElements( constraintHelper, jandexHelper, classInfo, bean, annotationProcessingOptions, parameterNameProvider )
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
			ExecutableParameterNameProvider parameterNameProvider
	) {
		//get constrained fields
		Stream<ConstrainedElement> constrainedElementStream = ConstrainedFieldJandexBuilder.getInstance(
				constraintHelper,
				jandexHelper,
				annotationProcessingOptions
		).getConstrainedFields( classInfo, bean );
		//TODO: need to check what is happening with constructors.
		//get constrained methods/constructors ?
		Stream.concat( constrainedElementStream, ConstrainedMethodJandexBuilder.getInstance(
				constraintHelper,
				jandexHelper,
				annotationProcessingOptions,
				parameterNameProvider
		).getConstrainedExecutables( classInfo, bean ) );
		//get class level constraints
		Stream.concat( constrainedElementStream, ClassConstrainsJandexBuilder.getInstance(
				constraintHelper,
				jandexHelper,
				annotationProcessingOptions
		).getClassConstrains( classInfo, bean ) );

		return constrainedElementStream;
	}

	@Override
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}
}
