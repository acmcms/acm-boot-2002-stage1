/**
 * Created on 25.11.2002
 * 
 * myx - barachta */
package ae2core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.myx.ae3.report.Report;
import ae2core.handlers.lfsmodl.Handler;

/**
 * @author myx
 * 
 * myx - barachta 
 *         "typecomment": Window>Preferences>Java>Templates. To enable and
 *         disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
final class PlugLoaderModule extends ClassLoader {
	static final class Module {
		final String	moduleName;
		
		List<Long>		version	= null;
		
		boolean			zip		= false;
		
		java.io.File	file	= null;
		
		Module(final String moduleName) {
			this.moduleName = moduleName;
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
	
	static final class ModuleChooserFilter implements java.io.FileFilter {
		@Override
		public boolean accept(final java.io.File pathname) {
			try {
				final String fullname = pathname.getName();
				final String least;
				final String name;
				final Module entry;
				{
					final int posV = fullname.lastIndexOf( '-' );
					if (posV == -1) {
						System.out.println( "AE2-CORE: skipping: " + fullname );
						return false;
					}
					least = fullname.substring( posV + 1 );
					name = fullname.substring( 0, posV );
					final Module module = PlugLoaderModule.MODULES.get( name );
					if (module == null) {
						entry = new Module( name );
						PlugLoaderModule.MODULES.put( name, entry );
					} else {
						entry = module;
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
				return false;
			} catch (final Throwable t) {
				return false;
			}
		}
	}
	
	static final Map<String, PlugLoaderModule.Module>	MODULES	= new TreeMap<>();
	
	static Plug.Module[] getModules(final PlugLoaderCore parent) throws Exception {
		final List<Plug.Module> modules = new ArrayList<>();
		for (final Map.Entry<String, Module> current : PlugLoaderModule.MODULES.entrySet()) {
			final Module module = current.getValue();
			final String moduleName = module.moduleName;
			final PlugLoaderModule moduleLoader = new PlugLoaderModule( module, parent );
			if (Plug.getObsolete( moduleLoader.source )) {
				continue;
			}
			final String mainClass = Plug.getMainClass( moduleLoader.source );
			if (mainClass != null) {
				modules.add( new Plug.Module() {
					@Override
					public ClassLoader getClassLoader() {
						return moduleLoader;
					}
					
					@Override
					public String getMainClassName() {
						return mainClass;
					}
					
					@Override
					public String getModuleName() {
						return moduleName;
					}
				} );
			}
		}
		return modules.toArray( new Plug.Module[modules.size()] );
	}
	
	protected final Source	source;
	
	private final Module	module;
	
	PlugLoaderModule(final Module module, final PlugLoaderCore parent) throws Exception {
		super( parent );
		this.module = module;
		final File current = module.getFile();
		final boolean zip = module.isZip();
		final Source source = zip
				? new SourceZip( current )
				: new SourceFiles( current );
		// : new SourceDirect( new SourceFiles( current ) );
		Handler.SOURCES.put( module.moduleName, this.source = source );
		if (Report.MODE_DEBUG) {
			System.out.println( "AE2-CORE: Next module: " + module.moduleName );
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
			return new URL( Handler.URL_PREFFIX + this.module.moduleName + '/' + name );
		} catch (final MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "MD_LOADER, module: " + this.module.moduleName + ", source: " + this.source;
	}
}
