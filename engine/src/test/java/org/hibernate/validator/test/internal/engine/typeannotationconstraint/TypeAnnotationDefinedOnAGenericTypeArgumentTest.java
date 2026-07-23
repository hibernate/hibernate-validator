/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.typeannotationconstraint;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Arrays;
import java.util.List;

import jakarta.validation.UnexpectedTypeException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Guillaume Smet
 */
@TestForIssue(jiraKey = "HV-1279")
public class TypeAnnotationDefinedOnAGenericTypeArgumentTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	public void constraintOnGenericTypeArgumentOfArrayFieldThrowsException() {
		assertThatThrownBy( () -> validator.validate( new GenericArrayEntity<>( new String[] { "Too long" } ) ) )
				.isInstanceOf( UnexpectedTypeException.class )
				.hasMessageMatching( "HV000030:.*" );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	public void constraintOnGenericTypeArgumentOfArrayGetterThrowsException() {
		assertThatThrownBy( () -> validator.validate( new GenericArrayWithGetterEntity<>( new String[] { "Too long" } ) ) )
				.isInstanceOf( UnexpectedTypeException.class )
				.hasMessageMatching( "HV000030:.*" );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	public void constraintOnGenericTypeArgumentOfArrayParameterThrowsException() {
		assertThatThrownBy( () -> {
			IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );
			fishTank.test2( new String[] { "Too long" } );
		} ).isInstanceOf( UnexpectedTypeException.class )
				.hasMessageMatching( "HV000030:.*" );
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	public void constraintOnGenericTypeArgumentOfArrayReturnValueThrowsException() {
		assertThatThrownBy( () -> {
			IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );
			fishTank.test4( new String[] { "Too long" } );
		} ).isInstanceOf( UnexpectedTypeException.class )
				.hasMessageMatching( "HV000030:.*" );
	}

	@Test
	public void constraintOnGenericTypeArgumentOfListFieldThrowsException() {
		assertThatThrownBy( () -> validator.validate( new GenericListEntity<>( Arrays.asList( "Too long" ) ) ) )
				.isInstanceOf( UnexpectedTypeException.class )
				.hasMessageMatching( "HV000030:.*" );
	}

	@Test
	public void constraintOnGenericTypeArgumentOfListGetterThrowsException() {
		assertThatThrownBy( () -> validator.validate( new GenericListWithGetterEntity<>( Arrays.asList( "Too long" ) ) ) )
				.isInstanceOf( UnexpectedTypeException.class )
				.hasMessageMatching( "HV000030:.*" );
	}

	@Test
	public void constraintOnGenericTypeArgumentOfListParameterThrowsException() {
		assertThatThrownBy( () -> {
			IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );
			fishTank.test1( Arrays.asList( "Too long" ) );
		} ).isInstanceOf( UnexpectedTypeException.class )
				.hasMessageMatching( "HV000030:.*" );
	}

	@Test
	public void constraintOnGenericTypeArgumentOfListReturnValueThrowsException() {
		assertThatThrownBy( () -> {
			IFishTank fishTank = ValidatorUtil.getValidatingProxy( new FishTank(), validator );
			fishTank.test3( Arrays.asList( "Too long" ) );
		} ).isInstanceOf( UnexpectedTypeException.class )
				.hasMessageMatching( "HV000030:.*" );
	}

	@SuppressWarnings("unused")
	private static class GenericArrayEntity<T> {

		private T @Length(max = 5) [] array;

		private GenericArrayEntity(T[] array) {
			this.array = array;
		}
	}

	@SuppressWarnings("unused")
	private static class GenericArrayWithGetterEntity<T> {

		private T[] arrayWithGetter;

		private GenericArrayWithGetterEntity(T[] array) {
			this.arrayWithGetter = array;
		}

		public T @Length(max = 5) [] getArrayWithGetter() {
			return arrayWithGetter;
		}
	}

	@SuppressWarnings("unused")
	private static class GenericListEntity<T> {

		private List<@Length(max = 5) T> list;

		private GenericListEntity(List<T> list) {
			this.list = list;
		}
	}

	@SuppressWarnings("unused")
	private static class GenericListWithGetterEntity<T> {

		private List<T> listWithGetter;

		private GenericListWithGetterEntity(List<T> list) {
			this.listWithGetter = list;
		}

		public List<@Length(max = 5) T> getListWithGetter() {
			return listWithGetter;
		}
	}

	public interface IFishTank {
		<T> void test1(List<T> fishNames);

		<T> void test2(T[] fishNames);

		<T> List<T> test3(List<T> list);

		<T> T[] test4(T[] array);
	}

	private static class FishTank implements IFishTank {

		@Override
		public <T> void test1(List<@Size(min = 5) T> fishNames) {
		}

		@Override
		public <T> void test2(T @Size(min = 5) [] fishNames) {
		}

		@Override
		public <T> List<@Size(min = 5) T> test3(List<T> list) {
			return list;
		}

		@Override
		public <T> T @Size(min = 5) [] test4(T[] array) {
			return array;
		}
	}
}
