/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.jandex.ConstrainedFieldJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.util.JandexUtils;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;

/**
 * @author Marko Bekhta
 */
public class JandexMetaDataProvider extends MetaDataProviderKeyedByClassName {

	private static final Log log = LoggerFactory.make();

	private AnnotationProcessingOptions annotationProcessingOptions;

	public JandexMetaDataProvider(
			ConstraintHelper constraintHelper,
			InputStream jandexIndexStreamResource,
			AnnotationProcessingOptions annotationProcessingOptions) {
		super( constraintHelper, readJandexIndex( constraintHelper, jandexIndexStreamResource ) );
		this.annotationProcessingOptions = annotationProcessingOptions;
	}

	private static Map<String, BeanConfiguration<?>> readJandexIndex(ConstraintHelper constraintHelper, InputStream jandexIndexStreamResource) {
		IndexReader jandexReader = new IndexReader( jandexIndexStreamResource );
		Index index = null;
		try {
			index = jandexReader.read();
		}
		catch (IOException e) {
			throw log.getParsingJandexIndexException( e );
		}

		Map<String, BeanConfiguration<?>> beanConfigurationMap = CollectionHelper.newHashMap();

		// go through all classes (and interfaces ?) to build configuration map
		for ( ClassInfo classInfo : index.getKnownClasses() ) {
			beanConfigurationMap.put( classInfo.name().toString(), getBeanConfiguration( constraintHelper, index, classInfo ) );
		}

		return null;
	}

	private static BeanConfiguration getBeanConfiguration(ConstraintHelper constraintHelper, Index jandexIndex, ClassInfo classInfo) {
		return new BeanConfiguration(
				ConfigurationSource.JANDEX,
				JandexUtils.getClassForName( classInfo.name().toString() ),
				getConstrainedElements( constraintHelper, classInfo ),
				getDefaultGroupSequence( jandexIndex, classInfo ),
				getDefaultGroupSequenceProvider()
		);
	}

	private static DefaultGroupSequenceProvider<?> getDefaultGroupSequenceProvider() {
		return null;
	}

	private static List<Class<?>> getDefaultGroupSequence(Index jandexIndex, ClassInfo classInfo) {
		return null;
	}

	private static Set<? extends ConstrainedElement> getConstrainedElements(ConstraintHelper constraintHelper, ClassInfo classInfo) {

		//get constrained fields
		Stream<ConstrainedElement> constrainedFields = ConstrainedFieldJandexBuilder.getInstance( constraintHelper )
				.getConstrainedFields( classInfo, JandexUtils.getClassForName( classInfo.name().toString() ) );

		return null;
	}

	@Override
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}
}
