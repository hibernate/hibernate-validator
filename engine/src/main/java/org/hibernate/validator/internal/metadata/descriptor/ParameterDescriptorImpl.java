/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.metadata.descriptor;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.metadata.GroupConversionDescriptor;
import javax.validation.metadata.ParameterDescriptor;

/**
 * Describes a validated method parameter.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class ParameterDescriptorImpl extends ElementDescriptorImpl implements ParameterDescriptor {
	private final int index;
	private final String name;
	private final boolean cascaded;
	private final Set<GroupConversionDescriptor> groupConversions;

	public ParameterDescriptorImpl(Type type,
								   int index,
								   String name,
								   Set<ConstraintDescriptorImpl<?>> constraints,
								   boolean isCascaded,
								   boolean defaultGroupSequenceRedefined,
								   List<Class<?>> defaultGroupSequence,
								   Set<GroupConversionDescriptor> groupConversions) {
		super( type, constraints, defaultGroupSequenceRedefined, defaultGroupSequence );
		this.index = index;
		this.name = name;
		this.cascaded = isCascaded;
		this.groupConversions = Collections.unmodifiableSet( groupConversions );
	}

	@Override
	public boolean isCascaded() {
		return cascaded;
	}

	@Override
	public Set<GroupConversionDescriptor> getGroupConversions() {
		return groupConversions;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ParameterDescriptorImpl" );
		sb.append( "{cascaded=" ).append( cascaded );
		sb.append( ", index=" ).append( index );
		sb.append( ", name=" ).append( name );
		sb.append( '}' );
		return sb.toString();
	}
}