/**
 * 
 */
package de.l3s.translate;

import java.util.List;


/**
 * Translation interface
 * 
 * @author Elena Demidova
 * @date April 2012
 *
 */
public interface TranslationI {
	/*
	public static String ENGLISH="en";
	public static String GERMAN="de";
	public static String SPANISH="es";
	public static String RUSSIAN="ru";
	public static String REFERENCE_LANGUAGE="en";
	*/
	
	/**
	 * Translate a label into the given language
	 * 
	 * @param label - a label to translate
	 * @param targetLang - target language
	 * @return translated label
	 */
	public String translate(String label, Language sourceLang, Language targetLang) throws Exception;
	
	/**
	 * Translate a list of labels into the given language
	 * 
	 * @param labels - labels to translate
	 * @param targetLang - target language
	 * @return translated labels
	 */
	public List<String> translateTexts(List<String> labels, Language sourceLang, Language targetLang) throws Exception;
	
	/**
	 * Get statistics of translation requests
	 * 
	 * @return number of requests
	 */
	public int getRequestCount();

	public String translate(String string, String fromlang, String tolang);
	

}
