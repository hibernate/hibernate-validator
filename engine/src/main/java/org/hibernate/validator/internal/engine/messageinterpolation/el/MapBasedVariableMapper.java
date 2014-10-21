/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.el;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

/**
 * @author Hardy Ferentschik
 */
public class MapBasedVariableMapper extends VariableMapper {
	private Map<String, ValueExpression> map = Collections.emptyMap();

	@Override
	public ValueExpression resolveVariable(String variable) {
		return map.get( variable );
	}

	@Override
	public ValueExpression setVariable(String variable, ValueExpression expression) {
		if ( map.isEmpty() ) {
			map = new HashMap<String, ValueExpression>();
		}
		return map.put( variable, expression );
	}
}


