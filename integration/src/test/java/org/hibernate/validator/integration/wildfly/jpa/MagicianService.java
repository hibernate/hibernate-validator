/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.jpa;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Gunnar Morling
 */
@Stateless
public class MagicianService {

	@PersistenceContext
	private EntityManager em;

	public void storeMagician() {
		Magician magician = new Magician();
		magician.setName( "Balduin" );
		em.persist( magician );
	}
}
