/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.overriding;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

import java.lang.annotation.Target;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

public class MethodOverridingTests {

	/**
	 * Case 1 : annotation on overridden method parameters - incorrect
	 */
	public static class MethodOverridingTestCase1 {
		public void doSomething(String param) {

		}
	}

	public static class MethodOverridingTestCase1Sub extends MethodOverridingTestCase1 {

		@Override
		public void doSomething(@NotNull String param) {
			super.doSomething( param );
		}
	}


	/**
	 * Case 2 : more annotation on overridden method parameters then on the parent - incorrect
	 */
	public static class MethodOverridingTestCase2 {
		public void doSomething(@NotBlank String param) {

		}
	}

	public static class MethodOverridingTestCase2Sub extends MethodOverridingTestCase2 {

		@Override
		public void doSomething(@NotNull @NotBlank @Size(max = 10) String param) {
			super.doSomething( param );
		}
	}


	/**
	 * Case 3 : Implementing interface with adding constraints on parameters - incorrect
	 */
	public interface MethodOverridingTestCase3 {
		void doSomething(String param);
	}

	public static class MethodOverridingTestCase3Sub implements MethodOverridingTestCase3 {

		@Override
		public void doSomething(@NotNull @NotBlank @Size(max = 10) String param) {

		}
	}

	/**
	 * Case 4 : Two interfaces with same method - one with parameter constraints other without - incorrect
	 */
	public interface MethodOverridingTestCase4Interface {
		void doSomething(@NotBlank String param);
	}

	public interface MethodOverridingTestCase4OtherInterface {
		void doSomething(String param);
	}

	public static class MethodOverridingTestCase4Sub implements MethodOverridingTestCase4Interface, MethodOverridingTestCase4OtherInterface {

		@Override
		public void doSomething(@NotBlank String param) {

		}
	}

	/**
	 * Case 5 : Implementing interface with adding constraints on return value - correct
	 */
	public interface MethodOverridingTestCase5 {
		String doSomething(String param);
	}

	public static class MethodOverridingTestCase5Sub implements MethodOverridingTestCase5 {

		@Override
		@NotBlank
		@Size(max = 10)
		public String doSomething(String param) {
			return "";
		}
	}

	/**
	 * Case 6 : Implementing interface with removing constraints on return value - incorrect
	 */
	public interface MethodOverridingTestCase6 {
		@NotBlank
		@Size(max = 10)
		String doSomething(String param);
	}

	public static class MethodOverridingTestCase6Sub implements MethodOverridingTestCase6 {

		@NotBlank
		@Override
		public String doSomething(String param) {
			return "";
		}
	}

	/**
	 * Case 7 : Deeper hierarchy: Implementing interfaces with different constraints on parameter - incorrect
	 */
	public interface MethodOverridingTestCase7Interface {
		String doSomething(@NotBlank @Size(max = 10) String param);
	}

	public interface MethodOverridingTestCase7OtherInterface {
		String doSomething(@NotBlank String param);
	}

	public static class MethodOverridingTestCase7Sub implements MethodOverridingTestCase7Interface {

		@Override
		public String doSomething(@NotBlank String param) {
			return "";
		}
	}

	public static class MethodOverridingTestCase7SubSub extends MethodOverridingTestCase7Sub implements MethodOverridingTestCase7OtherInterface {

		@Override
		public String doSomething(@NotBlank String param) {
			return "";
		}
	}

	/**
	 * Case 8: case from documentation - Illegally declared parameter constraints on interface implementation - incorrect
	 */
	public static class Case8 {
		public interface OrderService {

			void placeOrder(String customerCode, String item, int quantity);
		}

		public static class SimpleOrderService implements OrderService {

			@Override
			public void placeOrder(
					@NotNull @Size(min = 3, max = 20) String customerCode,
					@NotNull String item,
					@Min(1) int quantity) {

			}
		}
	}

	/**
	 * Case 9: case from documentation - Illegally declared parameter constraints on sub class - incorrect:
	 */
	public static class Case9 {
		public class OrderService {

			void placeOrder(String customerCode, String item, int quantity) {
			}
		}

		public class SimpleOrderService extends OrderService {

			@Override
			public void placeOrder(
					@NotNull @Size(min = 3, max = 20) String customerCode,
					@NotNull String item,
					@Min(1) int quantity) {
			}
		}
	}

	/**
	 * Case 10: case from documentation - Illegally declared parameter constraints on parallel types - incorrect:
	 */
	public static class Case10 {
		public interface OrderService {

			void placeOrder(String customerCode, String item, int quantity);
		}

		public interface OrderPlacementService {

			void placeOrder(
					@NotNull @Size(min = 3, max = 20) String customerCode,
					@NotNull String item,
					@Min(1) int quantity);
		}

		public static class SimpleOrderService implements OrderService, OrderPlacementService {

			@Override
			public void placeOrder(String customerCode, String item, int quantity) {
			}
		}
	}

	/**
	 * Case 11: some other annotation is present but it should not affect correctness - correct:
	 */
	public static class Case11 {

		@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
		public @interface SomeAnnotation {

		}

		public class OrderService {

			void placeOrder(
					@NotNull @Size(min = 3, max = 20) String customerCode,
					@NotNull String item,
					@Min(1) int quantity) {
			}

			@Min(0)
			@SomeAnnotation // this should not affect the check
			int getOrderId() {
				return 1;
			}
		}

		public class SimpleOrderService extends OrderService {

			@Override
			public void placeOrder(
					@SomeAnnotation String customerCode,
					String item,
					int quantity) {
			}

			@Min(0)
			@Max(10)
			@Override
			int getOrderId() {
				return 10;
			}
		}

	}

	/**
	 * Case 12: Correct parallel implementation in deep hierarchy - correct
	 */
	public static class Case12 {

		public interface SimpleOrderService {
			void placeOrder(
					@NotNull @Size(min = 3, max = 20) String customerCode,
					@NotNull String item,
					@Min(1) int quantity);
		}

		public class SimpleOrderServiceImpl implements SimpleOrderService {
			@Override
			public void placeOrder(String customerCode, String item, int quantity) {

			}
		}

		public interface SomeOtherOrderService {
			void placeOrder(
					@NotNull @Size(min = 3, max = 20) String customerCode,
					@NotNull String item,
					@Min(1) int quantity);
		}

		public class SomeOtherOrderServiceStrangeImpl extends SimpleOrderServiceImpl implements SomeOtherOrderService {
			@Override
			public void placeOrder(String customerCode, String item, int quantity) {

			}
		}

	}

	/**
	 * Case 13: Correct parallel implementation in deep hierarchy with a class at the top of the hierarchy
	 * where a method is originally declared - correct
	 */
	public static class Case13 {

		public class BaseOrderServiceImpl {
			public void placeOrder(
					@NotNull @Size(min = 3, max = 20) String customerCode,
					@NotNull String item,
					@Min(1) int quantity) {

			}
		}

		public interface SimpleOrderService {
			void placeOrder(
					@NotNull @Size(min = 3, max = 20) String customerCode,
					@NotNull String item,
					@Min(1) int quantity);
		}

		public class SimpleOrderServiceImpl extends BaseOrderServiceImpl implements SimpleOrderService {
			@Override
			public void placeOrder(String customerCode, String item, int quantity) {

			}
		}

		public interface SomeOtherOrderService {
			void placeOrder(
					@NotNull @Size(min = 3, max = 20) String customerCode,
					@NotNull String item,
					@Min(1) int quantity);
		}

		public class SomeOtherOrderServiceStrangeImpl extends SimpleOrderServiceImpl implements SomeOtherOrderService {
			@Override
			public void placeOrder(String customerCode, String item, int quantity) {

			}
		}

	}


	/**
	 * Case 14: Incorrect parallel implementation in deep hierarchy with a class at the top of the hierarchy
	 * where a method is originally declared. BaseOrderServiceImpl is missing one of the annotations on parameter - incorrect
	 */
	public static class Case14 {

		public class BaseOrderServiceImpl {
			public void placeOrder(
					@NotNull String customerCode,
					@NotNull String item,
					@Min(1) int quantity) {

			}
		}

		public interface SimpleOrderService {
			void placeOrder(
					@NotNull @Size(min = 3, max = 20) String customerCode,
					@NotNull String item,
					@Min(1) int quantity);
		}

		public class SimpleOrderServiceImpl extends BaseOrderServiceImpl implements SimpleOrderService {
			@Override
			public void placeOrder(@NotNull String customerCode, String item, int quantity) {

			}
		}

		public interface SomeOtherOrderService {
			void placeOrder(
					@NotNull @Size(min = 3, max = 20) String customerCode,
					@NotNull String item,
					@Min(1) int quantity);
		}

		public class SomeOtherOrderServiceStrangeImpl extends SimpleOrderServiceImpl implements SomeOtherOrderService {
			@Override
			public void placeOrder(String customerCode, String item, int quantity) {

			}
		}

	}
}

