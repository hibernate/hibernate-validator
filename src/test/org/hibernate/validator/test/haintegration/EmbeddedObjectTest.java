//$Id: $
package org.hibernate.validator.test.haintegration;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.event.ValidatePreInsertEventListener;
import org.hibernate.validator.event.ValidatePreUpdateEventListener;
import org.hibernate.validator.test.HANTestCase;

/**
 * @author Emmanuel Bernard
 */
public class EmbeddedObjectTest extends HANTestCase {
	public void testNotNullEmbeddedObject() throws Exception {
		CreditCard cc = new CreditCard();
		User username = new User();
		username.setFirstname( "Emmanuel" );
		username.setMiddlename( "P" );
		username.setLastname( "Bernard" );
		cc.setUsername( username );
		ClassValidator ccValid = new ClassValidator( CreditCard.class );
		assertEquals( 0, ccValid.getInvalidValues( cc ).length );
		username.setMiddlename( null );
		assertEquals( 0, ccValid.getInvalidValues( cc ).length );
		Session s = openSession();
		Transaction tx = s.beginTransaction();
		s.persist( cc );
		s.flush();
		tx.rollback();
		s.close();
	}

	protected Class[] getMappings() {
		return new Class[]{
				CreditCard.class
		};
	}

    protected void configure(Configuration cfg) {
		cfg.getEventListeners()
				.setPreInsertEventListeners( new PreInsertEventListener[]{new ValidatePreInsertEventListener()} );
		cfg.getEventListeners()
				.setPreUpdateEventListeners( new PreUpdateEventListener[]{new ValidatePreUpdateEventListener()} );
	}
}
