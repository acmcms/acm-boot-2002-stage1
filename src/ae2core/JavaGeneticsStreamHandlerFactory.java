/*
 * Created on 08.06.2003
 * 
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ae2core;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.PrivilegedAction;
import java.util.StringTokenizer;

import ru.myx.ae3.help.Convert;

/**
 * @author myx
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
final class JavaGeneticsStreamHandlerFactory implements URLStreamHandlerFactory {
	private static final String	PROTOCOL_PATH_PROPERTY	= "java.protocol.handler.pkgs";
	
	private final String		original				= System.getProperty( JavaGeneticsStreamHandlerFactory.PROTOCOL_PATH_PROPERTY,
																"" ) + '|';
	
	@Override
	public final URLStreamHandler createURLStreamHandler(final String protocol) {
		final StringTokenizer packagePrefixIter = this.getTokenizer();
		while (packagePrefixIter.hasMoreTokens()) {
			final String packagePrefix = packagePrefixIter.nextToken().trim();
			if (packagePrefix.length() == 0) {
				continue;
			}
			try {
				final String clsName = packagePrefix + "." + protocol + ".Handler";
				try {
					final Class<?> cls = Class.forName( clsName );
					if (cls != null) {
						final URLStreamHandler handler = (URLStreamHandler) cls.newInstance();
						if (handler != null) {
							return handler;
						}
					}
				} catch (final ClassNotFoundException e) {
					final ClassLoader cl = ClassLoader.getSystemClassLoader();
					if (cl != null) {
						final Class<?> cls = cl.loadClass( clsName );
						if (cls != null) {
							final URLStreamHandler handler = (URLStreamHandler) cls.newInstance();
							if (handler != null) {
								return handler;
							}
						}
					}
				}
			} catch (final Exception e) {
				// any number of exceptions can get thrown here
			}
		}
		return null;
	}
	
	private final StringTokenizer getTokenizer() {
		final PrivilegedAction<String> action = Convert.Any
				.toAny( new sun.security.action.GetPropertyAction( JavaGeneticsStreamHandlerFactory.PROTOCOL_PATH_PROPERTY,
						"" ) );
		final String packagePrefixList = this.original + java.security.AccessController.doPrivileged( action );
		return new StringTokenizer( packagePrefixList, "|" );
	}
}
