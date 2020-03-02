/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * HV-250
 */
public class ClassValidatorWithTypeVariableTest {

	private Validator validator;

	@BeforeClass
	public void setUp() {
		validator = ValidatorUtil.getValidator();
	}

	@Test
	public void offersNull() {
		Batch batch = new Batch( null );

		Set<ConstraintViolation<Batch>> violations = validator.validate( batch );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "offers" )
		);
	}

	@Test
	public void offerItemNull() {
		ItemAOffer offer = new ItemAOffer( null );
		Set<ItemOffer<? extends Item>> offers = new HashSet<ItemOffer<? extends Item>>();
		offers.add( offer );
		Batch batch = new Batch( offers );

		Set<ConstraintViolation<Batch>> violations = validator.validate( batch );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "offers" )
								.property( "item", true, null, null, Set.class, 0 )
						)
		);
	}

	@Test
	public void offerItemDateNull() {
		ItemA item = new ItemA( null );
		ItemOffer<? extends Item> offer = new ItemAOffer( item );
		Set<ItemOffer<? extends Item>> offers = new HashSet<ItemOffer<? extends Item>>();
		offers.add( offer );
		Batch batch = new Batch( offers );

		Set<ConstraintViolation<Batch>> violations = validator.validate( batch );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "offers" )
								.property( "item", true, null, null, Set.class, 0 )
								.property( "date" )
						)
		);
	}

	@SuppressWarnings("unused")
	private class Batch {
		@NotNull
		@Valid
		private Set<ItemOffer<? extends Item>> offers = new HashSet<ItemOffer<? extends Item>>();

		public Batch(Set<ItemOffer<? extends Item>> offers) {
			this.offers = offers;
		}
	}

	@SuppressWarnings("unused")
	private abstract class Item {
		@NotNull
		private Date date;

		public Item(Date date) {
			this.date = date;
		}
	}

	@SuppressWarnings("unused")
	private abstract class ItemOffer<T extends Item> {
		@NotNull
		@Valid
		private T item;

		public ItemOffer(T item) {
			this.item = item;
		}
	}

	private class ItemA extends Item {
		public ItemA(Date date) {
			super( date );
		}
	}

	private class ItemAOffer extends ItemOffer<ItemA> {
		public ItemAOffer(ItemA item) {
			super( item );
		}
	}
}


