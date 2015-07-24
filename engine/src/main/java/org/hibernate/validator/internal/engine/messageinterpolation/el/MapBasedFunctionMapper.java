/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.el;

import javax.el.FunctionMapper;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hardy Ferentschik
 */
public class MapBasedFunctionMapper extends FunctionMapper {
	private static final String FUNCTION_NAME_SEPARATOR = ":";
	private Map<String, Method> map = Collections.emptyMap();

	@Override
	public Method resolveFunction(String prefix, String localName) {
		return map.get( prefix + FUNCTION_NAME_SEPARATOR + localName );
	}

	public void setFunction(String prefix, String localName, Method method) {
		if ( map.isEmpty() ) {
			map = new HashMap<String, Method>();
		}
		map.put( prefix + FUNCTION_NAME_SEPARATOR + localName, method );
	}
}

