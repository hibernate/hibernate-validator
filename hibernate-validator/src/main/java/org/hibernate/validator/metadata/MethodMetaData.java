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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a method of a Java type and all its associated meta-data relevant
 * in the context of bean validation, for instance the constraints at it's
 * parameters or return value.
 *
 * @author Gunnar Morling
 */
public class MethodMetaData implements Iterable<BeanMetaConstraint<?, ? extends Annotation>> {

	private final Method method;

	private final Map<Integer, ParameterMetaData> parameterMetaData;

	private final List<BeanMetaConstraint<?, ? extends Annotation>> constraints;

	private final boolean isCascading;

	public MethodMetaData(
			Method method,
			Map<Integer, ParameterMetaData> parameterMetaData,
			List<BeanMetaConstraint<?, ? extends Annotation>> constraints,
			boolean isCascading) {

		this.method = method;
		this.parameterMetaData = parameterMetaData;
		this.constraints = constraints;
		this.isCascading = isCascading;
	}

	public Method getMethod() {
		return method;
	}

	public ParameterMetaData getParameterMetaData(int parameterIndex) {

		ParameterMetaData theValue = parameterMetaData.get( parameterIndex );

		return theValue != null ? theValue : ParameterMetaData.NULL;
	}

	public Iterator<BeanMetaConstraint<?, ? extends Annotation>> iterator() {
		return constraints.iterator();
	}

	public boolean isCascading() {
		return isCascading;
	}

	@Override
	public String toString() {
		return "MethodMetaData [method=" + method + ", parameterMetaData="
				+ parameterMetaData + ", constraints=" + constraints
				+ ", isCascading=" + isCascading + "]";
	}

}
