/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;

/**
 * @author Marko Bekta
 */
public class SafeHtmlDef extends ConstraintDef<SafeHtmlDef, SafeHtml> {

	private final Set<TagWithAttributes> tags;

	public SafeHtmlDef() {
		super( SafeHtml.class );
		tags = CollectionHelper.newHashSet();
	}

	private SafeHtmlDef(ConstraintDef<?, SafeHtml> original) {
		super( original );
		tags = CollectionHelper.newHashSet();
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

	public class SafeHtmlTagDef extends SafeHtmlDef {
		private final TagWithAttributes tag;

		public SafeHtmlTagDef(String tag, SafeHtmlDef safeHtmlDef) {
			super( safeHtmlDef );
			this.tag = new TagWithAttributes( tag );
			SafeHtmlDef.this.tags.add( this.tag );
		}

		public SafeHtmlTagDef(TagWithAttributes tag, SafeHtmlDef safeHtmlDef) {
			super( safeHtmlDef );
			this.tag = tag;
		}

		public SafeHtmlTagWithAttributeDef attribute(String attribute) {
			tag.getSimpleAttributes().add( attribute );
			updateDef();
			return new SafeHtmlTagWithAttributeDef( tag, attribute, this );
		}

		public SafeHtmlTagDef attributes(String... attributes) {
			tag.getSimpleAttributes().addAll( Stream.of( attributes ).collect( Collectors.toSet() ) );
			updateDef();
			return this;
		}

		public class SafeHtmlTagWithAttributeDef extends SafeHtmlTagDef {

			private final Attribute attribute;

			public SafeHtmlTagWithAttributeDef(TagWithAttributes tag, String attribute, SafeHtmlDef safeHtmlDef) {
				super( tag, safeHtmlDef );
				this.attribute = new Attribute( attribute );
				tag.getAttributes().add( this.attribute );
			}

			public SafeHtmlTagWithAttributeDef protocol(String protocol) {
				attribute.getProtocols().add( protocol );
				updateDef();
				return this;
			}

			public SafeHtmlTagWithAttributeDef protocols(String... protocols) {
				attribute.getProtocols().addAll( Stream.of( protocols ).collect( Collectors.toSet() ) );
				updateDef();
				return this;
			}

		}
	}

	private void updateDef() {
		addParameter( "additionalTagsWithAttributes", SafeHtmlDef.this.tags.stream().map( TagWithAttributes::toTagAnnotationProxy )
				.toArray( n -> new SafeHtml.Tag[n] ) );
	}

	private static class TagWithAttributes {
		private final String name;
		private final Set<String> simpleAttributes;
		private final List<Attribute> attributes;

		public TagWithAttributes(String name) {
			this.name = name;
			simpleAttributes = CollectionHelper.newHashSet();
			attributes = CollectionHelper.newArrayList();
		}

		public String getName() {
			return name;
		}

		public Set<String> getSimpleAttributes() {
			return simpleAttributes;
		}

		public List<Attribute> getAttributes() {
			return attributes;
		}

		public SafeHtml.Tag toTagAnnotationProxy() {
			AnnotationDescriptor<SafeHtml.Tag> tagDescriptor = new AnnotationDescriptor( SafeHtml.Tag.class );
			tagDescriptor.setValue( "name", this.getName() );
			tagDescriptor.setValue( "attributes", this.getSimpleAttributes().stream().toArray( n -> new String[n] ) );
			tagDescriptor.setValue( "additionalAttributesWithProtocols", this.getAttributes()
					.stream().map( Attribute::toAttributeAnnotationProxy ).toArray( n -> new SafeHtml.Tag.Attribute[n] ) );
			return AnnotationFactory.create( tagDescriptor );
		}

	}

	private static class Attribute {
		private final String name;
		private final Set<String> protocols;

		public Attribute(String name, String... protocols) {
			this.name = name;
			this.protocols = Stream.of( protocols ).collect( Collectors.toSet() );
		}

		public String getName() {
			return name;
		}

		public Set<String> getProtocols() {
			return protocols;
		}

		public SafeHtml.Tag.Attribute toAttributeAnnotationProxy() {
			AnnotationDescriptor<SafeHtml.Tag.Attribute> attributeDescriptor = new AnnotationDescriptor( SafeHtml.Tag.Attribute.class );
			attributeDescriptor.setValue( "name", this.getName() );
			attributeDescriptor.setValue( "protocols", this.getProtocols()
					.toArray( new String[this.getProtocols().size()] ) );
			return AnnotationFactory.create( attributeDescriptor );
		}
	}
}
