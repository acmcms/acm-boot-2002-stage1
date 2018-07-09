/*
 * Created on 01.11.2005
 */
package ae2core;

/**
 * @author myx
 * 
 */
public final class SourceDirect extends SourceDirectAbstract {
	final String[]	contents;
	
	/**
	 * @param source
	 */
	public SourceDirect(final Source source) {
		if (source.isDirect()) {
			throw new IllegalArgumentException( "Source is already direct!" );
		}
		this.contents = source.list();
		for (final String current : this.contents) {
			final byte[] data = source.get( current );
			this.put( current, data );
		}
	}
	
	@Override
	public final String[] list() {
		return this.contents;
	}
}
