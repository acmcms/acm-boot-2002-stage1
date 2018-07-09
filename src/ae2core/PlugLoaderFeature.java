/**
 * Created on 25.11.2002
 * 
 * myx - barachta */
package ae2core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import ru.myx.ae3.help.Convert;
import ru.myx.ae3.help.Format;
import ru.myx.ae3.report.Report;
import ae2core.handlers.lfsfeat.Handler;

/**
 * @author myx
 */
final class PlugLoaderFeature extends ClassLoader {
	static final class Feature {
		final String	featureName;
		
		List<Long>		version	= null;
		
		boolean			zip		= false;
		
		java.io.File	file	= null;
		
		Feature(final String featureName) {
			this.featureName = featureName;
		}
		
		java.io.File getFile() {
			return this.file;
		}
		
		List<Long> getVersion() {
			return this.version;
		}
		
		boolean isZip() {
			return this.zip;
		}
	}
	
	static final class FeatureChooserFilter implements java.io.FileFilter {
		@Override
		public boolean accept(final java.io.File pathname) {
			try {
				final String fullname = pathname.getName();
				final String least;
				final String name;
				final Feature entry;
				{
					final int posV = fullname.lastIndexOf( '-' );
					if (posV == -1) {
						System.out.println( "AE2-CORE: skipping: " + fullname );
						return false;
					}
					least = fullname.substring( posV + 1 ).toLowerCase();
					name = fullname.substring( 0, posV );
					final Feature feature = PlugLoaderFeature.FEATURES.get( name );
					if (feature == null) {
						entry = new Feature( name );
						PlugLoaderFeature.FEATURES.put( name, entry );
					} else {
						entry = feature;
					}
				}
				if (pathname.isDirectory()) {
					final List<Long> version = Plug.parseVersion( least );
					if (Plug.compareVersions( version, entry.version ) > 0) {
						entry.version = version;
						entry.file = pathname;
						entry.zip = false;
					}
					return true;
				}
				if (pathname.isFile() && (least.endsWith( ".jar" ) || least.endsWith( ".zip" ))) {
					final List<Long> version = Plug.parseVersion( least.substring( 0, least.length() - 4 ) );
					if (Plug.compareVersions( version, entry.version ) > 0) {
						entry.version = version;
						entry.file = pathname;
						entry.zip = true;
					}
					return true;
				}
				System.out.println( "AE2-CORE: skipping (unknown type): " + fullname );
				return false;
			} catch (final Throwable t) {
				System.out.println( "AE2-CORE: skipping: " + pathname + ", error: " + Format.Throwable.toText( t ) );
				return false;
			}
		}
	}
	
	static final Map<String, Feature>	FEATURES	= new TreeMap<>();
	
	static Plug.Feature[] getFeatures(final ClassLoader parent, final PlugLoaderCore commonLoader, final File settings)
			throws Exception {
		final List<Plug.Feature> features = new ArrayList<>();
		for (final Map.Entry<String, PlugLoaderFeature.Feature> current : PlugLoaderFeature.FEATURES.entrySet()) {
			final Feature feature = current.getValue();
			final String featureName = feature.featureName;
			final File featureSettingsFile = new File( settings, featureName );
			final Properties featureSettings = new Properties();
			if (featureSettingsFile.exists()) {
				try (final FileInputStream fin = new FileInputStream( featureSettingsFile )) {
					featureSettings.load( fin );
				}
			}
			final Properties featureSettingsNew = new Properties();
			featureSettingsNew.putAll( featureSettings );
			featureSettingsNew.setProperty( "name", featureName );
			featureSettingsNew.setProperty( "version", PlugLoaderFeature.toVersion( feature.version ) );
			if (!featureSettingsNew.containsKey( "enable" )) {
				featureSettingsNew.setProperty( "enable", "true" );
			}
			if (PlugLoaderFeature.mapsDiffer( featureSettings, featureSettingsNew )) {
				try (final FileOutputStream fout = new FileOutputStream( featureSettingsFile )) {
					featureSettingsNew.store( fout, "feature settings" );
				}
			}
			final boolean enable = Convert.MapEntry.toBoolean( featureSettingsNew, "enable", true );
			if (enable) {
				final PlugLoaderFeature featureLoader = new PlugLoaderFeature( feature, parent, commonLoader );
				if (Plug.getObsolete( featureLoader.source )) {
					System.out.println( "AE2-CORE: skipping (obsolete): " + featureName );
					continue;
				}
				final String mainClass = Plug.getMainClass( featureLoader.source );
				if (mainClass != null) {
					features.add( new Plug.Feature() {
						@Override
						public ClassLoader getClassLoader() {
							return featureLoader;
						}
						
						@Override
						public String getFeatureName() {
							return featureName;
						}
						
						@Override
						public String getMainClassName() {
							return mainClass;
						}
					} );
				} else {
					System.out.println( "AE2-CORE: skipping (no main class): " + featureName );
				}
			} else {
				System.out.println( "AE2-CORE: skipping (disabled): " + featureName );
			}
		}
		return features.toArray( new Plug.Feature[features.size()] );
	}
	
	private static final boolean mapsDiffer(final Map<Object, Object> first, final Map<Object, Object> second) {
		if (first.size() != second.size()) {
			return true;
		}
		for (final Object key : first.keySet()) {
			if (second.containsKey( key )) {
				final Object firstValue = first.get( key );
				final Object secondValue = second.get( key );
				if (firstValue == null) {
					if (secondValue != null) {
						return true;
					}
				} else {
					if (firstValue != secondValue && !firstValue.equals( secondValue )) {
						return true;
					}
				}
			} else {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * IN THE MEMORY FOR THE PAST<code>
	private static final String toVersion(final long version) {
		final StringBuilder buffer = new StringBuilder();
		for (long current = version; current > 0; current <<= 8) {
			if (current != version) {
				buffer.append( '.' );
			}
			buffer.append( (current >> 56) & 0xFF );
		}
		return buffer.toString();
	}
	 * </code>
	 * 
	 * @param version
	 * @return
	 */
	private static final String toVersion(final List<Long> version) {
		final StringBuilder buffer = new StringBuilder();
		for (final Long current : version) {
			if (buffer.length() > 0) {
				buffer.append( '.' );
			}
			buffer.append( current );
		}
		return buffer.toString();
	}
	
	protected final Source		source;
	
	private final Feature		feature;
	
	private final ClassLoader	parent;
	
	private final ClassLoader	common;
	
	PlugLoaderFeature(final Feature feature, final ClassLoader parent, final PlugLoaderCore commonLoader)
			throws Exception {
		super( commonLoader );
		this.feature = feature;
		this.parent = parent;
		this.common = commonLoader;
		final File current = feature.getFile();
		final boolean zip = feature.isZip();
		final Source source = zip
				? new SourceZip( current )
				: new SourceFiles( current );
		// : new SourceDirect( new SourceFiles( current ) );
		final String exportList = Plug.getExport( source ).trim().replace( ';', ',' );
		for (final StringTokenizer st = new StringTokenizer( exportList, "," ); st.hasMoreTokens();) {
			final String currentExport = st.nextToken().trim();
			if (currentExport.length() == 0) {
				continue;
			}
			commonLoader.registerExport( this, currentExport, source );
		}
		Handler.SOURCES.put( feature.featureName, this.source = source );
		if (Report.MODE_DEBUG) {
			System.out.println( "AE2-CORE: Next feature: " + feature.featureName );
		}
	}
	
	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		final byte[] data = this.source.getClass( name );
		if (data == null) {
			throw new ClassNotFoundException( "Not found: " + name );
		}
		return this.defineClass( name, data, 0, data.length );
	}
	
	@Override
	protected URL findResource(final String name) {
		final byte[] data = this.source.get( name );
		if (data == null) {
			return null;
		}
		try {
			return new URL( Handler.URL_PREFFIX + this.feature.featureName + '/' + name );
		} catch (final MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		Class<?> c = this.findLoadedClass( name );
		if (c != null) {
			return c;
		}
		final byte[] data = this.source.getClass( name );
		if (data != null) {
			if (name.indexOf( '.' ) == -1) {
				c = this.defineClass( name, data, 0, data.length );
				if (resolve) {
					this.resolveClass( c );
				}
				return c;
			}
			try {
				c = this.parent.loadClass( name );
			} catch (final ClassNotFoundException e) {
				// If still not found, then call findClass<?> in order
				// to find the class.
				c = this.findClass( name );
			}
			if (resolve) {
				this.resolveClass( c );
			}
			return c;
		}
		c = this.common.loadClass( name );
		if (resolve) {
			this.resolveClass( c );
		}
		return c;
	}
	
	@Override
	public String toString() {
		return "FT_LOADER, feature: " + this.feature.featureName + ", source: " + this.source;
	}
}
