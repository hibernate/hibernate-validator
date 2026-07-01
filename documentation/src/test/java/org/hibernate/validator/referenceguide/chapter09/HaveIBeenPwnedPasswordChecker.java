/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.hibernate.validator.spi.password.CompromisedPasswordChecker;
import org.hibernate.validator.spi.password.CompromisedPasswordResult;

//tag::include[]
public class HaveIBeenPwnedPasswordChecker implements CompromisedPasswordChecker {

	private static final String API_URL = "https://api.pwnedpasswords.com/range/";

	private final HttpClient httpClient;

	public HaveIBeenPwnedPasswordChecker() {
		this.httpClient = HttpClient.newHttpClient();
	}

	@Override
	public CompromisedPasswordResult check(char[] password) {
		String sha1 = sha1Hex( password );
		String prefix = sha1.substring( 0, 5 );
		String suffix = sha1.substring( 5 );

		String responseBody = queryApi( prefix );
		int occurrences = findOccurrences( responseBody, suffix );

		return CompromisedPasswordResult.simple( occurrences );
	}

	private String queryApi(String prefix) {
		HttpRequest request = HttpRequest.newBuilder()
				.uri( URI.create( API_URL + prefix ) )
				.header( "Add-Padding", "true" )
				.header( "User-Agent", "Hibernate-Validator" )
				.GET()
				.build();
		try {
			HttpResponse<String> response = httpClient.send(
					request, HttpResponse.BodyHandlers.ofString()
			);
			return response.body();
		}
		catch (Exception e) {
			throw new RuntimeException( "Failed to query Have I Been Pwned API", e );
		}
	}

	//end::include[]

	private static int findOccurrences(String responseBody, String suffix) {
		for ( String line : responseBody.split( "\r\n" ) ) {
			String[] parts = line.split( ":" );
			if ( parts[0].equalsIgnoreCase( suffix ) ) {
				int count = Integer.parseInt( parts[1] );
				// Padded entries always have a count of 0 and should be ignored
				return count > 0 ? count : 0;
			}
		}
		return 0;
	}

	private static String sha1Hex(char[] password) {
		try {
			MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
			byte[] bytes = new String( password ).getBytes( java.nio.charset.StandardCharsets.UTF_8 );
			byte[] hash = digest.digest( bytes );
			return HexFormat.of().withUpperCase().formatHex( hash );
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException( "SHA-1 not available", e );
		}
	}
}
