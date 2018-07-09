/*
 * Created on 01.11.2005
 */
package ae2core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author myx
 * 
 */
public final class SourceZip extends SourceDirectAbstract {
	final String[]	contents;
	
	/**
	 * @param zip
	 * @throws Exception
	 */
	public SourceZip(final File zip) throws Exception {
		try (final ZipFile zf = new ZipFile( zip )) {
			final Enumeration<? extends ZipEntry> entries = zf.entries();
			final List<String> contents = new ArrayList<>();
			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				final int size = (int) entry.getSize();
				final String name = entry.getName().replace( '\\', '/' );
				try (final InputStream in = zf.getInputStream( entry )) {
					final byte[] data = new byte[size];
					int read = 0;
					for (; read < size;) {
						final int rd = in.read( data, read, size - read );
						if (rd < 1) {
							break;
						}
						read += rd;
					}
					if (read < size) {
						throw new IOException( "Read less than requested!" );
					}
					this.put( name, data );
					contents.add( name );
				}
			}
			this.contents = contents.toArray( new String[contents.size()] );
		}
	}
	
	@Override
	public final String[] list() {
		return this.contents;
	}
}
