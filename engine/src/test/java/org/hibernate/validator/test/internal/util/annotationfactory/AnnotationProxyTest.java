/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util.annotationfactory;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 */
public class AnnotationProxyTest {

	private MyAnno realAnnotation;

	private AnnotationDescriptor.Builder<MyAnno> descriptorBuilder;

	@BeforeMethod
	public void setupAnnotations() throws Exception {
		realAnnotation = Foo.class.getAnnotation( MyAnno.class );
		descriptorBuilder = getDescriptorBuilderFromAnnotation( realAnnotation );
	}

	@Test
	public void testEqualsAnnotationsAreEqual() {
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isEqualTo( realAnnotation );
		assertThat( realAnnotation ).isEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsAnnotationsAreSame() {
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsCheckNull() {
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( null );
	}

	@Test
	public void testEqualsAnnotationTypesDiffer() {
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		MyOtherAnno anotherAnnotation = Baz.class.getAnnotation( MyOtherAnno.class );
		AnnotationDescriptor.Builder<MyOtherAnno> anotherDescriptor = getDescriptorBuilderFromAnnotation(
				anotherAnnotation
		);
		MyOtherAnno anotherProxiedAnnotation = anotherDescriptor.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( anotherProxiedAnnotation );
		assertThat( anotherProxiedAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( anotherProxiedAnnotation ).isNotEqualTo( realAnnotation );
	}

	@Test
	public void testEqualsPrimitiveMembersDiffer() {
		descriptorBuilder.setValue( "int_", 42 );
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsDoubleNaNEqualsItself() throws Exception {
		realAnnotation = Bar.class.getMethod( "nanDouble" ).getAnnotation( MyAnno.class );
		descriptorBuilder = getDescriptorBuilderFromAnnotation( realAnnotation );

		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isEqualTo( realAnnotation );
		assertThat( realAnnotation ).isEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsDoubleZeroNotEqualToMinusZero() throws Exception {
		realAnnotation = Bar.class.getMethod( "zeroDouble" ).getAnnotation( MyAnno.class );
		descriptorBuilder = getDescriptorBuilderFromAnnotation( realAnnotation );
		descriptorBuilder.setValue( "double_", -0.0 );

		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsFloatNaNEqualsItself() throws Exception {
		realAnnotation = Bar.class.getMethod( "nanFloat" ).getAnnotation( MyAnno.class );
		descriptorBuilder = getDescriptorBuilderFromAnnotation( realAnnotation );

		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isEqualTo( realAnnotation );
		assertThat( realAnnotation ).isEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsFloatZeroNotEqualToMinusZero() throws Exception {
		realAnnotation = Bar.class.getMethod( "zeroFloat" ).getAnnotation( MyAnno.class );
		descriptorBuilder = getDescriptorBuilderFromAnnotation( realAnnotation );
		descriptorBuilder.setValue( "float_", -0.0f );

		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsStringMembersDiffer() {
		descriptorBuilder.setValue( "string", "Bar" );
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsClassMembersDiffer() {
		descriptorBuilder.setValue( "class_", Integer.class );
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsBooleanArrayMembersDiffer() {
		descriptorBuilder.setValue( "booleans", new boolean[] { } );
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsByteArrayMembersDiffer() {
		descriptorBuilder.setValue( "bytes", new byte[] { } );
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsCharArrayMembersDiffer() {
		descriptorBuilder.setValue( "chars", new char[] { } );
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsDoubleArrayMembersDiffer() {
		descriptorBuilder.setValue( "doubles", new double[] { } );
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsFloatArrayMembersDiffer() {
		descriptorBuilder.setValue( "floats", new float[] { } );
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsIntArrayMembersDiffer() {
		descriptorBuilder.setValue( "ints", new int[] { } );
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsLongArrayMembersDiffer() {
		descriptorBuilder.setValue( "longs", new long[] { } );
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsShortArrayMembersDiffer() {
		descriptorBuilder.setValue( "shorts", new short[] { } );
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testEqualsClassArrayMembersDiffer() {
		descriptorBuilder.setValue( "classes", new Class<?>[] { } );
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation ).isNotEqualTo( realAnnotation );
		assertThat( realAnnotation ).isNotEqualTo( proxiedAnnotation );
		assertThat( proxiedAnnotation ).isEqualTo( proxiedAnnotation );
	}

	@Test
	public void testHashCode() {
		MyAnno proxiedAnnotation = descriptorBuilder.build().annotation();

		assertThat( proxiedAnnotation.hashCode() ).isEqualTo( realAnnotation.hashCode() );
	}

	/**
	 * Returns an {@link AnnotationDescriptor} representing the given annotation.
	 *
	 * @param annotation The annotation to represent.
	 *
	 * @return A descriptor for the given annotation.
	 */
	private <A extends Annotation> AnnotationDescriptor.Builder<A> getDescriptorBuilderFromAnnotation(A annotation) {
		return new AnnotationDescriptor.Builder<A>( annotation );
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
