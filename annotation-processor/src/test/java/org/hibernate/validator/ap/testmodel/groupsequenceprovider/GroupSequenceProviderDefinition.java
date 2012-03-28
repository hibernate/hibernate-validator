/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.ap.testmodel.groupsequenceprovider;

import javax.validation.GroupSequence;

import org.hibernate.validator.group.GroupSequenceProvider;

/**
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
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
