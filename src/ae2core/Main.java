package ae2core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ru.myx.ae3.Engine;
import ru.myx.ae3.console.MainConsole;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.help.Format;
import ru.myx.ae3.report.Report;
import ru.myx.ae3.vfs.Storage;
import ru.myx.ae3boot.ThreadBootACM;
import ae2core.Plug.Feature;
import ae2core.Plug.Module;

/**
 * Created on 31.10.2002
 * 
 * myx - barachta */
/**
 * @author barachta
 * 
 */
public class Main {
	private static boolean	virgin	= true;
	
	private static final void initFeatures(final PlugLoaderCore commonLoader, final String[] args) throws Exception {
		System.out.println( "AE2-CORE: Initializing features..." );
		final PlugLoaderFeature.FeatureChooserFilter filter = new PlugLoaderFeature.FeatureChooserFilter();
		final File root = new File( Engine.PATH_PUBLIC, "features" );
		if (!root.exists()) {
			if (!root.mkdirs()) {
				throw new IOException( "Cannot create directory: path=" + root.getAbsolutePath() );
			}
		}
		if (!root.isDirectory()) {
			throw new IllegalArgumentException( "Features is not a directory: path=" + root.getAbsolutePath() );
		}
		root.listFiles( filter );
		final File settings = new File( new File( Engine.PATH_PROTECTED, "settings" ), ".features" );
		if (!settings.exists()) {
			if (!settings.mkdirs()) {
				throw new IOException( "Cannot create directory: path=" + settings.getAbsolutePath() );
			}
		}
		if (!settings.isDirectory()) {
			throw new IllegalArgumentException( "Features: 'settings' is not a directory: path="
					+ settings.getAbsolutePath() );
		}
		final Plug.Feature[] features = PlugLoaderFeature.getFeatures( Main.class.getClassLoader(),
				commonLoader,
				settings );
		System.out.println( "AE2-CORE: invoking features, " + features.length + " found." );
		System.out.flush();
		for (final Feature current : features) {
			if (Report.MODE_DEBUG) {
				System.out.println( "AE2-CORE: invoking feature: " + current.getFeatureName() );
				System.out.flush();
			}
			final ClassLoader boot = current.getClassLoader();
			if (Report.MODE_DEBUG) {
				System.out.println( "AE2-CORE: Boot from: " + boot );
				System.out.flush();
			}
			final String mainClass = current.getMainClassName();
			if (Report.MODE_DEBUG) {
				System.out.println( "AE2-CORE: Main class: " + mainClass );
				System.out.flush();
			}
			try {
				Main.invokeClass( args, boot, mainClass );
			} catch (final Throwable t) {
				System.out
						.println( Format.Throwable.toText( "Error initializing feature: " + current.getFeatureName(),
								t ) );
			}
		}
	}
	
	private static final void initHandlers() {
		final String property = System.getProperty( "java.protocol.handler.pkgs", "" );
		if (property.indexOf( "ae2core.handlers" ) == -1) {
			if (property.length() == 0) {
				System.setProperty( "java.protocol.handler.pkgs", "ae2core.handlers" );
			} else {
				System.setProperty( "java.protocol.handler.pkgs", property + "|ae2core.handlers" );
			}
			try {
				new URL( ae2core.handlers.lfsfeat.Handler.URL_PREFFIX + "test" ).getProtocol();
			} catch (final Throwable t) {
				t.printStackTrace();
			}
			try {
				new URL( ae2core.handlers.lfsmodl.Handler.URL_PREFFIX + "test" ).getProtocol();
			} catch (final Throwable t) {
				t.printStackTrace();
			}
			System.setProperty( "java.protocol.handler.pkgs", property );
		}
	}
	
	private static final void initModules(final PlugLoaderCore commonLoader) throws Exception {
		System.out.println( "AE2-CORE: Initializing modules..." );
		final PlugLoaderModule.ModuleChooserFilter filter = new PlugLoaderModule.ModuleChooserFilter();
		final File root = new File( Engine.PATH_PUBLIC, "modules" );
		if (!root.exists()) {
			if (!root.mkdirs()) {
				throw new IOException( "Cannot create directory: path=" + root.getAbsolutePath() );
			}
		}
		if (!root.isDirectory()) {
			throw new IllegalArgumentException( "Features is not a directory: path=" + root.getAbsolutePath() );
		}
		root.listFiles( filter );
		final Plug.Module[] modules = PlugLoaderModule.getModules( commonLoader );
		System.out.println( "AE2-CORE: invoking modules, " + modules.length + " found." );
		System.out.flush();
		for (final Module current : modules) {
			if (Report.MODE_DEBUG) {
				System.out.println( "AE2-CORE: invoking module: " + current.getModuleName() );
				System.out.flush();
			}
			final ClassLoader boot = current.getClassLoader();
			if (Report.MODE_DEBUG) {
				System.out.println( "AE2-CORE: Boot from: " + boot );
				System.out.flush();
			}
			final String mainClass = current.getMainClassName();
			if (Report.MODE_DEBUG) {
				System.out.println( "AE2-CORE: Main class: " + mainClass );
				System.out.flush();
			}
			System.setProperty( "command.after.init", "" );
			Main.invokeClass( new String[] {}, boot, mainClass );
			final String command = System.getProperty( "command.after.init", "" );
			if (command.length() > 0) {
				System.setProperty( "command.after.init", "" );
				final StringTokenizer st = new StringTokenizer( command, " " );
				final String commandClass = st.nextToken();
				final List<String> arguments = new ArrayList<>();
				for (; st.hasMoreTokens();) {
					arguments.add( st.nextToken() );
				}
				new Thread( "BOOT: async command: " + commandClass ) {
					{
						this.setDaemon( true );
					}
					
					@Override
					public void run() {
						System.out.println( "AE2-CORE: invoking async command: " + commandClass );
						System.out.flush();
						try {
							Main.invokeClass( arguments.toArray( new String[arguments.size()] ), boot, commandClass );
						} catch (final Throwable t) {
							System.out.println( Format.Throwable
									.toText( "AE2-CORE: Error while invoking async command:", t ) );
						}
					}
				}.start();
			}
		}
	}
	
	private static final void initStorage() {
		System.out.println( "AE2-CORE: Initializing storage..." );
	}
	
	static final void invokeClass(final String[] args, final ClassLoader loader, final String className)
			throws Exception {
		final Class<?> mainClass = Class.forName( className, true, loader );
		final Method mtd = mainClass.getMethod( "main", String[].class );
		if (mtd != null) {
			if (Report.MODE_DEBUG) {
				System.out.println( "AE2-CORE: Invoking '" + className + "'..." );
			}
			mtd.invoke( null, new Object[] { args } );
		} else {
			System.out.println( "AE2-CORE: No main method found ('" + className + "') - skipping." );
		}
	}
	
	/**
	 * @param args
	 * @throws Throwable
	 */
	public static void main(final String[] args) throws Throwable {
		System.out.println( "AE2-CORE: init start" );
		/**
		 * default log type is 'stderr', we're replacing default with 'files'.
		 */
		System.setProperty( "ru.myx.ae3.properties.log.type",
				System.getProperty( "ru.myx.ae3.properties.log.type", "files" ) );
		/**
		 * 
		 */
		ThreadBootACM.init();
		if (Main.virgin) {
			Main.virgin = false;
			URL.setURLStreamHandlerFactory( new JavaGeneticsStreamHandlerFactory() );
		}
		if (Tests.tests()) {
			return;
		}
		Main.initHandlers();
		final PlugLoaderCore commonLoader = new PlugLoaderCore();
		Main.initFeatures( commonLoader, args );
		Main.initStorage();
		if (args != null && args.length > 1) {
			if ("--tool".equals( args[0] )) {
				final File tools = new File( Engine.PATH_PUBLIC, "tools" );
				final ClassLoader loader = new URLClassLoader( new URL[] { tools.toURI().toURL() }, commonLoader );
				/**
				 * <code>
				final Source source = new SourceFiles( tools );
				if (Report.MODE_DEBUG) {
					System.out.println( "AE2-CORE: invoking tool: " + args[1] );
					System.out.flush();
				}
				final ClassLoader boot = current.getClassLoader();
				if (Report.MODE_DEBUG) {
					System.out.println( "AE2-CORE: Boot from: " + boot );
					System.out.flush();
				}
				final String mainClass = current.getMainClassName();
				if (Report.MODE_DEBUG) {
					System.out.println( "AE2-CORE: Main class: " + mainClass );
					System.out.flush();
				}
				System.setProperty( "command.after.init", "" );
				</code>
				 */
				final String[] arguments;
				if (args.length > 2) {
					arguments = new String[args.length - 2];
					System.arraycopy( args, 2, arguments, 0, arguments.length );
				} else {
					arguments = new String[] {};
				}
				Main.invokeClass( arguments, loader, args[1] );
			}
			return;
		}
		Main.initModules( commonLoader );
		if (args != null && args.length > 0 && "server".equals( args[0] )) {
			if (Thread.currentThread().isDaemon()) {
				System.out.println( "AE2-CORE: Daemon thread - exiting after initialization." );
				System.out.flush();
			} else {
				System.out.println( "AE2-CORE: Non-daemon thread - starting console thread." );
				System.out.flush();
				final Thread consoleThread = MainConsole.startConsoleThread( Storage.getRoot( Exec.getRootProcess() ),
						args,
						new String[] { "server" } );
				
				/**
				 * cannot be (always false) ^^^^^^
				 */
				if (Thread.currentThread().isDaemon()) {
					MainConsole.waitConsoleThread( consoleThread );
					System.out.println( "AE2-CORE: Non-daemon thread - console thread finished." );
					System.out.flush();
					return;
				}
				
				System.out.println( "AE2-CORE: Non-daemon thread - entering idle loop." );
				System.out.flush();
				for (; !Thread.interrupted();) {
					try {
						Thread.sleep( 15000L );
					} catch (final InterruptedException e) {
						break;
					}
				}
				System.out.println( "AE2-CORE: Non-daemon thread - finished idle loop." );
				System.out.flush();
			}
		}
	}
}
