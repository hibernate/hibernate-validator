/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.wildfly.jpa;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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

	public void storeWand() {
		Wand wand = new Wand();
		wand.setBrand( "Doh" );
		em.persist( wand );
	}
}
