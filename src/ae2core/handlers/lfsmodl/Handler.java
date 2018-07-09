/*
 * Created on 30.05.2003
 * 
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ae2core.handlers.lfsmodl;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;

import ae2core.Source;

/**
 * @author myx
 * 
 */
public class Handler extends URLStreamHandler {
	/**
	 * 
	 */
	public static final String				URL_PREFFIX	= "lfsmodl://";
	
	/**
	 * 
	 */
	public static final Map<String, Source>	SOURCES		= new HashMap<>();
	
	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		return new Connection( url );
	}
}
