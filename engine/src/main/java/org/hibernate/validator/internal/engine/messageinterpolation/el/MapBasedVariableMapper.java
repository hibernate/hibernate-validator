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


