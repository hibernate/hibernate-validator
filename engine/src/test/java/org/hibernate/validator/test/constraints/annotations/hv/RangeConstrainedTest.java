/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.money.MonetaryAmount;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.cfg.defs.MaxDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.RangeDef;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.internal.constraintvalidators.hv.RangeValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.hv.RangeValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.hv.RangeValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.hv.RangeValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.hv.RangeValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.hv.RangeValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.hv.RangeValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.hv.RangeValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.hv.RangeValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.hv.RangeValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.hv.RangeValidatorForShort;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.assertj.core.api.Assertions;
import org.javamoney.moneta.Money;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class RangeConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testRangeNumber() {
		Foo foo = new Foo(
				"2", BigDecimal.ONE.add( BigDecimal.ONE ), BigInteger.valueOf( 2L ),
				2.0, 2.0f, 2L, 2, (byte) 2, (short) 2, 2, Money.of( 2, "UAH" )
		);
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testRangeInvalid() {
		Foo foo = new Foo(
				"0", BigDecimal.ZERO, BigInteger.ZERO,
				0.0, 0.0f, 0L, 0, (byte) 0, (short) 0, 0, Money.of( 0, "UAH" )
		);
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Range.class ).withProperty( "string" ),
				violationOf( Range.class ).withProperty( "bigDecimal" ),
				violationOf( Range.class ).withProperty( "bigInteger" ),
				violationOf( Range.class ).withProperty( "aDouble" ),
				violationOf( Range.class ).withProperty( "aFloat" ),
				violationOf( Range.class ).withProperty( "aLong" ),
				violationOf( Range.class ).withProperty( "anInt" ),
				violationOf( Range.class ).withProperty( "aByte" ),
				violationOf( Range.class ).withProperty( "aShort" ),
				violationOf( Range.class ).withProperty( "number" ),
				violationOf( Range.class ).withProperty( "amount" )
		);
	}

	@Test
	public void testStandaloneConstraint() {
		assertTrue( validator.getConstraintsForClass( Foo.class )
				.getConstraintsForProperty( "number" ).getConstraintDescriptors().iterator().next()
				.getComposingConstraints().isEmpty() );
	}

	@Test(dataProvider = "numbers")
	public void testNumericRangesMatchMinAndMax(Number number) {
		assertEquivalent( NumericRanges.class, number );
		assertEquals( validator.validateValue( NumericRanges.class, "roundedInvertedRange", number ).isEmpty(),
				validator.validateValue( NumericRanges.class, "roundedInvertedComposed", number ).isEmpty() );
	}

	@DataProvider
	public Object[][] numbers() {
		return new Object[][] {
				{ null },
				{ (byte) 1 },
				{ (short) 1 },
				{ 1 },
				{ 1L },
				{ 1.0f },
				{ 1.0 },
				{ -1 },
				{ 0 },
				{ -0.0 },
				{ -0.0f },
				{ 0.0 },
				{ 0.0f },
				{ 3 },
				{ 4 },
				{ Long.MIN_VALUE },
				{ Long.MIN_VALUE + 1 },
				{ Long.MAX_VALUE - 1 },
				{ Long.MAX_VALUE },
				{ (double) Long.MIN_VALUE },
				{ (double) Long.MAX_VALUE },
				{ Math.nextDown( (double) Long.MIN_VALUE ) },
				{ Math.nextUp( (double) Long.MAX_VALUE ) },
				{ (float) Long.MIN_VALUE },
				{ (float) Long.MAX_VALUE },
				{ Double.NaN },
				{ Double.NEGATIVE_INFINITY },
				{ Double.POSITIVE_INFINITY },
				{ Float.NaN },
				{ Float.NEGATIVE_INFINITY },
				{ Float.POSITIVE_INFINITY },
				{ new BigDecimal( "0.0000000000000000001" ) },
				{ new BigDecimal( "9223372036854775807.000000000000000001" ) },
				{ BigDecimal.valueOf( Long.MIN_VALUE ).subtract( BigDecimal.ONE ) },
				{ BigInteger.valueOf( Long.MIN_VALUE ).subtract( BigInteger.ONE ) },
				{ BigInteger.valueOf( Long.MAX_VALUE ).add( BigInteger.ONE ) },
				{ BigInteger.valueOf( Long.MAX_VALUE ) },
				{ new AtomicInteger( 2 ) },
				{ new AtomicLong( Long.MAX_VALUE ) },
				{ new CustomNumber( 2.0 ) },
				{ new CustomNumber( Double.NaN ) },
				{ new CustomNumber( Double.POSITIVE_INFINITY ) }
		};
	}

	@Test(dataProvider = "numericText")
	public void testTextRangesMatchMinAndMax(CharSequence text) {
		assertEquivalent( TextRanges.class, text );
	}

	@DataProvider
	public Object[][] numericText() {
		return new Object[][] {
				{ null },
				{ "" },
				{ "0" },
				{ "-0.0" },
				{ "2" },
				{ "+2" },
				{ "2e0" },
				{ "٢" },
				{ "２" },
				{ new StringBuilder( "2.0" ) },
				{ " 2" },
				{ "2 " },
				{ "NaN" },
				{ "Infinity" },
				{ "1e100" },
				{ ".5" },
				{ "9223372036854775807" },
				{ "9223372036854775807.000000000000001" },
				{ "-9223372036854775808" },
				{ "-9223372036854775809" }
		};
	}

	@Test
	public void testMonetaryRangesMatchMinAndMax() {
		for ( MonetaryAmount amount : new MonetaryAmount[] {
				null,
				Money.of( -1, "EUR" ),
				Money.of( 0, "EUR" ),
				Money.of( 2, "EUR" ),
				Money.of( 4, "EUR" ),
				Money.of( new BigDecimal( "9223372036854775807" ), "EUR" ),
				Money.of( new BigDecimal( "9223372036854775808" ), "EUR" ),
				Money.of( new BigDecimal( "-9223372036854775809" ), "EUR" ) } ) {
			assertEquivalent( MonetaryRanges.class, amount );
		}
	}

	@DataProvider
	public Object[][] declaredTypes() {
		Object[] bytes = { null, Byte.MIN_VALUE, (byte) -2, (byte) 0, (byte) 1, (byte) 3, (byte) 4, Byte.MAX_VALUE };
		Object[] shorts = { null, Short.MIN_VALUE, (short) -2, (short) 0, (short) 1, (short) 3, (short) 4, Short.MAX_VALUE };
		Object[] integers = { null, Integer.MIN_VALUE, -2, 0, 1, 3, 4, Integer.MAX_VALUE };
		Object[] longs = { null, Long.MIN_VALUE, Long.MIN_VALUE + 1, -2L, 0L, 1L, 3L, 4L, Long.MAX_VALUE - 1, Long.MAX_VALUE };
		Object[] floats = {
				null,
				-0.0f,
				0.0f,
				-2.0f,
				3.0f,
				Math.nextUp( 3.0f ),
				Float.NaN,
				Float.NEGATIVE_INFINITY,
				Float.POSITIVE_INFINITY,
				(float) Long.MIN_VALUE,
				(float) Long.MAX_VALUE,
				Math.nextDown( (float) Long.MIN_VALUE ),
				Math.nextUp( (float) Long.MAX_VALUE ) };
		Object[] doubles = {
				null,
				-0.0,
				0.0,
				-2.0,
				3.0,
				Math.nextUp( 3.0 ),
				Double.NaN,
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				(double) Long.MIN_VALUE,
				(double) Long.MAX_VALUE,
				Math.nextDown( (double) Long.MIN_VALUE ),
				Math.nextUp( (double) Long.MAX_VALUE ) };
		return new Object[][] {
				{
						"decimal",
						RangeValidatorForBigDecimal.class,
						new Object[] {
								null,
								BigDecimal.ZERO,
								new BigDecimal( "-2" ),
								new BigDecimal( "3" ),
								new BigDecimal( "3.000000000000000001" ),
								BigDecimal.valueOf( Long.MIN_VALUE ),
								BigDecimal.valueOf( Long.MAX_VALUE ),
								new BigDecimal( "9223372036854775807.000000000000000001" ),
								new BigDecimal( "-9223372036854775808.000000000000000001" ) } },
				{
						"integer",
						RangeValidatorForBigInteger.class,
						new Object[] {
								null,
								BigInteger.ZERO,
								BigInteger.valueOf( -2 ),
								BigInteger.valueOf( 3 ),
								BigInteger.valueOf( 4 ),
								BigInteger.valueOf( Long.MIN_VALUE ),
								BigInteger.valueOf( Long.MAX_VALUE ),
								BigInteger.valueOf( Long.MIN_VALUE ).subtract( BigInteger.ONE ),
								BigInteger.valueOf( Long.MAX_VALUE ).add( BigInteger.ONE ) } },
				{ "byte", RangeValidatorForByte.class, bytes },
				{ "short", RangeValidatorForShort.class, shorts },
				{ "int", RangeValidatorForInteger.class, integers },
				{ "long", RangeValidatorForLong.class, longs },
				{ "float", RangeValidatorForFloat.class, floats },
				{ "double", RangeValidatorForDouble.class, doubles },
				{
						"number",
						RangeValidatorForNumber.class,
						new Object[] {
								null,
								new CustomNumber( 2.0 ),
								new CustomNumber( Double.NaN ),
								new CustomNumber( Double.POSITIVE_INFINITY ),
								new AtomicInteger( 2 ),
								new AtomicLong( Long.MAX_VALUE ),
								BigInteger.valueOf( Long.MAX_VALUE ).add( BigInteger.ONE ),
								new BigDecimal( "9223372036854775807.00000000001" ),
								-0.0f } },
				{
						"text",
						RangeValidatorForCharSequence.class,
						new Object[] {
								null,
								"0",
								"-2",
								"3",
								"3.00000000000000001",
								"٢",
								new StringBuilder( "2.0" ),
								"",
								"NaN",
								" 2",
								"9223372036854775807",
								"9223372036854775808" } },
				{
						"money",
						RangeValidatorForMonetaryAmount.class,
						new Object[] {
								null,
								Money.of( -2, "EUR" ),
								Money.of( 0, "EUR" ),
								Money.of( 3, "EUR" ),
								Money.of( 4, "EUR" ),
								Money.of( new BigDecimal( "9223372036854775807" ), "EUR" ),
								Money.of( new BigDecimal( "9223372036854775808" ), "EUR" ) } },
				{ "primitiveByte", RangeValidatorForByte.class, Arrays.copyOfRange( bytes, 1, bytes.length ) },
				{ "primitiveShort", RangeValidatorForShort.class, Arrays.copyOfRange( shorts, 1, shorts.length ) },
				{ "primitiveInt", RangeValidatorForInteger.class, Arrays.copyOfRange( integers, 1, integers.length ) },
				{ "primitiveLong", RangeValidatorForLong.class, Arrays.copyOfRange( longs, 1, longs.length ) },
				{ "primitiveFloat", RangeValidatorForFloat.class, Arrays.copyOfRange( floats, 1, floats.length ) },
				{ "primitiveDouble", RangeValidatorForDouble.class, Arrays.copyOfRange( doubles, 1, doubles.length ) }
		};
	}

	@Test(dataProvider = "declaredTypes")
	public void testDeclaredTypesSelectDedicatedValidatorsAndMatchMinMax(String prefix, Class<?> expectedValidator, Object[] values) {
		var configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		var mapping = configuration.createConstraintMapping();
		var type = mapping.type( DeclaredRanges.class );
		var range = type.field( prefix + "Range" );
		var composed = type.field( prefix + "Composed" );
		RangeBounds[] bounds = {
				new RangeBounds( DefaultBounds.class, 0, Long.MAX_VALUE ),
				new RangeBounds( FullBounds.class, Long.MIN_VALUE, Long.MAX_VALUE ),
				new RangeBounds( ZeroBounds.class, 0, 0 ),
				new RangeBounds( InvertedBounds.class, 5, 1 ),
				new RangeBounds( SmallBounds.class, -2, 3 ),
				new RangeBounds( RoundedInvertedBounds.class, Long.MAX_VALUE, Long.MAX_VALUE - 1 )
		};
		for ( RangeBounds bound : bounds ) {
			range.constraint( new RangeDef().min( bound.min() ).max( bound.max() ).groups( bound.group() ) );
			composed.constraint( new MinDef().value( bound.min() ).groups( bound.group() ) )
					.constraint( new MaxDef().value( bound.max() ).groups( bound.group() ) );
		}
		configuration.addMapping( mapping );
		RecordingValidatorFactory recorder = new RecordingValidatorFactory( configuration.getDefaultConstraintValidatorFactory() );
		configuration.constraintValidatorFactory( recorder );
		try ( ValidatorFactory factory = configuration.buildValidatorFactory() ) {
			var validator = factory.getValidator();
			for ( RangeBounds bound : bounds ) {
				for ( Object value : values ) {
					assertEquals( validator.validateValue( DeclaredRanges.class, prefix + "Range", value, bound.group() ).isEmpty(),
							validator.validateValue( DeclaredRanges.class, prefix + "Composed", value, bound.group() ).isEmpty(),
							prefix + ": " + bound.group().getSimpleName() + ": " + value );
				}
			}
			Assertions.assertThat( recorder.requestedValidators ).contains( expectedValidator );
		}
	}

	private record RangeBounds(Class<?> group, long min, long max) {
	}

	private interface DefaultBounds {
	}

	private interface FullBounds {
	}

	private interface ZeroBounds {
	}

	private interface InvertedBounds {
	}

	private interface SmallBounds {
	}

	private interface RoundedInvertedBounds {
	}

	private static class RecordingValidatorFactory implements ConstraintValidatorFactory {
		private final ConstraintValidatorFactory delegate;
		private final Set<Class<?>> requestedValidators = new HashSet<>();

		private RecordingValidatorFactory(ConstraintValidatorFactory delegate) {
			this.delegate = delegate;
		}

		@Override
		public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
			requestedValidators.add( key );
			return delegate.getInstance( key );
		}

		@Override
		public void releaseInstance(ConstraintValidator<?, ?> instance) {
			delegate.releaseInstance( instance );
		}
	}

	private static class DeclaredRanges {
		private BigDecimal decimalRange;
		private BigDecimal decimalComposed;

		private BigInteger integerRange;
		private BigInteger integerComposed;

		private Byte byteRange;
		private Byte byteComposed;

		private Short shortRange;
		private Short shortComposed;

		private Integer intRange;
		private Integer intComposed;

		private Long longRange;
		private Long longComposed;

		private Float floatRange;
		private Float floatComposed;

		private Double doubleRange;
		private Double doubleComposed;

		private Number numberRange;
		private Number numberComposed;

		private CharSequence textRange;
		private CharSequence textComposed;

		private MonetaryAmount moneyRange;
		private MonetaryAmount moneyComposed;

		private byte primitiveByteRange;
		private byte primitiveByteComposed;

		private short primitiveShortRange;
		private short primitiveShortComposed;

		private int primitiveIntRange;
		private int primitiveIntComposed;

		private long primitiveLongRange;
		private long primitiveLongComposed;

		private float primitiveFloatRange;
		private float primitiveFloatComposed;

		private double primitiveDoubleRange;
		private double primitiveDoubleComposed;

	}

	private void assertEquivalent(Class<?> beanClass, Object value) {
		for ( String prefix : new String[] { "default", "full", "zero", "inverted", "small" } ) {
			assertEquals( validator.validateValue( beanClass, prefix + "Range", value ).isEmpty(),
					validator.validateValue( beanClass, prefix + "Composed", value ).isEmpty(),
					beanClass.getSimpleName() + ": " + prefix + ": " + value );
		}
	}

	private static class CustomNumber extends Number {
		private static final long serialVersionUID = 1L;
		private final double value;

		private CustomNumber(double value) {
			this.value = value;
		}

		@Override
		public int intValue() {
			return -999;
		}

		@Override
		public long longValue() {
			return -999;
		}

		@Override
		public float floatValue() {
			return (float) value;
		}

		@Override
		public double doubleValue() {
			return value;
		}
	}

	private static class NumericRanges {

		@Range
		private Number defaultRange;

		@Min(0)
		@Max(Long.MAX_VALUE)
		private Number defaultComposed;

		@Range(min = Long.MIN_VALUE, max = Long.MAX_VALUE)
		private Number fullRange;

		@Min(Long.MIN_VALUE)
		@Max(Long.MAX_VALUE)
		private Number fullComposed;

		@Range(min = 0, max = 0)
		private Number zeroRange;

		@Min(0)
		@Max(0)
		private Number zeroComposed;

		@Range(min = 5, max = 1)
		private Number invertedRange;

		@Min(5)
		@Max(1)
		private Number invertedComposed;

		@Range(min = -2, max = 3)
		private Number smallRange;

		@Min(-2)
		@Max(3)
		private Number smallComposed;

		@Range(min = Long.MAX_VALUE, max = Long.MAX_VALUE - 1)
		private Number roundedInvertedRange;

		@Min(Long.MAX_VALUE)
		@Max(Long.MAX_VALUE - 1)
		private Number roundedInvertedComposed;
	}

	private static class TextRanges {

		@Range
		private CharSequence defaultRange;

		@Min(0)
		@Max(Long.MAX_VALUE)
		private CharSequence defaultComposed;

		@Range(min = Long.MIN_VALUE, max = Long.MAX_VALUE)
		private CharSequence fullRange;

		@Min(Long.MIN_VALUE)
		@Max(Long.MAX_VALUE)
		private CharSequence fullComposed;

		@Range(min = 0, max = 0)
		private CharSequence zeroRange;

		@Min(0)
		@Max(0)
		private CharSequence zeroComposed;

		@Range(min = 5, max = 1)
		private CharSequence invertedRange;

		@Min(5)
		@Max(1)
		private CharSequence invertedComposed;

		@Range(min = -2, max = 3)
		private CharSequence smallRange;

		@Min(-2)
		@Max(3)
		private CharSequence smallComposed;
	}

	private static class MonetaryRanges {

		@Range
		private MonetaryAmount defaultRange;

		@Min(0)
		@Max(Long.MAX_VALUE)
		private MonetaryAmount defaultComposed;

		@Range(min = Long.MIN_VALUE, max = Long.MAX_VALUE)
		private MonetaryAmount fullRange;

		@Min(Long.MIN_VALUE)
		@Max(Long.MAX_VALUE)
		private MonetaryAmount fullComposed;

		@Range(min = 0, max = 0)
		private MonetaryAmount zeroRange;

		@Min(0)
		@Max(0)
		private MonetaryAmount zeroComposed;

		@Range(min = 5, max = 1)
		private MonetaryAmount invertedRange;

		@Min(5)
		@Max(1)
		private MonetaryAmount invertedComposed;

		@Range(min = -2, max = 3)
		private MonetaryAmount smallRange;

		@Min(-2)
		@Max(3)
		private MonetaryAmount smallComposed;
	}

	private static class Foo {

		@Range(min = 1, max = 5)
		private final String string;

		@Range(min = 1, max = 5)
		private final BigDecimal bigDecimal;

		@Range(min = 1, max = 5)
		private final BigInteger bigInteger;

		@Range(min = 1, max = 5)
		private final double aDouble;

		@Range(min = 1, max = 5)
		private final float aFloat;

		@Range(min = 1, max = 5)
		private final long aLong;

		@Range(min = 1, max = 5)
		private final int anInt;

		@Range(min = 1, max = 5)
		private final byte aByte;

		@Range(min = 1, max = 5)
		private final short aShort;

		@Range(min = 1, max = 5)
		private final Number number;

		@Range(min = 1, max = 5)
		private final MonetaryAmount amount;

		public Foo(String string, BigDecimal bigDecimal,
				BigInteger bigInteger, double aDouble,
				float aFloat, long aLong, int anInt,
				byte aByte, short aShort,
				Number number, MonetaryAmount amount) {
			this.string = string;
			this.bigDecimal = bigDecimal;
			this.bigInteger = bigInteger;
			this.aDouble = aDouble;
			this.aFloat = aFloat;
			this.aLong = aLong;
			this.anInt = anInt;
			this.aByte = aByte;
			this.aShort = aShort;
			this.number = number;
			this.amount = amount;
		}
	}
}
