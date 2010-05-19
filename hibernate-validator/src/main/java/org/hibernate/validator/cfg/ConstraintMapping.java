// $Id:$
/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintMapping {
	private final Map<Class<?>, List<ConstraintDefinition<?>>> configData;

	public ConstraintMapping() {
		configData = new HashMap<Class<?>, List<ConstraintDefinition<?>>>();
	}

	public ConstraintsForType type(Class<?> beanClass) {
		return new ConstraintsForType( beanClass, this );
	}

	protected void addConstraintConfig(ConstraintDefinition<?> definition) {
		Class<?> beanClass = definition.getBeanType();
		if(configData.containsKey( beanClass )) {
			configData.get( beanClass ).add( definition );
		} else {
			List<ConstraintDefinition<?>> definitionList = new ArrayList<ConstraintDefinition<?>>();
			definitionList.add( definition );
			configData.put(beanClass, definitionList);
		}
	}

	public Map<Class<?>, List<ConstraintDefinition<?>>> getConfigData() {
		return configData;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ConstraintMapping" );
		sb.append( "{configData=" ).append( configData );
		sb.append( '}' );
		return sb.toString();
	}
}


