/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.jandex;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.assertj.core.api.ListAssert;
import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.jandex.ConstrainedFieldJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.util.JandexHelper;
import org.hibernate.validator.internal.metadata.provider.JandexMetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.test.internal.metadata.jandex.model.ConstrainedFieldJandexBuilderModel;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class JandexMetaDataProviderTest {

	private IndexView indexView;

	@BeforeClass
	public void setUp() throws IOException {
		Class<?>[] classesToIndex = {
				Min.class,
				Max.class,
				NotNull.class,
				ConstrainedFieldJandexBuilderModel.class
		};

		Indexer indexer = new Indexer();
		// Normally a direct file is opened, but classloader backed streams work as well.
		for ( Class<?> classToIndex : classesToIndex ) {
			try ( InputStream stream = getClass().getClassLoader().getResourceAsStream( classToIndex.getCanonicalName().replace( '.', '/' ) + ".class" ) ) {
				indexer.index( stream );
			}
		}
		indexView = indexer.complete();
	}

	/**
	 * Simple test to verify that {@link ConstrainedFieldJandexBuilder} is working
	 */
	@Test
	public void testGetConstrainedFields() {
		JandexMetaDataProvider jandexMetaDataProvider = new JandexMetaDataProvider( new ConstraintHelper(), new JandexHelper(), indexView,
				new AnnotationProcessingOptionsImpl(), new ExecutableParameterNameProvider( new DefaultParameterNameProvider() ) );

		List<BeanConfiguration<? super ConstrainedFieldJandexBuilderModel>> beanConfigurations =
				jandexMetaDataProvider.getBeanConfigurationForHierarchy( ConstrainedFieldJandexBuilderModel.class );

		assertThat( beanConfigurations ).isNotEmpty();
	}

	@Test
	public void validAnnotationIsMissing() {
		MethodInfo method = indexView.getClassByName( DotName.createSimple( ConstrainedFieldJandexBuilderModel.class.getName() ) )
				.methods().stream().filter( methodInfo -> methodInfo.name().equals( "someMethod1" ) )
				.findAny().get();
		ListAssert<Type> parametersAssert = new ListAssert<>( method.parameters() );
		parametersAssert.hasSize( 2 );

		ListAssert<String> annotations = new ListAssert<>( method.parameters().get( 0 ).annotations().stream()
				.map( annotationInstance -> annotationInstance.name().toString() )
				.collect( Collectors.toList() ) );
		annotations.hasSize( 1 );
		annotations.contains( "javax.validation.constraints.NotNull" );

		annotations = new ListAssert<>( method.parameters().get( 1 ).annotations().stream()
				.map( annotationInstance -> annotationInstance.name().toString() )
				.collect( Collectors.toList() ) );
		annotations.hasSize( 3 );
		annotations.contains( "javax.validation.constraints.NotNull", "javax.validation.constraints.Size", "javax.validation.Valid" );
	}

}
