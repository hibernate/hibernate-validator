/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.parameternameprovider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import javax.validation.ParameterNameProvider;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;

/**
 * A {@link ParameterNameProvider} implementation backed by the <a href="http://paranamer.codehaus.org/">ParaNamer</a>
 * library.
 * <p>
 * The {@link Paranamer} implementation to use can be passed when creating a {@code ParanamerParameterNameProvider}. By
 * default a {@link AdaptiveParanamer} will be used which is wrapped into a {@link CachingParanamer}. If no parameter
 * names can be obtained from the configured {@code Paranamer}, the default parameter name provider will be used as
 * fall back.
 * <p>
 * The ParaNamer library must be present on the classpath when using this parameter name provider.
 *
 * @author Gunnar Morling
 * @see <a href="http://paranamer.codehaus.org/">ParaNamer web site</a>
 */
public class ParanamerParameterNameProvider implements ParameterNameProvider {

	private final ParameterNameProvider fallBackProvider;
	private final Paranamer paranamer;

	public ParanamerParameterNameProvider() {
		this( null );
	}

	public ParanamerParameterNameProvider(Paranamer paranamer) {
		this.paranamer = paranamer != null ? paranamer : new CachingParanamer( new AdaptiveParanamer() );
		fallBackProvider = new DefaultParameterNameProvider();
	}

	@Override
	public List<String> getParameterNames(Constructor<?> constructor) {
		String[] parameterNames;

		//there are no guarantees regarding thread-safety
		synchronized ( paranamer ) {
			parameterNames = paranamer.lookupParameterNames( constructor, false );
		}

		//either null or an empty array is returned if no names could be retrieved
		if ( parameterNames != null && parameterNames.length == constructor.getParameterTypes().length ) {
			return Arrays.asList( parameterNames );
		}

		return fallBackProvider.getParameterNames( constructor );
	}

	@Override
	public List<String> getParameterNames(Method method) {
		String[] parameterNames;

		synchronized ( paranamer ) {
			parameterNames = paranamer.lookupParameterNames( method, false );
		}

		if ( parameterNames != null && parameterNames.length == method.getParameterTypes().length ) {
			return Arrays.asList( parameterNames );
		}

		return fallBackProvider.getParameterNames( method );
	}
}
