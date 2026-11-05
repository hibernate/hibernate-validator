package org.hibernate.validator.referenceguide.chapter09;

//tag::include[]
/**
 * Application-provided service for checking passwords against a dictionary
 * of common words. The implementation decides what constitutes a match
 * (exact, substring, etc.) and where the dictionary is (file, database, etc.).
 */
public interface DictionaryService {

	boolean containsDictionaryWord(String password);
}
//end::include[]
