/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.jandex;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.jandex.ClassConstrainsJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.ConstrainedFieldJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.ConstrainedMethodJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.util.JandexHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.parameternameprovider.ParanamerParameterNameProvider;
import org.hibernate.validator.test.internal.metadata.jandex.model.ConstrainedFieldJandexBuilderModel;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import org.assertj.core.api.ListAssert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class ConstrainedFieldJandexBuilderTest {

	private Index index;

	@BeforeClass
	public void setUp() throws IOException {
		Indexer indexer = new Indexer();
		// Normally a direct file is opened, but class-loader backed streams work as well.
		try ( InputStream stream = getClass().getClassLoader().getResourceAsStream(
				"org/hibernate/validator/test/internal/metadata/jandex/model/ConstrainedFieldJandexBuilderModel.class" )
		) {
			indexer.index( stream );
			index = indexer.complete();
		}
	}

	/**
	 * Simple test to verify that {@link ConstrainedFieldJandexBuilder} is working
	 */
	@Test
	public void testGetConstrainedFields() {
		ConstrainedFieldJandexBuilder.getInstance( new ConstraintHelper(), JandexHelper.getInstance(), new AnnotationProcessingOptionsImpl() )
				.getConstrainedFields(
						index.getClassByName( DotName.createSimple( ConstrainedFieldJandexBuilderModel.class.getName() ) ),
						ConstrainedFieldJandexBuilderModel.class
				).collect( Collectors.toSet() );

		ConstrainedMethodJandexBuilder.getInstance( new ConstraintHelper(), JandexHelper.getInstance(), new AnnotationProcessingOptionsImpl(),
				new ExecutableParameterNameProvider( new ParanamerParameterNameProvider() )
		).getConstrainedExecutables(
				index.getClassByName( DotName.createSimple( ConstrainedFieldJandexBuilderModel.class.getName() ) ),
				ConstrainedFieldJandexBuilderModel.class
		).collect( Collectors.toSet() );

		ClassConstrainsJandexBuilder.getInstance( new ConstraintHelper(), JandexHelper.getInstance(), new AnnotationProcessingOptionsImpl() )
				.getClassConstrains(
						index.getClassByName( DotName.createSimple( ConstrainedFieldJandexBuilderModel.class.getName() ) ),
						ConstrainedFieldJandexBuilderModel.class
				).collect( Collectors.toSet() );
	}

	@Test
	public void validAnnotationIsMissing() {
		MethodInfo method = index.getClassByName( DotName.createSimple( ConstrainedFieldJandexBuilderModel.class.getName() ) )
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
