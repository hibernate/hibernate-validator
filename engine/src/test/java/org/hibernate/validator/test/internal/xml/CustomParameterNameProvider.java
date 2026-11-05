package org.hibernate.validator.test.internal.xml;

import org.hibernate.validator.testutil.PrefixableParameterNameProvider;

/**
 * @author Gunnar Morling
 */
public class CustomParameterNameProvider extends PrefixableParameterNameProvider {

	public CustomParameterNameProvider() {
		super( "param" );
	}
}
