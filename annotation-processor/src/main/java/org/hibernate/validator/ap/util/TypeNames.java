/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.ap.util;

/**
 * Contains the fully-qualified names of types used by the annotation processor
 * which are accessed only via the mirror API in order to avoid references to
 * the actual class objects.
 *
 * @author Gunnar Morling
 */
public class TypeNames {

	private static String getName(String parent, String child) {
		return parent + "." + child;
	}

	public static class Javax {

		private static final String NAME = "javax";

		public static class Validation {

			private static final String NAME = getName( Javax.NAME, "validation" );

			public static final String CONSTRAINT = getName( NAME, "Constraint" );
			public static final String CONSTRAINT_VALIDATOR = getName( NAME, "ConstraintValidator" );
			public static final String GROUP_SEQUENCE = getName( NAME, "GroupSequence" );
			public static final String PAYLOAD = getName( NAME, "Payload" );
			public static final String VALID = getName( NAME, "Valid" );

			public static class Constraints {

				public static final String NAME = getName( Validation.NAME, "constraints" );

				public final static String ASSERT_FALSE = getName( NAME, "AssertFalse" );
				public final static String ASSERT_TRUE = getName( NAME, "AssertTrue" );
				public final static String DECIMAL_MAX = getName( NAME, "DecimalMax" );
				public final static String DECIMAL_MIN = getName( NAME, "DecimalMin" );
				public final static String DIGITS = getName( NAME, "Digits" );
				public final static String FUTURE = getName( NAME, "Future" );
				public final static String MAX = getName( NAME, "Max" );
				public final static String MIN = getName( NAME, "Min" );
				public final static String NOT_NULL = getName( NAME, "NotNull" );
				public final static String NULL = getName( NAME, "Null" );
				public final static String PAST = getName( NAME, "Past" );
				public final static String PATTERN = getName( NAME, "Pattern" );
				public final static String SIZE = getName( NAME, "Size" );
			}
		}
	}

	public static class Org {

		private static final String NAME = "org";

		public static class Hibernate {

			private static final String NAME = getName( Org.NAME, "hibernate" );

			public static class Validator {

				private static final String NAME = getName( Hibernate.NAME, "validator" );

				public static class Group {

					private static final String NAME = getName( Validator.NAME, "group" );

					public final static String GROUP_SEQUENCE_PROVIDER = getName( NAME, "GroupSequenceProvider" );
					public final static String DEFAULT_GROUP_SEQUENCE_PROVIDER = getName(
							NAME,
							"DefaultGroupSequenceProvider"
					);
				}
			}
		}

		public static class Joda {

			private static final String NAME = getName( Org.NAME, "joda" );

			public static class Time {

				private static final String NAME = getName( Joda.NAME, "time" );

				public final static String READABLE_PARTIAL = getName( NAME, "ReadablePartial" );
				public final static String READABLE_INSTANT = getName( NAME, "ReadableInstant" );
			}
		}
	}

}
