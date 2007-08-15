//$Id: $
package org.hibernate.validator.test.haintegration;

import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Column;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.cfg.Configuration;
import org.hibernate.validator.Environment;
import org.hibernate.validator.test.HANTestCase;
import org.hibernate.validator.event.ValidateEventListener;
import org.hibernate.Session;

/**
 * Test the ability to disable DDL update
 *
 * @author Emmanuel Bernard
 */
public class NonHibernateAnnotationsIntegrationTest extends HANTestCase {
	public void testNotApplyDll() throws Exception {
		PersistentClass classMapping = getCfg().getClassMapping( Address.class.getName() );
		Column stateColumn = (Column) classMapping.getProperty( "state" ).getColumnIterator().next();
		assertFalse( stateColumn.getLength() == 3 );
		Column zipColumn = (Column) classMapping.getProperty( "zip" ).getColumnIterator().next();
		assertFalse( zipColumn.getLength() ==  5 );
		assertTrue( zipColumn.isNullable() );
	}

	public void testNotApplyListener() throws Exception {
		Session s = openSession( );
		Address a = new Address();
		s.persist( a ); //shouldn't fail
		s.flush();
		s.close();
	}

	protected void configure(Configuration cfg) {
		cfg.setProperty( Environment.MESSAGE_INTERPOLATOR_CLASS, PrefixMessageInterpolator.class.getName() );
		cfg.setProperty( Environment.APPLY_TO_DDL, "false" );
		cfg.setProperty( Environment.AUTOREGISTER_LISTENERS, "false" );
	}

	protected Class[] getMappings() {
		return new Class[]{
				Address.class,
		};
	}

	public NonHibernateAnnotationsIntegrationTest(String x) {
		super( x );
	}
}
