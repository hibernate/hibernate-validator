package org.hibernate.validator.referenceguide.chapter02.typeargument;

import javax.validation.Valid;

public class FooHolder {
	@Valid
	Foo<@CustomNotBlank String> foo = new Foo<String>( "" );
}
