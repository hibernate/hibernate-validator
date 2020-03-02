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
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

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

