/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter12.nodenameprovider;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//tag::include[]
public class PersonSerializationTest {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void personIsSerialized() throws JsonProcessingException {
		Person person = new Person( "Clark", "Kent" );

		String serializedPerson = objectMapper.writeValueAsString( person );

		assertEquals( "{\"first_name\":\"Clark\",\"last_name\":\"Kent\"}", serializedPerson );
	}
}
//end::include[]
