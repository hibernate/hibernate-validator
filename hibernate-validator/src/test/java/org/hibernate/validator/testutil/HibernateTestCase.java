/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hibernate.validator.testutil;

import java.io.InputStream;
import java.util.Properties;
import javax.validation.ValidatorFactory;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

/**
 * Base class for validation test which work in combination with Hibernate Core.
 *
 * @author Hardy Ferentschik
 */
public abstract class HibernateTestCase {
	private SessionFactory sessionFactory;
	private Configuration cfg;

	@BeforeTest
	protected void setUp() throws Exception {
		buildSessionFactory( getAnnotatedClasses(), getAnnotatedPackages(), getXmlFiles() );
	}

	@AfterTest
	protected void tearDown() throws Exception {
		SchemaExport export = new SchemaExport( cfg );
		export.drop( false, true );
		sessionFactory = null;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	private void buildSessionFactory(Class<?>[] classes, String[] packages, String[] xmlFiles) throws Exception {
		if ( sessionFactory != null ) {
			sessionFactory.close();
		}
		try {
			setCfg( new AnnotationConfiguration() );
			configure( cfg );
			if ( recreateSchema() ) {
				cfg.setProperty( org.hibernate.cfg.Environment.HBM2DDL_AUTO, "create-drop" );
			}
			for ( String aPackage : packages ) {
				( (AnnotationConfiguration) getCfg() ).addPackage( aPackage );
			}
			for ( Class<?> aClass : classes ) {
				( (AnnotationConfiguration) getCfg() ).addAnnotatedClass( aClass );
			}
			for ( String xmlFile : xmlFiles ) {
				InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream( xmlFile );
				getCfg().addInputStream( is );
			}
			sessionFactory = getCfg().buildSessionFactory();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			throw e;
		}
	}

	protected void configure(Configuration cfg) {
		Properties prop = cfg.getProperties();
		//prop.put( "javax.persistence.validation.mode", "none" );
		prop.put( "javax.persistence.validation.factory", getValidatorFactory() );
		prop.put( "hibernate.current_session_context_class", "thread" );
	}

	protected abstract ValidatorFactory getValidatorFactory();

	protected abstract Class<?>[] getAnnotatedClasses();

	protected String[] getAnnotatedPackages() {
		return new String[] { };
	}

	protected String[] getXmlFiles() {
		return new String[] { };
	}

	protected boolean recreateSchema() {
		return true;
	}

	protected void setCfg(Configuration cfg) {
		this.cfg = cfg;
	}

	protected Configuration getCfg() {
		return cfg;
	}
}



