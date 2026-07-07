/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.bean;

import java.lang.invoke.MethodHandles;
import java.util.Locale;

import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

final class BeanReferences {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private BeanReferences() {
	}

	public static <T> BeanReference<T> parse(Class<T> expectedType, String value) {
		Contracts.assertNotNull( expectedType, "expectedType" );
		Contracts.assertNotEmpty( value, "value" );

		BeanRetrieval retrieval;
		int colonIndex = value.indexOf( ':' );
		String name;
		if ( colonIndex < 0 ) {
			retrieval = BeanRetrieval.ANY;
			name = value;
		}
		else {
			String retrievalAsString = value.substring( 0, colonIndex );
			try {
				retrieval = BeanRetrieval.valueOf( retrievalAsString.toUpperCase( Locale.ROOT ) );
			}
			catch (IllegalArgumentException e) {
				throw LOG.getInvalidBeanRetrievalException( value, retrievalAsString + ":" );
			}
			name = value.substring( colonIndex + 1 );
		}
		return BeanReference.of( expectedType, name, retrieval );
	}
}
