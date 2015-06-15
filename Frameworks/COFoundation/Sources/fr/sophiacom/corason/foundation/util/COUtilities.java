package fr.sophiacom.corason.foundation.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXStringUtilities;

/**
 * Utility class which offers several static methods:
 *<ul>
 * <li> Merge 2 arrays
 * <li> Compares 2 arrays and returns an array of present objects and an array of missing objects
 * <li> supprimer, remplacer du code html
 * <li> filtrer la segmentation
 * <li> valider une adresse email
 * <li> ...
 *</ul>
 *
 * @author jcardon
 * @version 1.2, 22/02/2006
 *
 */
public class COUtilities
{
	/**
	 * This method compares the array <code>in_comparedArray</code> to the reference <code>in_arrayReferance</code>.
	 * It fills the array <code>out_objectAdded</code> with the objects found in <code>in_comparedArray</code> and not in <code>in_arrayReferance</code>,
	 * and fills the array <code>out_missingObject</code> not found in <code>in_comparedArray</code>
	 *
	 * @param in_referenceArray reference array
	 * @param in_ArrayToCompare array to compare
	 * @param out_objectAdded array of objects added. If a null value is passed, it's ignored.
	 * @param out_missingObject array of missing objects. If a null value is passed, it's ignored.
	 * @deprecated if we use this method, we should use sets instead of NSArray
	 *
	 */
	@Deprecated
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void compareArraysAndReturnDifferences(
			final NSArray in_referenceArray, final NSArray in_ArrayToCompare,
			final NSMutableArray out_objectAdded, final NSMutableArray out_missingObject)
	{

		if ((in_referenceArray != null) && (in_ArrayToCompare != null)) {

			Object lc_object;
			if (out_missingObject != null)
			{
				out_missingObject.removeAllObjects();

				for (int i = 0; i < in_referenceArray.count(); i++)
				{
					lc_object = in_referenceArray.objectAtIndex(i);
					if (!in_ArrayToCompare.containsObject(lc_object))
					{
						out_missingObject.addObject(lc_object);
					}
				}
			}
			if (out_objectAdded != null)
			{
				out_objectAdded.removeAllObjects();

				for (int i = 0; i < in_ArrayToCompare.count(); i++)
				{
					lc_object = in_ArrayToCompare.objectAtIndex(i);
					if (!in_referenceArray.containsObject(lc_object))
					{
						out_objectAdded.addObject(lc_object);
					}
				}
			}
		}
	}

	/**
	 * This method returns an array of Integer built from a String passed in parameter. Each Integer must be separated by a
	 * carriage return. It deals with "\r" (typically a copy/paste from Excel for example).
	 *
	 * @param in_str
	 *
	 */
	public static NSArray<Integer> splitStringOfIntegerInArray(final String in_str)
	{
		NSMutableArray<Integer> lc_arrayValue = new NSMutableArray<Integer>();

		String lc_str = in_str.replaceAll("\r", "");

		String[] lc_arrayStr = lc_str.split("\n");
		if((lc_arrayStr!=null)&&(lc_arrayStr.length>0)){
			int lc_nb = lc_arrayStr.length;
			Integer lc_integer;
			for(int i=0;i<lc_nb;i++){
				try{
					lc_integer = new Integer(Integer.parseInt(lc_arrayStr[i]));
					lc_arrayValue.addObject(lc_integer);
				} catch (Exception e){
				}
			}
		}
		return lc_arrayValue;
	}

	/**
	 * This method returns an array of String built from a String passed in parameter. Each String must be separated by a
	 * carriage return. It deals with "\r" (typically a copy/paste from Excel for example).
	 *
	 * @param in_str
	 *
	 */
	public static NSArray<String> splitStringOfStringInArray(final String in_str)
	{
		if (in_str == null || (in_str!=null && in_str.equals("")))
			return NSArray.emptyArray();

		String lc_str = in_str.replaceAll("\r", "");
		return splitStringWithCharInArray(lc_str, "\n");
	}

	public static NSArray<String> splitStringWithCharInArray(final String in_str, final String separator)
	{
		if (in_str == null || in_str.length() == 0)
			return NSArray.emptyArray();

		String[] lc_arrayStr = in_str.split(separator);

		if (lc_arrayStr!=null && lc_arrayStr.length>0)
		{
			int lc_nb = lc_arrayStr.length;
			NSMutableArray<String> lc_arrayValue = new NSMutableArray<String>(lc_nb);

			for (int i=0;i<lc_nb;i++)
				lc_arrayValue.addObject(lc_arrayStr[i]);
			return lc_arrayValue.immutableClone();
		}
		return NSArray.emptyArray();
	}

	/**
	 * This method returns a string build with the array items separated with "\n"
	 *
	 * @param array
	 * @return string
	 */
	public static String transformArrayInString(final List<?> array)
	{
		String tempStr = "";
		for (Object object : array) {
			tempStr = tempStr.concat(object.toString()).concat("\n");
		}
		return tempStr;
	}

	/**
	 * Some attributes store an array as a string because it's obviously simpler to edit. However, we must check
	 * that the values are correctly saved (with no space, only one comma between values, ...<p>
	 * This method does the job.
	 *
	 * @param value to clean up
	 * @return a string without space, only one comma between values and return null if the string is empty.
	 */
	public static String cleanStringForArrayAttribute(final String value)
	{
		String aString = ERXStringUtilities.removeSpaces(value);
		// Remove extra ,
		if (!ERXStringUtilities.stringIsNullOrEmpty(aString))
		{
			StringBuilder noDupes = new StringBuilder();
			char previousChar = aString.charAt(0);
			if (previousChar != ',')
				noDupes.append(previousChar);
			for (int i = 1; i < aString.length(); i++)
			{
				char currentChar = aString.charAt(i);
				if ((currentChar == ',' && previousChar != ',' && i != aString.length()) || currentChar != ',')
				{
					noDupes.append(currentChar);
					previousChar = currentChar;
				}
			}
			// A comma can still present at the end if there are several , at the end like blabla ,,,,
			if (noDupes.length() > 0 && noDupes.charAt(noDupes.length()-1) == ',')
				noDupes.deleteCharAt(noDupes.length()-1);
			aString =  noDupes.toString();
		}
		return ERXStringUtilities.nullForEmptyString(aString);
	}


	/**
	 * writeDataToFile has been removed. User the following code instead: <p>
	 * InputStream is = new ByteArrayInputStream(data.bytes());
	 * ERXFileUtilities.writeInputStreamToFile(InputStream stream, File file)
	 */

	/**
	 *
	 * La methode writeDataToFile ecrit le contenu de in_data dans le fichier in_file
	 * Si in_append est vrai, in_data est ajoute au contenu existant de in_file
	 *
	 * @param in_data
	 * @param in_file
	 * @param in_append
	 * @return
	 */
	public static boolean writeDataToFile(final NSData in_data, final File in_file, final boolean in_append)
	{
		boolean lc_noError=false;
		OutputStream lc_stream=null;
		try {
			lc_stream = new FileOutputStream(in_file,in_append);
			in_data.writeToStream(lc_stream);
			lc_noError=true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(lc_stream!=null){
				try {
					lc_stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return lc_noError;
	}


	/**
	 * Escape $ and \\ for Matcher replacement. Use this with java1.4, for java1.5 or greater use Matcher.quoteReplacement<br>
	 * http://cephas.net/blog/2006/02/09/javalangillegalargumentexception-illegal-group-reference-replaceall-and-dollar-signs/
	 */
	public static String matcherQuoteReplacement(final String s)
	{
		if(s.indexOf('\\') == -1 && s.indexOf('$') == -1)
			return s;
		StringBuffer stringbuffer = new StringBuffer();
		for(int i = 0; i < s.length(); i++)
		{            char c = s.charAt(i);
		if(c == '\\')
		{               stringbuffer.append('\\');
		stringbuffer.append('\\');
		continue;
		}
		if(c == '$')
		{
			stringbuffer.append('\\');
			stringbuffer.append('$');
		} else
		{
			stringbuffer.append(c);
		}
		}
		return stringbuffer.toString();
	}

	public static String generateRandomAlphaNumericNumber(final int in_length)
	{
		StringBuffer lc_string=new StringBuffer();
		for(int i=0;i<in_length;i++){
			int lc_num=(int)(Math.random()*35);
			if(lc_num<26){
				lc_string.append((char)(lc_num+0x61));
			}
			else{
				lc_string.append((char)(lc_num+0x30-26));
			}
		}
		return lc_string.toString();
	}

	/**
	 * getContentFromUrl has been removed. User the wonder equivalent instead: <p>
	 * ERXStringUtilities.stringFromURL(URL url, String encoding)
	 */

	/**
	 * getFilesFromPath has been removed. User the wonder equivalent instead: <p>
	 * ERXFileUtilities.listFiles(File baseDir, boolean recursive, FileFilter filter)
	 */

	/**
	 * Cette methode retourne la liste des fichiers presents dans un jar du classpath et dans un package donne
	 *
	 * @param in_jarPath Le chemin du jar
	 * @param in_pckgName Le nom du package
	 * @return La liste des fichiers
	 */
	public static List<File> getFilesFromJar(final String in_jarPath, final String in_pckgName)
	{
		Vector<File> lc_vecteur = new Vector<File>();

		if((in_jarPath!=null) && (in_pckgName!=null)) {
			JarFile lc_jfile;
			try {
				lc_jfile = new JarFile(in_jarPath);

				String lc_pkgpath = in_pckgName.replace(".", "/");
				for (Enumeration<JarEntry> lc_entries = lc_jfile.entries(); lc_entries.hasMoreElements();) {
					JarEntry lc_element = lc_entries.nextElement();
					String lc_elementName = lc_element.getName();
					if((!lc_element.isDirectory()) && (lc_elementName.startsWith(lc_pkgpath))) {
						lc_vecteur.add(new File(lc_elementName));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return lc_vecteur;
	}

}
