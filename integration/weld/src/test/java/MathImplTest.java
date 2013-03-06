/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class MathImplTest {

	/**
	 * Since Arquillian actually creates JAR files under the covers, the @Deployment
	 * is your way of controlling what is included in that Archive. Note, each
	 * class utilized in your test case - whether directly or indirectly - must be
	 * added to the deployment archive.
	 */
	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( Math.class )
				.addClass( MathImpl.class )
				.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	// Arquillian enables @Inject directly in the test case class itself!
	@Inject
	Math m;


	@Test
	public void testAdd() throws Exception {
		assertEquals( 5, m.add( 2, 3 ) );
	}

	@Test
	public void testSubtract() throws Exception {
		assertEquals( -1, m.subtract( 2, 3 ) );
	}
}


