/**
 * 
 */
package ae2core.handlers.lfsfeat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import ru.myx.ae3.Engine;
import ru.myx.io.DataInputByteArrayFast;
import ae2core.Source;

final class Connection extends URLConnection {
	private final byte[]	data;
	
	Connection(final URL url) throws IOException {
		super( url );
		final Source source = Handler.SOURCES.get( url.getHost() );
		if (source == null) {
			throw new IOException( "No such feature: " + url.getHost() );
		}
		final String name = url.getPath().substring( 1 );
		byte[] data = source.get( name );
		if (data == Source.FOLDER) {
			final String prefix = name.endsWith( "/" )
					? name
					: name + '/';
			final int prefixLength = prefix.length();
			final StringBuilder list = new StringBuilder();
			for (final String key : source.list()) {
				if (key.length() <= prefixLength
						|| !key.regionMatches( 0, name, 0, prefixLength )
						|| key.indexOf( '/', prefixLength - 1 ) != prefixLength - 1) {
					continue;
				}
				list.append( key.substring( prefixLength ) );
				list.append( '\r' );
				list.append( '\n' );
			}
			data = list.toString().getBytes( Engine.CHARSET_UTF8 );
		} else if (data == null) {
			throw new IOException( "No such data: " + url );
		}
		this.data = data;
	}
	
	@Override
	public void connect() {
		// empty
	}
	
	@Override
	public InputStream getInputStream() {
		return new DataInputByteArrayFast( this.data );
	}
}
