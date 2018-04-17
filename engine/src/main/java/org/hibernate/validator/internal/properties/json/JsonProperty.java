/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.json;

import java.lang.reflect.Type;

import javax.json.JsonObject;

import org.hibernate.validator.internal.properties.Property;

/**
 * @author Marko Bekhta
 */
public class JsonProperty implements Property {

	private final String name;
	private final Class<?> type;

	public JsonProperty(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	@Override public Object getValueFrom(Object bean) {
		JsonObject jsonObject = (JsonObject) bean;

		if ( Number.class.isAssignableFrom( type ) ) {
			return jsonObject.getJsonNumber( name ).numberValue();
		}
		else if ( Boolean.class.isAssignableFrom( type ) ) {
			return jsonObject.getBoolean( name );
		}
		else if ( String.class.isAssignableFrom( type ) ) {
			return jsonObject.getString( name );
		}
		return jsonObject.getJsonObject( name );
	}

	@Override public String getPropertyName() {
		return getName();
	}

	@Override public String getName() {
		return name;
	}

	@Override public Class<?> getDeclaringClass() {
		// Not sure about this one. What would we really want to use here...
		return JsonObject.class;
	}

	@Override public Type getTypeForValidatorResolution() {
		return getType();
	}

	@Override public Type getType() {
		return type;
	}

	@Override public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		JsonProperty that = (JsonProperty) o;

		if ( !name.equals( that.name ) ) {
			return false;
		}
		return type.equals( that.type );
	}

	@Override public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}
}
