//$Id: $
package org.hibernate.validator.test.jpa;

import java.util.Map;

import javax.persistence.EntityManager;

import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.validator.Environment;
import org.hibernate.validator.InvalidStateException;

/**
 * @author Emmanuel Bernard
 */
public class JPAValidateListenerTest extends JPATestCase {

	public void testEventTrigger() {
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		Commander beetles = new Commander();
		beetles.setName( "" );
		Submarine yellowSubmarine = new Submarine();
		yellowSubmarine.setCommander( beetles );
		yellowSubmarine.setName( "" );
		yellowSubmarine.setSize( 3 );
		try {
			em.persist( yellowSubmarine );
			em.flush();
			fail("Event not wired");
		}
		catch(InvalidStateException e) {
			assertEquals( 3, e.getInvalidValues().length );
		}
		finally {
			em.getTransaction().rollback();
			em.close();
		}

		//update trigger
		em = factory.createEntityManager();
		em.getTransaction().begin();
		beetles = new Commander();
		beetles.setName( "Beetles" );
		yellowSubmarine = new Submarine();
		yellowSubmarine.setCommander( beetles );
		yellowSubmarine.setName( "Yellow" );
		yellowSubmarine.setSize( 13 );
		em.persist( yellowSubmarine );
		em.flush();
		em.clear();
		yellowSubmarine = em.find( Submarine.class, yellowSubmarine.getId() );
		yellowSubmarine.setSize( 3 );
		try {
			em.flush();
			fail("Event not wired");
		}
		catch(InvalidStateException e) {
			assertEquals( 1, e.getInvalidValues().length );
		}
		finally {
			em.getTransaction().rollback();
			em.close();
		}

	}

	public Class[] getAnnotatedClasses() {
		return new Class[]{
				Commander.class,
				Submarine.class
		};
	}


	public Map getConfig() {
		Map map = super.getConfig();
		//Remove regular Validator wiring
		map.put( HibernatePersistence.EVENT_LISTENER_PREFIX + "." + "pre-insert",
				"" );
		map.put( HibernatePersistence.EVENT_LISTENER_PREFIX + "." + "pre-update",
				"" );
		return map;
	}
}
