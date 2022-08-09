/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.ejb;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.ejb.Stateless;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@Stateless
@Path("/")
public class EjbJaxRsResource {

	@POST
	@Path("put/list")
	@Consumes(MediaType.APPLICATION_JSON)
	public String putList(@NotEmpty List<String> a) {
		return "Hello bars " + a.stream().collect( Collectors.joining( ", " ) );
	}

}

