/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.AnnotationDef;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.SafeHtml;

/**
 * @author Marko Bekta
 */
public class SafeHtmlDef extends ConstraintDef<SafeHtmlDef, SafeHtml> {

	public SafeHtmlDef() {
		super( SafeHtml.class );
	}

	private SafeHtmlDef(ConstraintDef<?, SafeHtml> original) {
		super( original );
	}

	public SafeHtmlDef whitelistType(SafeHtml.WhiteListType whitelistType) {
		addParameter( "whitelistType", whitelistType );
		return this;
	}

	public SafeHtmlDef additionalTags(String... additionalTags) {
		addParameter( "additionalTags", additionalTags );
		return this;
	}

	/**
	 * @deprecated Use {@link SafeHtmlDef#additionalTags(TagDef, TagDef...)} instead.
	 */
	@Deprecated
	public SafeHtmlDef additionalTagsWithAttributes(SafeHtml.Tag... additionalTagsWithAttributes) {
		addParameter( "additionalTagsWithAttributes", additionalTagsWithAttributes );
		return this;
	}

	/**
	 * Whitelists additional tags.
	 *
	 * @since 6.0
	 */
	public SafeHtmlDef additionalTags(TagDef tag, TagDef... furtherTags) {
		addAnnotationAsParameter( "additionalTagsWithAttributes", tag );

		if ( furtherTags != null && furtherTags.length > 0 ) {
			for ( TagDef tagDef : furtherTags ) {
				addAnnotationAsParameter( "additionalTagsWithAttributes", tagDef );
			}
		}
		return this;
	}

	/**
	 * @since 6.0
	 */
	public SafeHtmlDef baseURI(String baseURI) {
		addParameter( "baseURI", baseURI );
		return this;
	}

	/**
	 * Represents a {@link SafeHtml.Tag}.
	 *
	 * @since 6.0
	 */
	public static class TagDef extends AnnotationDef<TagDef, SafeHtml.Tag> {

		public TagDef(String name) {
			super( SafeHtml.Tag.class );
			addParameter( "name", name );
		}

		public TagDef attributes(String attribute, String... furtherAttributes) {
			String[] attributes;

			if ( furtherAttributes != null && furtherAttributes.length > 0 ) {
				attributes = new String[furtherAttributes.length + 1];
				System.arraycopy( furtherAttributes, 0, attributes, 1, furtherAttributes.length );
				attributes[0] = attribute;
			}
			else {
				attributes = new String[] { attribute };
			}

			addParameter( "attributes", attributes );
			return this;
		}

		public TagDef attributes(AttributeDef attribute, AttributeDef... furtherAttributes) {
			addAnnotationAsParameter( "attributesWithProtocols", attribute );

			if ( furtherAttributes != null && furtherAttributes.length > 0 ) {
				for ( AttributeDef attributeDef : furtherAttributes ) {
					addAnnotationAsParameter( "attributesWithProtocols", attributeDef );
				}
			}

			return this;
		}
	}

	/**
	 * Represents a {@link SafeHtml.Attribute}.
	 *
	 * @since 6.0
	 */
	public static class AttributeDef extends AnnotationDef<AttributeDef, SafeHtml.Attribute> {

		public AttributeDef(String name, String protocol, String... furtherProtocols) {
			super( SafeHtml.Attribute.class );
			addParameter( "name", name );
			addProtocols( protocol, furtherProtocols );
		}

		private void addProtocols(String protocol, String... furtherProtocols) {
			String[] protocols;

			if ( furtherProtocols != null ) {
				protocols = new String[furtherProtocols.length + 1];
				System.arraycopy( furtherProtocols, 0, protocols, 1, furtherProtocols.length );
				protocols[0] = protocol;
			}
			else {
				protocols = new String[] { protocol };
			}

			addParameter( "protocols", protocols );
		}
	}
}
