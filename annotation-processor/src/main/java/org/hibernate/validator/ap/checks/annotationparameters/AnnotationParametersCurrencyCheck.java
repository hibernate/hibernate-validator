/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.checks.annotationparameters;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;

import org.hibernate.validator.ap.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.TypeNames;

/**
 * Checks that the parameters used on {@code org.hibernate.validator.constraints.Currency} annotations are valid.
 *
 * @author Marko Bekhta
 */
public class AnnotationParametersCurrencyCheck extends AnnotationParametersAbstractCheck {

	private static final Set<String> CURRENCY_ISO_CODES;

	// codes taken from http://www.xe.com/iso4217.php
	static {
		CURRENCY_ISO_CODES = CollectionHelper.asSet( "AED"
				, "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN"
				, "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB"
				, "BOV", "BRL", "BSD", "BTN", "BWP", "BYN", "BZD", "CAD", "CDF"
				, "CHE", "CHF", "CHW", "CLF", "CLP", "CNY", "COP", "COU", "CRC"
				, "CUC", "CUP", "CVE", "CZK", "DJF", "DKK", "DOP", "DZD", "EGP"
				, "ERN", "ETB", "EUR", "FJD", "FKP", "GBP", "GEL", "GHS", "GIP"
				, "GMD", "GNF", "GTQ", "GYD", "HKD", "HNL", "HRK", "HTG", "HUF"
				, "IDR", "ILS", "INR", "IQD", "IRR", "ISK", "JMD", "JOD", "JPY"
				, "KES", "KGS", "KHR", "KMF", "KPW", "KRW", "KWD", "KYD", "KZT"
				, "LAK", "LBP", "LKR", "LRD", "LSL", "LYD", "MAD", "MDL", "MGA"
				, "MKD", "MMK", "MNT", "MOP", "MRO", "MUR", "MVR", "MWK", "MXN"
				, "MXV", "MYR", "MZN", "NAD", "NGN", "NIO", "NOK", "NPR", "NZD"
				, "OMR", "PAB", "PEN", "PGK", "PHP", "PKR", "PLN", "PYG", "QAR"
				, "RON", "RSD", "RUB", "RWF", "SAR", "SBD", "SCR", "SDG", "SEK"
				, "SGD", "SHP", "SLL", "SOS", "SRD", "SSP", "STD", "SVC", "SYP"
				, "SZL", "THB", "TJS", "TMT", "TND", "TOP", "TRY", "TTD", "TWD"
				, "TZS", "UAH", "UGX", "USD", "USN", "UYI", "UYU", "UZS", "VEF"
				, "VND", "VUV", "WST", "XAF", "XAG", "XAU", "XBA", "XBB", "XBC"
				, "XBD", "XCD", "XDR", "XOF", "XPD", "XPF", "XPT", "XSU", "XTS"
				, "XUA", "XXX", "YER", "ZAR", "ZMW", "ZWL" );
	}

	public AnnotationParametersCurrencyCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, TypeNames.HibernateValidatorTypes.CURRENCY );
	}

	@Override
	protected Set<ConstraintCheckIssue> doCheck(Element element, AnnotationMirror annotation) {
		List<? extends AnnotationValue> values = annotationApiHelper.getAnnotationArrayValue( annotation, "value" );

		for ( AnnotationValue value : values ) {
			if ( !CURRENCY_ISO_CODES.contains( ( (String) value.getValue() ).toUpperCase() ) ) {
				return CollectionHelper.asSet(
						ConstraintCheckIssue.warning(
								element, annotation, "INVALID_CURRENCY_VALUE_ANNOTATION_PARAMETERS", value.getValue()
						)
				);
			}
		}

		return Collections.emptySet();
	}
}
