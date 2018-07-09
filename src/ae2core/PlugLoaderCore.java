/*
 * Created on 29.09.2003
 * 
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ae2core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author myx
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
class PlugLoaderCore extends ClassLoader {
	private final Map<String, PlugLoaderFeature>	exported	= new HashMap<>();
	
	PlugLoaderCore() {
		super( PlugLoaderCore.class.getClassLoader() );
	}
	
	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		final PlugLoaderFeature loader = this.exported.get( name );
		if (loader == null) {
			throw new ClassNotFoundException( "Not found: " + name );
		}
		return loader.loadClass( name );
	}
	
	void registerExport(final PlugLoaderFeature loader, final String export, final Source source) {
		final String exportMatcher = export.replace( '.', '/' ) + '/';
		final String[] entries = source.list();
		for (int i = entries.length - 1; i >= 0; --i) {
			final String current = entries[i];
			if (current.startsWith( exportMatcher ) && current.endsWith( ".class" )) {
				final String className = current.substring( 0, current.length() - 6 ).replace( '/', '.' );
				this.exported.put( className, loader );
			}
		}
	}
}
