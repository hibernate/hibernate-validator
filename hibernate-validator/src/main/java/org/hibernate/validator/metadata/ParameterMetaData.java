/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.metadata;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;

/**
 * Contains constraint-related meta-data for one method parameter.
 *
 * @author Gunnar Morling
 */
public class ParameterMetaData implements Iterable<MethodMetaConstraint<?>> {

	private final Class<?> type;

	private final int index;

	private final String name;

	private final List<MethodMetaConstraint<?>> constraints;

	private final boolean isCascading;

	public ParameterMetaData(int index, Class<?> type, String name, List<MethodMetaConstraint<?>> constraints, boolean isCascading) {

		this.index = index;
		this.type = type;
		this.name = name;
		this.constraints = Collections.unmodifiableList( constraints );
		this.isCascading = isCascading;
	}

	public Class<?> getType() {
		return type;
	}

	public int getIndex() {
		return index;
	}

	public String getParameterName() {
		return name;
	}

	public boolean isCascading() {
		return isCascading;
	}

	/**
	 * Whether this parameter is constrained or not. This is the case, if this
	 * parameter has at least one constraint or a cascaded validation shall be
	 * performed for it.
	 *
	 * @return <code>True</code>, if this parameter is constrained,
	 *         <code>false</code> otherwise.
	 */
	public boolean isConstrained() {

		return isCascading || !constraints.isEmpty();
	}

	public Iterator<MethodMetaConstraint<?>> iterator() {
		return constraints.iterator();
	}

	public ParameterMetaData merge(ParameterMetaData otherMetaData) {

		return new ParameterMetaData(
				index,
				type,
				name,
				newArrayList( this, otherMetaData ),
				isCascading() || otherMetaData.isCascading()
		);
	}

	@Override
	public String toString() {

		//display short annotation type names
		StringBuilder sb = new StringBuilder();

		for ( MetaConstraint<?> oneConstraint : constraints ) {
			sb.append( oneConstraint.getDescriptor().getAnnotation().annotationType().getSimpleName() );
			sb.append( ", " );
		}

		String constraintsAsString = sb.length() > 0 ? sb.substring( 0, sb.length() - 2 ) : sb.toString();

		return "ParameterMetaData [type=" + type + "], [index=" + index + "], name=" + name + "], constraints=["
				+ constraintsAsString + "], isCascading=" + isCascading + "]";
	}
}
