/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.IpAddress;
import org.hibernate.validator.internal.util.Contracts;

/**
 * Checks that a given character sequence (e.g. string) is a valid IP address.
 *
 * @author Ivan Malutin
 */
public class IpAddressValidator implements ConstraintValidator<IpAddress, CharSequence> {

	private IpAddressValidationAlgorithm ipAddressValidationAlgorithm;

	@Override
	public void initialize(IpAddress constraintAnnotation) {
		this.ipAddressValidationAlgorithm = IpAddressValidationAlgorithm.from( constraintAnnotation.type() );
	}

	@Override
	public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
		if ( charSequence == null ) {
			return true;
		}

		return ipAddressValidationAlgorithm.isValid( charSequence );
	}

	private enum IpAddressValidationAlgorithm {
		IPv4 {
			@Override
			public boolean isValid(CharSequence ipAddress) {
				return isValidIpV4( ipAddress, 0, ipAddress.length() );
			}
		},
		IPv6 {
			private static final int IPv4_SEGMENTS = 2;
			private static final int IPv6_SEGMENTS_MAX_TOTAL = 8;

			@Override
			public boolean isValid(CharSequence charSequence) {
				if ( charSequence.length() < 2 ) {
					// we at least need to have a ::
					return false;
				}

				// implementation is highly inspired by sun.net.util.IPAddressUtil
				String ipAddress = charSequence.toString();
				// We ignore the scoped literal, so we look for % which defines it (scoped literal) if it's there:
				int length = ipAddress.indexOf( '%' );
				if ( length < 0 ) {
					length = ipAddress.length();
				}

				int i = 0;
				int numberOfConsumedSegments = 0;
				// Leading :: requires some special handling.
				if ( ipAddress.charAt( i ) == ':' ) {
					if ( ipAddress.charAt( ++i ) != ':' ) {
						return false;
					}
				}
				char currentCharacter;
				boolean hasSuppression = false;
				boolean previousConsumedTokenIsDigit = false;
				int segmentLength = 0;
				int val = 0;
				int curtok = i;

				while ( i < length ) {
					currentCharacter = ipAddress.charAt( i++ );

					if ( currentCharacter == ':' ) {
						curtok = i;
						if ( !previousConsumedTokenIsDigit ) {
							if ( hasSuppression ) {
								return false;
							}
							hasSuppression = true;
							continue;
						}
						else if ( i == length ) {
							return false;
						}
						numberOfConsumedSegments++;

						if ( numberOfConsumedSegments > IPv6_SEGMENTS_MAX_TOTAL ) {
							return false;
						}

						previousConsumedTokenIsDigit = false;
						val = 0;
						segmentLength = 0;
						continue;
					}
					// if we have a dot we assume it's an ipv4 segment.
					// if that's so it can only be the last 32 bits of the IPv6
					// so we check we are looking at the last 2 segments (note we may have had some suppression
					// and if that's so the number of consumed segments so far may be less than 6
					if ( currentCharacter == '.' ) {
						if ( ( ( numberOfConsumedSegments + IPv4_SEGMENTS ) <= IPv6_SEGMENTS_MAX_TOTAL ) ) {
							if ( !isValidIpV4( ipAddress, curtok, length ) ) {
								return false;
							}
						}
						else {
							return false;
						}
						previousConsumedTokenIsDigit = false;
						break;
					}
					int chval;

					char lowerCh = Character.toLowerCase( currentCharacter );
					if ( lowerCh >= 'a' && lowerCh <= 'f' ) {
						chval = lowerCh - 'a' + 10;
					}
					else {
						chval = currentCharacter - '0';

					}

					if ( chval > -1 && chval < 17 ) {
						val <<= 4;
						val |= chval;
						if ( val > 0xffff ) {
							return false;
						}
						previousConsumedTokenIsDigit = true;
						segmentLength++;
						if ( segmentLength == 5 ) {
							return false;
						}
						continue;
					}
					return false;
				}
				if ( previousConsumedTokenIsDigit ) {
					if ( numberOfConsumedSegments + 1 > IPv6_SEGMENTS_MAX_TOTAL ) {
						return false;
					}
					numberOfConsumedSegments++;
				}

				if ( hasSuppression ) {
					return numberOfConsumedSegments < IPv6_SEGMENTS_MAX_TOTAL;
				}
				return numberOfConsumedSegments == IPv6_SEGMENTS_MAX_TOTAL;
			}
		},
		ANY {
			@Override
			public boolean isValid(CharSequence charSequence) {
				return IPv4.isValid( charSequence ) || IPv6.isValid( charSequence );
			}
		};

		abstract boolean isValid(CharSequence charSequence);

		static IpAddressValidationAlgorithm from(IpAddress.Type type) {
			Contracts.assertNotNull( type );

			return switch ( type ) {
				case IPv4 -> IpAddressValidationAlgorithm.IPv4;
				case IPv6 -> IpAddressValidationAlgorithm.IPv6;
				case ANY -> IpAddressValidationAlgorithm.ANY;
			};
		}

		static boolean isValidIpV4(CharSequence string, int start, int end) {
			int length = end - start;
			if ( length < 7 || length > 15 ) {
				return false;
			}

			// implementation inspired by sun.net.util.IPAddressUtil
			int segmentValue = 0;
			int segments = 1;
			boolean newSegment = true;

			for ( int i = start; i < end; i++ ) {
				char c = string.charAt( i );
				if ( c == '.' ) {
					if ( newSegment || segmentValue < 0 || segmentValue > 255 || segments == 4 ) {
						return false;
					}
					segments++;
					segmentValue = 0;
					newSegment = true;
				}
				else {
					int digit = c - '0';
					if ( digit < 0 || digit > 9 ) {
						return false;
					}
					segmentValue *= 10;
					segmentValue += digit;
					// to prevet any leading 0 in the segment
					if ( !newSegment && segmentValue < 10 ) {
						return false;
					}
					newSegment = false;
				}
			}
			if ( newSegment || segmentValue < 0 || segmentValue > 255 || segments != 4 ) {
				return false;
			}

			return true;
		}
	}
}
