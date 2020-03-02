/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.groupsequenceprovider;

import jakarta.validation.GroupSequence;

import org.hibernate.validator.group.GroupSequenceProvider;

/**
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class GroupSequenceProviderDefinition {

	@GroupSequenceProvider(FooDefaultGroupSequenceProvider.class)
	public static class Foo {
	}

	/**
	 * Not allowed
	 */
	@GroupSequenceProvider(SampleDefaultGroupSequenceProvider.class)
	@GroupSequence( { Sample.class })
	public static class Sample {
	}

	/**
	 * Not allowed. Default group sequence provider class defines
	 * a wrong generic type.
	 */
	@GroupSequenceProvider(FooDefaultGroupSequenceProvider.class)
	public static class Bar {
	}

	/**
	 * Not allowed. Default group sequence provider class is an
	 * interface.
	 */
	@GroupSequenceProvider(BazDefaultGroupSequenceProvider.class)
	public static class Baz {
	}

	/**
	 * Not allowed. GroupSequenceProvider annotation is added on
	 * on an interface.
	 */
	@GroupSequenceProvider(QuxDefaultGroupSequenceProvider.class)
	public interface Qux {
	}

	/**
	 * Not allowed. Default group sequence provider class is
	 * abstract.
	 */
	@GroupSequenceProvider(FooBarDefaultGroupSequenceProvider.class)
	public static class FooBar {
	}

	/**
	 * Not allowed. Default group sequence provider class has
	 * no public default constructor.
	 */
	@GroupSequenceProvider(FooBarBazDefaultGroupSequenceProvider.class)
	public static class FooBarBaz {
	}
}
