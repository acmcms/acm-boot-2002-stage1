/*
 * Created on 30.05.2003
 * 
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ae2core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import ru.myx.io.DataInputByteArrayFast;

/**
 * @author myx
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
final class Plug {
	/**
	 * All features a connected to a core, so core class loader should be
	 * parental for any feature.
	 */
	static interface Feature {
		/**
		 * @return string
		 */
		ClassLoader getClassLoader();
		
		/**
		 * @return string
		 */
		String getFeatureName();
		
		/**
		 * @return string
		 */
		String getMainClassName();
	}
	
	/**
	 * All modules a connected to a core, so core class loader should be
	 * parental for any feature.
	 */
	static interface Module {
		/**
		 * @return string
		 */
		ClassLoader getClassLoader();
		
		/**
		 * @return string
		 */
		String getMainClassName();
		
		/**
		 * @return string
		 */
		String getModuleName();
	}
	
	/**
	 * @param v1
	 * @param v2
	 * @return
	 */
	static final int compareVersions(final List<Long> v1, final List<Long> v2) {
		if (v2 == null) {
			return v1 == null
					? 0
					: 1;
		}
		final int v1l = v1.size();
		final int v2l = v2.size();
		for (int i = 0;; ++i) {
			if (v1l <= i) {
				return v2l == v1l
						? 0
						: -1;
			}
			if (v2l <= i) {
				return 1;
			}
			final int compare = v1.get( i ).compareTo( v2.get( i ) );
			if (compare == 0) {
				continue;
			}
			return compare;
		}
	}
	
	static String getExport(final Source source) throws Exception {
		final byte[] data = source.get( "META-INF/MANIFEST.MF" );
		if (data == null) {
			return Plug.getProperties( source, new Properties() ).getProperty( "export.list", "" );
		}
		final String tmp = new String( data ).replace( '\r', '\n' );
		final int p1 = tmp.toLowerCase().indexOf( "\nexport-list:" );
		if (p1 == -1) {
			return "";
		}
		final int p2 = tmp.indexOf( '\n', p1 + 13 );
		final String result = p2 == -1
				? tmp.substring( p1 + 13 )
				: tmp.substring( p1 + 13, p2 );
		return result.trim();
	}
	
	static String getMainClass(final Source source) throws Exception {
		final byte[] data = source.get( "META-INF/MANIFEST.MF" );
		if (data == null) {
			return Plug.getProperties( source, new Properties() ).getProperty( "main.class", "Main" );
		}
		final String tmp = new String( data ).replace( '\r', '\n' );
		final int p1 = tmp.toLowerCase().indexOf( "\nmain-class:" );
		if (p1 == -1) {
			return null;
		}
		final int p2 = tmp.indexOf( '\n', p1 + 12 );
		final String result = (p2 == -1
				? tmp.substring( p1 + 12 )
				: tmp.substring( p1 + 12, p2 )).trim();
		return result.trim();
	}
	
	static boolean getObsolete(final Source source) {
		final byte[] data = source.get( "META-INF/MANIFEST.MF" );
		if (data == null) {
			return false;
		}
		final String tmp = new String( data ).replace( '\r', '\n' );
		final int p1 = tmp.toLowerCase().indexOf( "\nobsolete:" );
		if (p1 == -1) {
			return false;
		}
		final int p2 = tmp.indexOf( '\n', p1 + 10 );
		return "true".equals( p2 == -1
				? tmp.substring( p1 + 10 ).trim()
				: tmp.substring( p1 + 10, p2 ).trim() );
	}
	
	private static Properties getProperties(final Source source, final Properties parent) throws IOException {
		final Properties result = new Properties( parent );
		if (parent == System.getProperties()) {
			result.putAll( parent );
		}
		{
			final byte[] data = source.get( "properties" );
			if (data != null) {
				final Properties local = new Properties();
				local.load( new DataInputByteArrayFast( data ) );
				for (final Enumeration<?> e = local.propertyNames(); e.hasMoreElements();) {
					final String current = e.nextElement().toString();
					if (!result.containsKey( current )) {
						result.setProperty( current, local.getProperty( current ) );
					}
				}
			}
		}
		{
			final byte[] data = source.get( ".properties" );
			if (data != null) {
				final Properties local = new Properties();
				local.load( new DataInputByteArrayFast( data ) );
				for (final Enumeration<?> e = local.propertyNames(); e.hasMoreElements();) {
					final String current = e.nextElement().toString();
					if (!result.containsKey( current )) {
						result.setProperty( current, local.getProperty( current ) );
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * IN MEMORY OF OLDER VERSIONS <code>
	static final long parseVersion(final String version) {
		int cByte = 0;
		long result = 0;
		final StringTokenizer st = new StringTokenizer( version, "." );
		while (st.hasMoreTokens()) {
			final String current = st.nextToken();
			final long value = Integer.parseInt( current );
			if (value > 127) {
				throw new IllegalArgumentException( "Version value contains numbers greater than 127, version="
						+ version
						+ ", value="
						+ current );
			}
			result |= value << ((7 - (cByte++)) * 8);
		}
		return result;
	}
	 * </code>
	 * 
	 * @param version
	 * @return
	 */
	static final List<Long> parseVersion(final String version) {
		final List<Long> result = new ArrayList<>( 4 );
		final StringTokenizer st = new StringTokenizer( version, "." );
		while (st.hasMoreTokens()) {
			final String current = st.nextToken();
			final long value = Integer.parseInt( current );
			result.add( new Long( value ) );
		}
		return result;
	}
	
	private Plug() {
		// empty
	}
}
