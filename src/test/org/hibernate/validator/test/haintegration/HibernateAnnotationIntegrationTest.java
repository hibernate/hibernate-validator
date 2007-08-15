//$Id$
package org.hibernate.validator.test.haintegration;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.validator.Environment;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.event.ValidateEventListener;
import org.hibernate.validator.test.HANTestCase;

/**
 * Test the validate framework integration with the Hibernate
 * metadata binding
 *
 * @author Emmanuel Bernard
 */
public class HibernateAnnotationIntegrationTest extends HANTestCase {
	public void testApply() throws Exception {
		PersistentClass classMapping = getCfg().getClassMapping( Address.class.getName() );
		//new ClassValidator( Address.class, ResourceBundle.getBundle("messages", Locale.ENGLISH) ).apply( classMapping );
		Column stateColumn = (Column) classMapping.getProperty( "state" ).getColumnIterator().next();
		assertEquals( stateColumn.getLength(), 3 );
		Column zipColumn = (Column) classMapping.getProperty( "zip" ).getColumnIterator().next();
		assertEquals( zipColumn.getLength(), 5 );
		assertFalse( zipColumn.isNullable() );
	}

	public void testApplyOnIdColumn() throws Exception {
		PersistentClass classMapping = getCfg().getClassMapping( Tv.class.getName() );
		Column serialColumn = (Column) classMapping.getIdentifierProperty().getColumnIterator().next();
		assertEquals( "Vaidator annotation not applied on ids", 2, serialColumn.getLength() );
	}

	public void testApplyOnManyToOne() throws Exception {
		PersistentClass classMapping = getCfg().getClassMapping( TvOwner.class.getName() );
		Column serialColumn = (Column) classMapping.getProperty( "tv" ).getColumnIterator().next();
		assertEquals( "Validator annotations not applied on associations", false, serialColumn.isNullable() );
	}

	public void testSingleTableAvoidNotNull() throws Exception {
		PersistentClass classMapping = getCfg().getClassMapping( Rock.class.getName() );
		Column serialColumn = (Column) classMapping.getProperty( "bit" ).getColumnIterator().next();
		assertTrue( "Notnull should not be applised on single tables", serialColumn.isNullable() );
	}

	public void testEvents() throws Exception {
		Session s;
		Transaction tx;
		Address a = new Address();
		Address.blacklistedZipCode = "3232";
		a.setId( 12 );
		a.setCountry( "Country" );
		a.setLine1( "Line 1" );
		a.setZip( "nonnumeric" );
		a.setState( "NY" );
		s = openSession();
		tx = s.beginTransaction();
		try {
			s.persist( a );
			tx.commit();
			fail( "bean should have been validated" );
		}
		catch (InvalidStateException e) {
			//success
			assertEquals( 2, e.getInvalidValues().length );
			assertTrue( "Environment.MESSAGE_INTERPOLATOR_CLASS does not work",
					e.getInvalidValues()[0].getMessage().startsWith( "prefix_")
			);
		}
		finally {
			if ( tx != null ) tx.rollback();
			s.close();
		}
		s = openSession();
		tx = s.beginTransaction();
		a.setCountry( "Country" );
		a.setLine1( "Line 1" );
		a.setZip( "4343" );
		a.setState( "NY" );
		s.persist( a );
		a.setState( "TOOLONG" );
		try {
			s.flush();
			fail( "update should have been checked" );
		}
		catch (InvalidStateException e) {
			assertEquals( 1, e.getInvalidValues().length );
		}
		finally {
			if ( tx != null ) tx.rollback();
			s.close();
		}
	}

	public void testComponents() throws Exception {
		Session s;
		Transaction tx;
		s = openSession();
		tx = s.beginTransaction();
		Martian martian = new Martian();
		MartianPk pk = new MartianPk();
		pk.setColony( "Liberal" ); //one failure
		pk.setName( "Biboudie" );
		MarsAddress address = new MarsAddress();
		address.setContinent( "cont" ); //one failure
		address.setCanal( "Plus" ); //one failure
		martian.setId( pk );
		martian.setAddress( address );
		s.persist( martian );
		try {
			s.flush();
			fail( "Components are not validated" );
		}
		catch (InvalidStateException e) {
			assertEquals( 2, e.getInvalidValues().length );
		}
		finally {
			tx.rollback();
			s.close();
		}
	}

	public void testIdClass() throws Exception {
		Session s;
		Transaction tx;
		s = openSession();
		tx = s.beginTransaction();
		Venusian venus = new Venusian();
		venus.setName( "bibi" );
		venus.setRegion( "ts" );
		s.persist( venus );
		try {
			s.flush();
			fail( "test on embedded properties should have been done" );
		}
		catch (InvalidStateException e) {
			assertEquals( 1, e.getInvalidValues().length );
		}
		finally {
			tx.rollback();
			s.close();
		}
	}

	protected void configure(Configuration cfg) {
		cfg.setProperty( Environment.MESSAGE_INTERPOLATOR_CLASS, PrefixMessageInterpolator.class.getName() );
	}

	protected Class[] getMappings() {
		return new Class[]{
				Address.class,
				Martian.class,
				Venusian.class,
				Tv.class,
				TvOwner.class,
				Music.class,
				Rock.class
		};
	}

	public HibernateAnnotationIntegrationTest(String x) {
		super( x );
	}
}
