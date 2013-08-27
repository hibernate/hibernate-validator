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
package org.hibernate.validator.test.internal.util;

import java.lang.reflect.Method;
import org.testng.annotations.Test;
import org.hibernate.validator.internal.util.ReflectionHelper;
import static org.testng.Assert.assertTrue;

public class StaticMethodTest {

    @Test
	public void testStaticMethod() throws Exception {
		Method methodString = Foo.class.getMethod("createFoo", String.class);
		Method methodInteger = Foo.class.getMethod("createFoo", Integer.class);
        
        assertTrue(ReflectionHelper.overrides(methodString,methodInteger));
	}

	private static class Foo {
		public static String createFoo(Integer param) {
			return null;
		}

		public static String createFoo(String param) {
			return null;
		}
	}
    
}