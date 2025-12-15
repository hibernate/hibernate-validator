/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter12.nodenameprovider;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

//tag::include[]
public class PersonSerializationTest {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void personIsSerialized() throws JacksonException {
		Person person = new Person( "Clark", "Kent" );

		String serializedPerson = objectMapper.writeValueAsString( person );

		assertEquals( "{\"first_name\":\"Clark\",\"last_name\":\"Kent\"}", serializedPerson );
	}
}
//end::include[]
