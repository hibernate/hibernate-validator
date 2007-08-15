//$Id$
package org.hibernate.validator.test.jpa;

import java.util.Date;
import java.util.Map;
import javax.persistence.EntityManager;

import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.validator.InvalidStateException;

/**
 * @author Emmanuel Bernard
 */
public class HEMAutoWireringTest extends JPATestCase {
	public void testPropertyValidation() throws Exception {
		EntityManager em = factory.createEntityManager();
		Cat cat = new Cat();
		cat.setName( "iti" );
		em.getTransaction().begin();
		try {
			em.persist( cat );
			em.flush();
			fail( "No validation" );
		}
		catch (InvalidStateException e) {
			//success
		}
		finally {
			em.getTransaction().rollback();
			em.close();
		}
	}

	public void testEventPerProperties() throws Exception {
		EntityManager em = factory.createEntityManager();
		assertEquals( "Only validator and explicit NoOp should be present", 2,
				( (SessionImplementor) em.getDelegate() ).getListeners().getPreInsertEventListeners().length );
		em.close();
	}

	public Class[] getAnnotatedClasses() {
		return new Class[] {
				Cat.class
		};
	}


	public Map getConfig() {
		Map config = super.getConfig();
		config.put( HibernatePersistence.EVENT_LISTENER_PREFIX + ".pre-insert", NoOpListener.class.getName() );
		return config;
	}
}
