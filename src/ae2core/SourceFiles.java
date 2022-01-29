/*
 * Created on 01.11.2005
 */
package ae2core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author myx
 * 
 */
public final class SourceFiles implements Source {
	final File		root;
	
	final String	base;
	
	/**
	 * @param root
	 */
	public SourceFiles(final File root) {
		this.root = root;
		String base = root.getAbsolutePath();
		if (!base.endsWith( "/" ) && !base.endsWith( "\\" )) {
			base += '/';
		}
		this.base = base;
	}
	
	@Override
	public final byte[] get(final String name) {
		try {
			final File target = new File( this.root, name );
			if (target.isFile()) {
				try (final RandomAccessFile file = new RandomAccessFile( target, "r" )) {
					final int length = (int) file.length();
					final byte[] buffer = new byte[length];
					file.readFully( buffer );
					return buffer;
				}
			}
			if (target.isDirectory()) {
				final StringBuilder list = new StringBuilder();
				final File[] files = target.listFiles();
				for (final File file : files) {
					list.append( file.getName() );
					list.append( '\r' );
					list.append( '\n' );
				}
				return list.toString().getBytes( StandardCharsets.UTF_8 );
			}
			return null;
		} catch (final IOException e) {
			return null;
		}
	}
	
	@Override
	public final byte[] getClass(final String name) {
		try {
			try (final RandomAccessFile file = new RandomAccessFile( new File( this.root, //
					name.replace( '.', '/' ) + ".class" ), "r" )) {
				final int length = (int) file.length();
				final byte[] buffer = new byte[length];
				file.readFully( buffer );
				return buffer;
			}
		} catch (final IOException e) {
			return null;
		}
	}
	
	@Override
	public final boolean isDirect() {
		return false;
	}
	
	@Override
	public final String[] list() {
		final List<String> contents = new ArrayList<>();
		this.list( this.root, contents );
		return contents.toArray( new String[contents.size()] );
	}
	
	final void list(final File folder, final List<String> contents) {
		final File[] files = folder.listFiles();
		if (files != null) {
			for (final File current : files) {
				if (current.isDirectory()) {
					this.list( current, contents );
				}
				if (current.isFile() && current.canRead()) {
					contents.add( current.getAbsolutePath().substring( this.base.length() ).replace( '\\', '/' ) );
				}
			}
		}
	}
}
