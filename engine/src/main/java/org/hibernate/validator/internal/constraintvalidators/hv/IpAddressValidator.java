/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;


import java.util.Arrays;
import java.util.List;

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

	private interface IpAddressValidationAlgorithm {
		boolean isValid(CharSequence charSequence);

		static IpAddressValidationAlgorithm from(IpAddress.Type type) {
			Contracts.assertNotNull( type );

			return switch ( type ) {
				case IPv4 -> IpAddressValidationAlgorithmImpl.IPv4;
				case IPv6 -> IpAddressValidationAlgorithmImpl.IPv6;
				case ANY -> IpAddressValidationAlgorithmImpl.ANY;
			};
		}
	}

	private enum IpAddressValidationAlgorithmImpl implements IpAddressValidationAlgorithm {
		IPv4 {
			@Override
			public boolean isValid(CharSequence charSequence) {
				String ipAddress = charSequence.toString();

				String[] parts = ipAddress.split( "\\." );

				if ( parts.length != 4 ) {
					return false;
				}

				for ( String part : parts ) {
					if ( part.isBlank() || ( part.length() > 1 && part.startsWith( "0" ) ) ) {
						return false;
					}

					int num;
					try {
						num = Integer.parseInt( part );
					}
					catch (NumberFormatException e) {
						return false;
					}
					if ( num < 0 || 255 < num ) {
						return false;
					}

				}

				return true;
			}
		},
		IPv6 {
			@Override
			public boolean isValid(CharSequence charSequence) {
				String ipAddress = charSequence.toString();

				int compressionIndex = ipAddress.indexOf( "::" );
				if ( compressionIndex != -1 && ipAddress.lastIndexOf( "::" ) != compressionIndex ) {
					return false;
				}

				boolean startsWithCompression = ipAddress.startsWith( "::" );
				boolean endsWithCompression = ipAddress.endsWith( "::" );
				if ( ( ipAddress.startsWith( ":" ) && !startsWithCompression ) || ( ipAddress.endsWith( ":" ) && !endsWithCompression ) ) {
					return false;
				}

				String[] parts = ipAddress.split( ":" );
				boolean hasCompression = compressionIndex != -1;

				if ( hasCompression ) {
					List<String> partsList = Arrays.asList( parts );
					if ( endsWithCompression ) {
						partsList.add( "" );
					}
					else if ( startsWithCompression && !partsList.isEmpty() ) {
						partsList.remove( 0 );
					}
					parts = partsList.toArray( new String[0] );
				}

				if ( parts.length > 8 ) {
					return false;
				}

				int partsCount = 0;
				for ( int i = 0; i < parts.length; i++ ) {
					String part = parts[i];

					if ( part.isBlank() ) {
						if ( i > 0 && parts[i - 1].isBlank() ) {
							return false;
						}
					}
					else if ( i == parts.length - 1 && part.contains( "." ) ) {
						if ( !IPv4.isValid( part ) ) {
							return false;
						}
						partsCount += 2;
					}
					else {
						if ( part.length() > 4 ) {
							return false;
						}
						int num;
						try {
							num = Integer.parseInt( part, 16 );
						}
						catch (NumberFormatException e) {
							return false;
						}
						if ( num < 0 || num > 0xFFFF ) {
							return false;
						}
						partsCount++;
					}
				}

				if ( partsCount > 8 || ( partsCount < 8 && !hasCompression ) ) {
					return false;
				}

				return true;
			}
		},
		ANY {
			@Override
			public boolean isValid(CharSequence charSequence) {
				return IPv4.isValid( charSequence ) || IPv6.isValid( charSequence );
			}
		}
	}
}
