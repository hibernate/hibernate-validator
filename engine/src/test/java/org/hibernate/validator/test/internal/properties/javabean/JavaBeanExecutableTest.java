/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.properties.javabean;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.internal.properties.javabean.JavaBeanConstructor;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

public class JavaBeanExecutableTest {

	@Test
	@TestForIssue(jiraKey = "HV-1634")
	public void testGenericTypeParametersWithImplicitParameters() throws NoSuchMethodException, SecurityException {
		JavaBeanConstructor constructor = new JavaBeanConstructor( Bean.class.getDeclaredConstructors()[0] );

		assertThat( constructor.getParameters() ).hasSize( 3 );

		ParameterizedType parameterizedType1 = (ParameterizedType) constructor.getParameterGenericType( 1 );
		assertThat( parameterizedType1.getRawType() ).isEqualTo( Map.class );

		ParameterizedType parameterizedType2 = (ParameterizedType) constructor.getParameterGenericType( 2 );
		assertThat( parameterizedType2.getRawType() ).isEqualTo( List.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1634")
	public void testGenericTypeParametersWithoutImplicitParameters() throws NoSuchMethodException, SecurityException {
		JavaBeanConstructor constructor = new JavaBeanConstructor( StaticBean.class.getDeclaredConstructors()[0] );

		assertThat( constructor.getParameters() ).hasSize( 2 );

		ParameterizedType parameterizedType0 = (ParameterizedType) constructor.getParameterGenericType( 0 );
		assertThat( parameterizedType0.getRawType() ).isEqualTo( Map.class );

		ParameterizedType parameterizedType1 = (ParameterizedType) constructor.getParameterGenericType( 1 );
		assertThat( parameterizedType1.getRawType() ).isEqualTo( List.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1634")
	public void testGenericTypeParametersWithSyntheticParameters() throws NoSuchMethodException, SecurityException {
		JavaBeanConstructor constructor = new JavaBeanConstructor( MyEnum.class.getDeclaredConstructors()[0] );

		assertThat( constructor.getParameters() ).hasSize( 4 );

		ParameterizedType parameterizedType1 = (ParameterizedType) constructor.getParameterGenericType( 2 );
		assertThat( parameterizedType1.getRawType() ).isEqualTo( Map.class );

		ParameterizedType parameterizedType2 = (ParameterizedType) constructor.getParameterGenericType( 3 );
		assertThat( parameterizedType2.getRawType() ).isEqualTo( List.class );
	}

	private class Bean {

		private Bean(Map<String, String> map, List<Integer> list) {
		}
	}

	private static class StaticBean {

		private StaticBean(Map<String, String> map, List<Integer> list) {
		}
	}

	private enum MyEnum {

		;

		private MyEnum(Map<String, String> map, List<Integer> list) {
		}
	}
}
