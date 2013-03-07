/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.integration.cdi;

import java.util.Collection;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hibernate.validator.internal.util.Version;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class InterceptorTest {
	private static final String VALIDATOR_VERSION = Version.getVersionString();

	private static BeansDescriptor beans = Descriptors.create( BeansDescriptor.class );

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( Repeater.class )
				.addClass( RepeaterImpl.class )
				// adding interceptor package
				.addPackage( "org.hibernate.validator.internal.cdi.interceptor" )
				.addAsManifestResource( new StringAsset( beans.exportAsString() ), "beans.xml" );
	}

	@Inject
	Repeater repeater;

	@Test
	public void testInjection() throws Exception {
		assertNotNull( repeater );
		try {
			repeater.repeat( null );
			fail( "CDI method interceptor should have thrown an exception" );
		}
		catch ( ConstraintViolationException e ) {
			// success
		}

	}

	private static Collection<JavaArchive> bundleHibernateValidatorWithDependencies() {
		Collection<JavaArchive> hibernateValidatorWithDependencies = DependencyResolvers.use(
				MavenDependencyResolver.class
		)
				// go offline to make sure to get the SNAPSHOT from the current build and not a resolved SNAPSHOT
				// from a remote repo
				.goOffline()
				.artifact( "org.hibernate:hibernate-validator:" + VALIDATOR_VERSION )
				.resolveAs( JavaArchive.class );
		return hibernateValidatorWithDependencies;
	}
}


