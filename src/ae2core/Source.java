/*
 * Created on 01.11.2005
 */
package ae2core;

/**
 * @author myx
 * 
 */
public interface Source {
	/**
	 * Used by the URLStreamHandlers to distinguish a folder with no cost.
	 */
	public static final byte[]	FOLDER	= new byte[0];
	
	/**
	 * @param name
	 * @return byte array
	 */
	byte[] get(final String name);
	
	/**
	 * @param name
	 * @return byte array
	 */
	byte[] getClass(final String name);
	
	/**
	 * @return boolean
	 */
	boolean isDirect();
	
	/**
	 * @return string array
	 */
	String[] list();
}
