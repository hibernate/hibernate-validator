/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import org.hibernate.validator.constraints.SafeHtml;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Iterator;

/**
 * Validate that the string does not contain malicious code.
 *
 * It uses <a href="http://www.jsoup.org">JSoup</a> as the underlying parser/sanitizer library.
 *
 * @author George Gastaldi
 * @author Hardy Ferentschik
 */
public class SafeHtmlValidator implements ConstraintValidator<SafeHtml, CharSequence> {
	private Whitelist whitelist;

	@Override
	public void initialize(SafeHtml safeHtmlAnnotation) {
		switch ( safeHtmlAnnotation.whitelistType() ) {
			case BASIC:
				whitelist = Whitelist.basic();
				break;
			case BASIC_WITH_IMAGES:
				whitelist = Whitelist.basicWithImages();
				break;
			case NONE:
				whitelist = Whitelist.none();
				break;
			case RELAXED:
				whitelist = Whitelist.relaxed();
				break;
			case SIMPLE_TEXT:
				whitelist = Whitelist.simpleText();
				break;
		}
		whitelist.addTags( safeHtmlAnnotation.additionalTags() );

		for ( SafeHtml.Tag tag : safeHtmlAnnotation.additionalTagsWithAttributes() ) {
			whitelist.addAttributes( tag.name(), tag.attributes() );
		}
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		return new Cleaner( whitelist ).isValid( getFragmentAsDocument( value ) );
	}

	/**
	 * Returns a document whose {@code <body>} element contains the given HTML fragment.
	 */
	private Document getFragmentAsDocument(CharSequence value) {
		// using the XML parser ensures that all elements in the input are retained, also if they actually are not allowed at the given
		// location; E.g. a <td> element isn't allowed directly within the <body> element, so it would be used by the default HTML parser.
		// we need to retain it though to apply the given white list properly; See HV-873
		Document fragment = Jsoup.parse( value.toString(), "", Parser.xmlParser() );
		Document document = Document.createShell( "" );

		// add the fragment's nodes to the body of resulting document
		Iterator<Element> nodes = fragment.children().iterator();
		while ( nodes.hasNext() ) {
			document.body().appendChild( nodes.next() );
		}

		return document;
	}
}
