//$Id: $
package org.hibernate.validator.test;

import java.io.InputStream;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.dialect.Dialect;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.Configuration;

/**
 * Copy from HAN
 *
 * @author Emmanuel Bernard
 */
public abstract class HANTestCase extends junit.framework.TestCase {

	private static SessionFactory sessions;
	private static AnnotationConfiguration cfg;
	private static Dialect dialect;
	private static Class lastTestClass;
	private Session session;

	public HANTestCase() {
		super();
	}

	public HANTestCase(String x) {
		super( x );
	}

	protected void buildSessionFactory(Class[] classes, String[] packages, String[] xmlFiles) throws Exception {

		if ( getSessions() != null ) getSessions().close();
		try {
			setCfg( new AnnotationConfiguration() );
			configure( cfg );
			if ( recreateSchema() ) {
				cfg.setProperty( Environment.HBM2DDL_AUTO, "create-drop" );
			}
			for ( int i = 0; i < packages.length; i++ ) {
				getCfg().addPackage( packages[i] );
			}
			for ( int i = 0; i < classes.length; i++ ) {
				getCfg().addAnnotatedClass( classes[i] );
			}
			for ( int i = 0; i < xmlFiles.length; i++ ) {
				InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream( xmlFiles[i] );
				getCfg().addInputStream( is );
			}
			setDialect( Dialect.getDialect() );
			setSessions( getCfg().buildSessionFactory( /*new TestInterceptor()*/ ) );
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	protected void setUp() throws Exception {
		if ( getSessions() == null || lastTestClass != getClass() ) {
			buildSessionFactory( getMappings(), getAnnotatedPackages(), getXmlFiles() );
			lastTestClass = getClass();
		}
	}

	protected void runTest() throws Throwable {
		try {
			super.runTest();
			if ( session != null && session.isOpen() ) {
				if ( session.isConnected() ) session.connection().rollback();
				session.close();
				session = null;
				fail( "unclosed session" );
			}
			else {
				session = null;
			}
		}
		catch (Throwable e) {
			try {
				if ( session != null && session.isOpen() ) {
					if ( session.isConnected() ) session.connection().rollback();
					session.close();
				}
			}
			catch (Exception ignore) {
			}
			try {
				if ( sessions != null ) {
					sessions.close();
					sessions = null;
				}
			}
			catch (Exception ignore) {
			}
			throw e;
		}
	}

	public Session openSession() throws HibernateException {
		session = getSessions().openSession();
		return session;
	}

	public Session openSession(Interceptor interceptor) throws HibernateException {
		session = getSessions().openSession( interceptor );
		return session;
	}

	protected abstract Class[] getMappings();

	protected String[] getAnnotatedPackages() {
		return new String[]{};
	}

	protected String[] getXmlFiles() {
		return new String[]{};
	}

	private void setSessions(SessionFactory sessions) {
		HANTestCase.sessions = sessions;
	}

	protected SessionFactory getSessions() {
		return sessions;
	}

	private void setDialect(Dialect dialect) {
		HANTestCase.dialect = dialect;
	}

	protected Dialect getDialect() {
		return dialect;
	}

	protected static void setCfg(AnnotationConfiguration cfg) {
		HANTestCase.cfg = cfg;
	}

	protected static AnnotationConfiguration getCfg() {
		return cfg;
	}

	protected void configure(Configuration cfg) {
		//cfg.setNamingStrategy( AlternativeNamingStrategy.INSTANCE );
		//cfg.getSessionEventListenerConfig().setFlushEventListener( new EJB3FlushEventListener() );
		//cfg.getSessionEventListenerConfig().setAutoFlushEventListener( new EJB3AutoFlushEventListener() );
	}

	protected boolean recreateSchema() {
		return true;
	}

}

