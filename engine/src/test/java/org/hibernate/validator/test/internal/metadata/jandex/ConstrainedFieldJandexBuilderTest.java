/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.jandex;

import java.io.IOException;
import java.io.InputStream;

import org.hibernate.validator.internal.metadata.jandex.ConstrainedFieldJandexBuilder;
import org.hibernate.validator.test.internal.metadata.jandex.model.ConstrainedFieldJandexBuilderModel;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;

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
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream(
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
		ConstrainedFieldJandexBuilder.getInstance().getConstrainedFields(
				index.getClassByName( DotName.createSimple( ConstrainedFieldJandexBuilderModel.class.getName() ) ),
				ConstrainedFieldJandexBuilderModel.class
		);

	}

}
