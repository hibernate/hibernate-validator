/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import javax.validation.metadata.PropertyDescriptor;

/**
 * Describes a validated property.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class PropertyDescriptorImpl extends ElementDescriptorImpl implements PropertyDescriptor {
	private final boolean cascaded;
	private final String property;
	private final Set<GroupConversionDescriptor> groupConversions;

	public PropertyDescriptorImpl(Type returnType,
								  String propertyName,
								  Set<ConstraintDescriptorImpl<?>> constraints,
								  boolean cascaded,
								  boolean defaultGroupSequenceRedefined,
								  List<Class<?>> defaultGroupSequence, Set<GroupConversionDescriptor> groupConversions) {
		super( returnType, constraints, defaultGroupSequenceRedefined, defaultGroupSequence );

		this.property = propertyName;
		this.cascaded = cascaded;
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
	public String getPropertyName() {
		return property;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "PropertyDescriptorImpl" );
		sb.append( "{property=" ).append( property );
		sb.append( ", cascaded='" ).append( cascaded ).append( '\'' );
		sb.append( '}' );
		return sb.toString();
	}
}
