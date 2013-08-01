/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
