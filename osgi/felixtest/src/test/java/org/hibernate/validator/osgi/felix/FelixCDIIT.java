/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.osgi.felix;

import static org.jboss.shrinkwrap.api.ShrinkWrap.create;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.testng.annotations.Test;

import com.example.cdi.MyBean;
import com.example.cdi.ValidNumber;
import com.example.cdi.ValidNumberValidator;

public class FelixCDIIT extends Arquillian {

	@Inject
	private MyBean bean;

	@Deployment
	public static Archive<?> deployment() {
		PomEquippedResolveStage pom = Maven.resolver().loadPomFromFile( "pom.xml" );
		WebArchive war = create( WebArchive.class )
				.addClasses(
						MyBean.class,
						ValidNumber.class,
						ValidNumberValidator.class )
				.addAsLibraries( pom.resolve( "org.testng:testng" ).withTransitivity().asFile() );

		return war;
	}

	@Test
	public void testDefault() {
		bean.doDefault( "123456" );
	}

	@Test
	public void testAll9Valid() {
		bean.doAll9( "99999" );
	}

	@Test(expectedExceptions = Exception.class, expectedExceptionsMessageRegExp = "(?s).*invalid number.*(?s)")
	public void testAll9Invalid() {
		bean.doAll9( "999949" );
	}
}
