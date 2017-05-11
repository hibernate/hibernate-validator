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
	 * @deprecated Use {@link SafeHtmlDef#additionalTag(String)} instead.
	 */
	@Deprecated
	public SafeHtmlDef additionalTagsWithAttributes(SafeHtml.Tag... additionalTagsWithAttributes) {
		addParameter( "additionalTagsWithAttributes", additionalTagsWithAttributes );
		return this;
	}

	public SafeHtmlDef baseURI(String baseURI) {
		addParameter( "baseURI", baseURI );
		return this;
	}

	public SafeHtmlTagDef additionalTag(String tag) {
		return new SafeHtmlTagDef( tag, this );
	}

	public static class SafeHtmlTagDef extends SafeHtmlDef {

		private final TagWithAttributes tag;

		public SafeHtmlTagDef(String tag, SafeHtmlDef safeHtmlDef) {
			super( safeHtmlDef );
			this.tag = new TagWithAttributes( tag );
			addAnnotationAsParameter( "additionalTagsWithAttributes", this.tag );
		}

		public SafeHtmlTagDef(TagWithAttributes tag, SafeHtmlDef safeHtmlDef) {
			super( safeHtmlDef );
			this.tag = tag;
		}

		public SafeHtmlTagWithAttributeDef attribute(String attribute) {
			tag.addAttributes( attribute );
			return new SafeHtmlTagWithAttributeDef( tag, attribute, this );
		}

		public SafeHtmlTagDef attributes(String... attributes) {
			tag.addAttributes( attributes );
			return this;
		}
	}

	public static class SafeHtmlTagWithAttributeDef extends SafeHtmlTagDef {

		private final Attribute attribute;

		public SafeHtmlTagWithAttributeDef(TagWithAttributes tag, String attribute, SafeHtmlDef safeHtmlDef) {
			super( tag, safeHtmlDef );
			this.attribute = new Attribute( attribute );
			tag.addAdditionalAttributesWithProtocols( this.attribute );
		}

		public SafeHtmlTagWithAttributeDef protocol(String protocol) {
			attribute.addProtocol( protocol );
			return this;
		}

		public SafeHtmlTagWithAttributeDef protocols(String... protocols) {
			attribute.addProtocols( protocols );
			return this;
		}
	}

	private static class TagWithAttributes extends AnnotationDef<TagWithAttributes, SafeHtml.Tag> {

		public TagWithAttributes(String name) {
			super( SafeHtml.Tag.class );
			addParameter( "name", name );
		}

		public TagWithAttributes addAttributes(String... attributes) {
			addParameter( "attributes", attributes );
			return this;
		}

		public TagWithAttributes addAdditionalAttributesWithProtocols(Attribute attribute) {
			addAnnotationAsParameter( "additionalAttributesWithProtocols", attribute );
			return this;
		}
	}

	private static class Attribute extends AnnotationDef<Attribute, SafeHtml.Tag.Attribute> {

		public Attribute(String name, String... protocols) {
			super( SafeHtml.Tag.Attribute.class );
			addParameter( "name", name );
			addParameter( "protocols", protocols );
		}

		public Attribute addProtocol(String protocol) {
			addParameter( "protocols", protocol );
			return this;
		}

		public Attribute addProtocols(String... protocols) {
			addParameter( "protocols", protocols );
			return this;
		}
	}
}
