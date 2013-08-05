package fr.sophiacom.corason.foundation.util;

/** 
 * This class contains static methods that can be used to debug an application
 * 
 * @author Philippe Rabier
 *
 */
public class CODebugUtilities 
{

	public static String getStackTraceAsString()
	{
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : stackTrace) 
		{
			sb.append(element.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
}
