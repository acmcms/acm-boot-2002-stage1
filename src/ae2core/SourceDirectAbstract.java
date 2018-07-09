/*
 * Created on 01.11.2005
 */
package ae2core;

import java.util.HashMap;
import java.util.Map;

import ru.myx.ae3.help.Format;

/**
 * @author myx
 * 
 */
public abstract class SourceDirectAbstract implements Source {
	final Map<String, byte[]>	data		= new HashMap<>( 256, 0.5f );
	
	int							codeBytes	= 0;
	
	int							dataBytes	= 0;
	
	@Override
	public final byte[] get(final String name) {
		final String key;
		if (name.endsWith( ".class" )) {
			key = name.substring( 0, name.length() - 6 ).replace( '/', '.' );
		} else {
			key = name;
		}
		return this.data.get( key );
	}
	
	@Override
	public final byte[] getClass(final String key) {
		return this.data.get( key );
	}
	
	@Override
	public final boolean isDirect() {
		return true;
	}
	
	/**
	 * @param name
	 * @param bytes
	 */
	protected void put(final String name, final byte[] bytes) {
		this.data.put( name, bytes );
		if (name.endsWith( ".class" )) {
			this.codeBytes += bytes.length;
			this.data.put( name.substring( 0, name.length() - 6 ).replace( '/', '.' ), bytes );
		} else {
			this.dataBytes += bytes.length;
		}
		final int pos = name.lastIndexOf( '/' );
		if (pos != -1) {
			final String folder = name.substring( 0, pos );
			if (!this.data.containsKey( folder )) {
				this.data.put( folder, Source.FOLDER );
			}
		}
	}
	
	@Override
	public final String toString() {
		return "DIRECT: entries="
				+ Format.Compact.toDecimal( this.data.size() )
				+ ", code="
				+ Format.Compact.toBytes( this.codeBytes )
				+ ", data="
				+ Format.Compact.toBytes( this.dataBytes );
	}
}
