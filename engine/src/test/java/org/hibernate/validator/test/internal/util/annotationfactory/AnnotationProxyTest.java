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
package org.hibernate.validator.test.internal.util.annotationfactory;

import static org.fest.assertions.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethods;

/**
 * @author Gunnar Morling
 */
public class AnnotationProxyTest {

	private MyAnno realAnnotation;

	private AnnotationDescriptor<MyAnno> descriptor;

	@BeforeMethod
	public void setupAnnotations() throws Exception {
		realAnnotation = Foo.class.getAnnotation( MyAnno.class );
		descriptor = getDescriptorFromAnnotation( realAnnotation );
	}

	@Test
	public void testEqualsAnnotationsAreEqual() {
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isEqualTo( realAnnotation );
		assertThat( realAnnotation ).isEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsAnnotationsAreSame() {
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsCheckNull() {
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( null );
	}

	@Test
	public void testEqualsAnnotationTypesDiffer() {
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		MyOtherAnno anotherAnnotation = Baz.class.getAnnotation( MyOtherAnno.class );
		AnnotationDescriptor<MyOtherAnno> anotherDescriptor = getDescriptorFromAnnotation(
				anotherAnnotation
		);
		MyOtherAnno anotherProxiedAnnotation = AnnotationFactory.create( anotherDescriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( anotherProxiedAnnotation );
		assertThat( anotherProxiedAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( anotherProxiedAnnotation ).isNotEqualTo( realAnnotation );
	}

	@Test
	public void testEqualsPrimitiveMembersDiffer() {
		descriptor.setValue( "int_", 42 );
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsDoubleNaNEqualsItself() throws Exception {
		realAnnotation = Bar.class.getMethod( "nanDouble" ).getAnnotation( MyAnno.class );
		descriptor = getDescriptorFromAnnotation( realAnnotation );

		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isEqualTo( realAnnotation );
		assertThat( realAnnotation ).isEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsDoubleZeroNotEqualToMinusZero() throws Exception {
		realAnnotation = Bar.class.getMethod( "zeroDouble" ).getAnnotation( MyAnno.class );
		descriptor = getDescriptorFromAnnotation( realAnnotation );
		descriptor.setValue( "double_", -0.0 );

		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsFloatNaNEqualsItself() throws Exception {
		realAnnotation = Bar.class.getMethod( "nanFloat" ).getAnnotation( MyAnno.class );
		descriptor = getDescriptorFromAnnotation( realAnnotation );

		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isEqualTo( realAnnotation );
		assertThat( realAnnotation ).isEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsFloatZeroNotEqualToMinusZero() throws Exception {
		realAnnotation = Bar.class.getMethod( "zeroFloat" ).getAnnotation( MyAnno.class );
		descriptor = getDescriptorFromAnnotation( realAnnotation );
		descriptor.setValue( "float_", -0.0f );

		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsStringMembersDiffer() {
		descriptor.setValue( "string", "Bar" );
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsClassMembersDiffer() {
		descriptor.setValue( "class_", Integer.class );
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsBooleanArrayMembersDiffer() {
		descriptor.setValue( "booleans", new boolean[] { } );
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsByteArrayMembersDiffer() {
		descriptor.setValue( "bytes", new byte[] { } );
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsCharArrayMembersDiffer() {
		descriptor.setValue( "chars", new char[] { } );
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsDoubleArrayMembersDiffer() {
		descriptor.setValue( "doubles", new double[] { } );
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsFloatArrayMembersDiffer() {
		descriptor.setValue( "floats", new float[] { } );
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsIntArrayMembersDiffer() {
		descriptor.setValue( "ints", new int[] { } );
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsLongArrayMembersDiffer() {
		descriptor.setValue( "longs", new long[] { } );
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsShortArrayMembersDiffer() {
		descriptor.setValue( "shorts", new short[] { } );
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsClassArrayMembersDiffer() {
		descriptor.setValue( "classes", new Class<?>[] { } );
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testHashCode() {
		MyAnno proxiedAnnotation = AnnotationFactory.create( descriptor );

		assertThat( proxiedAnnotation.hashCode() ).isEqualTo( realAnnotation.hashCode() );
	}

	/**
	 * Returns an {@link AnnotationDescriptor} representing the given annotation.
	 *
	 * @param annotation The annotation to represent.
	 *
	 * @return A descriptor for the given annotation.
	 */
	private <A extends Annotation> AnnotationDescriptor<A> getDescriptorFromAnnotation(A annotation) {
		@SuppressWarnings("unchecked")
		Class<A> annotationType = (Class<A>) annotation.annotationType();

		AnnotationDescriptor<A> descriptor = new AnnotationDescriptor<A>( annotationType );

		for ( Method method : GetDeclaredMethods.action( annotationType ).run() ) {
			try {
				descriptor.setValue( method.getName(), method.invoke( annotation ) );
			}
			catch ( RuntimeException e ) {
				throw e;
			}
			catch ( Exception e ) {
				throw new RuntimeException( e );
			}
		}

		return descriptor;
	}

	@MyAnno
	private static class Foo {
	}

	private static class Bar {
		@MyAnno(double_ = Double.NaN)
		public void nanDouble() {
		}

		@MyAnno(double_ = 0.0)
		public void zeroDouble() {
		}

		@MyAnno(float_ = Float.NaN)
		public void nanFloat() {
		}

		@MyAnno(float_ = 0.0f)
		public void zeroFloat() {
		}
	}


	@MyOtherAnno
	private static class Baz {
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface MyAnno {

		//primitive types
		boolean boolean_() default false;

		byte byte_() default Byte.MAX_VALUE;

		char char_() default 'A';

		double double_() default Double.MAX_VALUE;

		float float_() default Float.MAX_VALUE;

		int int_() default Integer.MAX_VALUE;

		long long_() default Long.MAX_VALUE;

		short short_() default Short.MAX_VALUE;

		//reference types
		String string() default "Foo";

		MyEnum myEnum() default MyEnum.FOO;

		Class<?> class_() default Object.class;

		MyOtherAnno myOtherAnno() default @MyOtherAnno;

		//primitive array types
		boolean[] booleans() default { false, true };

		byte[] bytes() default { Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE };

		char[] chars() default { 'A', 'B', 'C' };

		double[] doubles() default {
				Double.NEGATIVE_INFINITY,
				Double.MIN_VALUE,
				-1,
				0,
				1,
				Double.MAX_VALUE,
				Double.POSITIVE_INFINITY,
				Double.NaN
		};

		float[] floats() default {
				Float.NEGATIVE_INFINITY,
				Float.MIN_VALUE,
				-1,
				0,
				1,
				Float.MAX_VALUE,
				Float.POSITIVE_INFINITY,
				Float.NaN
		};

		int[] ints() default { Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE };

		long[] longs() default { Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE };

		short[] shorts() default { Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE };

		//reference array types
		String[] strings() default { "Foo", "Bar" };

		MyEnum[] myEnums() default { MyEnum.FOO, MyEnum.BAR };

		Class<?>[] classes() default { Object.class, Integer.class };

		MyOtherAnno[] myOtherAnnos() default { @MyOtherAnno, @MyOtherAnno };
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface MyOtherAnno {

		//primitive types
		boolean boolean_() default false;

		byte byte_() default Byte.MAX_VALUE;

		char char_() default 'A';

		double double_() default Double.MAX_VALUE;

		float float_() default Float.MAX_VALUE;

		int int_() default Integer.MAX_VALUE;

		long long_() default Long.MAX_VALUE;

		short short_() default Short.MAX_VALUE;

		//reference types
		String string() default "Foo";

		MyEnum myEnum() default MyEnum.FOO;

		Class<?> class_() default Object.class;

		//primitive array types
		boolean[] booleans() default { false, true };

		byte[] bytes() default { Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE };

		char[] chars() default { 'A', 'B', 'C' };

		double[] doubles() default {
				Double.NEGATIVE_INFINITY,
				Double.MIN_VALUE,
				-1,
				0,
				1,
				Double.MAX_VALUE,
				Double.POSITIVE_INFINITY,
				Double.NaN
		};

		float[] floats() default {
				Float.NEGATIVE_INFINITY,
				Float.MIN_VALUE,
				-1,
				0,
				1,
				Float.MAX_VALUE,
				Float.POSITIVE_INFINITY,
				Float.NaN
		};

		int[] ints() default { Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE };

		long[] longs() default { Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE };

		short[] shorts() default { Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE };

		//reference array types
		String[] strings() default { "Foo", "Bar" };

		MyEnum[] myEnums() default { MyEnum.FOO, MyEnum.BAR };

		Class<?>[] classes() default { Object.class, Integer.class };
	}

	public enum MyEnum {
		FOO, BAR;
	}
}
