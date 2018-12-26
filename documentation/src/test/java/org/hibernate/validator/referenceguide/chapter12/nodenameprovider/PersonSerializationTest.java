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
//tag::include[]
