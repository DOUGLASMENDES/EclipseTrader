package org.otfeed.support;

import java.nio.ByteBuffer;



/**
 * Converts ByteBuffer type to/from a string.
 * <p/>
 * Formats buffer into multi-line string, each line
 * showing up to 16 bytes of the data in a standard "hexdump"
 * format. For example,
 * <pre>
 * 00000001 40360000 00000000 21000001     %....@6......!...
 * 148a6fe0 69                             %..o.i
 * </pre>
 * <p/>
 * When parsing, ignores empty lines, and treat separator 
 * symbol as start of comment. Therefore, following is a valid
 * input to the parse method:
 * <pre>
 * %
 * % buffer snippet
 * %
 * 00000001 40360000 00000000 21000001     %....@6......!...
 * 148a6fe0 69                             %..o.i
 * </pre>
 * <p/>
 */
public class BufferFormat implements IFormat<ByteBuffer> {
	
	private final char DEFAULT_SEPARATOR = '%';
	
	private final IBufferAllocator allocator;
	
	/**
	 * Creates format using the provided allocator service
	 * (used for {@link #parse(String) parsing} only.
	 * 
	 * @param allocator allocator.
	 */
	public BufferFormat(IBufferAllocator allocator) {
		this.allocator = allocator;
	}
	
	/**
	 * Creates format using the default 
	 * {@link ByteReverseBufferAllocator byte-reversed allocation service}.
	 */
	public BufferFormat() {
		this(new ByteReverseBufferAllocator());
	}
	
	private char separator = DEFAULT_SEPARATOR;
	
	/**
	 * Symbol that marks the beginnig of comment.
	 * 
	 * @return separator character.
	 */
	public char getSeparator() {
		return separator;
	}
	
	/**
	 * Sets separator character.
	 * 
	 * @param val separator character.
	 */
	public void setSeparator(char val) {
		separator = val;
	}
	
	private static final String PRINTABLES 
		= "~`!@#$%^&*()_+-=[]{}\\|,.<>/?;:'\"";

	private static char translateToPrintable(int val) {
		if((val >= 'a' && val <= 'z') 
				|| (val >= 'A' && val <= 'Z')
				|| Character.isDigit(val)
				|| PRINTABLES.indexOf((char) val) >= 0) {
			return (char) val;
		} else {
			return '.';
		}
	}
	
	private static final String HEX = "0123456789abcdef";

	public String format(ByteBuffer buffer) {
		StringBuilder builder = new StringBuilder();
		StringBuilder verbose = new StringBuilder();
		
		int length = buffer.limit() - buffer.position();
		for(int i = 0; i < length; i++) {
			int val = buffer.get(buffer.position() + i);
			if(val < 0) val += 256; // unsigned!
			
			verbose.append(translateToPrintable(val));
			
			builder.append(HEX.charAt(val >> 4));
			builder.append(HEX.charAt(val & 0xf));
			if((i % 16) == 15) {
				builder.append("\t");
				builder.append(separator);
				builder.append(verbose);
				builder.append("\n");
				verbose.setLength(0);
			} else if((i % 4) == 3) {
				builder.append(" ");
			}
		}
		
		int padding = length % 16;
		if(padding > 0) {
			while(padding < 16) {
				
				builder.append("  ");
				
				if((padding % 16) == 15) {
					builder.append("\t");
					builder.append(separator);
					builder.append(verbose);
					builder.append("\n");
				} else if((padding % 4) == 3) {
					builder.append(" ");
				}
				
				padding++;
			}
		}
		
		return builder.toString();
	}

	public ByteBuffer parse(String val) {
		
		ByteBuffer buffer = allocator.allocate(val.length() / 2);
		String [] lines = val.split("\n");
		for(int i = 0; i < lines.length; i++) {
			String line = lines[i];
			int index = line.indexOf(separator);
			if(index >= 0) line = line.substring(0, index).trim();
			
			line = line.replaceAll("\\s", "").toLowerCase();
			if(line.length() == 0) {
				// ignore comments and empty lines
				continue;
			}
			
			int size = line.length() / 2;
			if(line.length() != size * 2) {
				throw new IllegalArgumentException("can't parse line " + (i + 1)
						+ " of the buffer: odd number of nibbles");
			}
			
			for(int j = 0; j < size; j++) {
				int high = line.charAt(j * 2);
				int low  = line.charAt(j * 2 + 1);
				
				high = HEX.indexOf((char) high);
				low  = HEX.indexOf((char) low);
				
				if(high < 0 || low < 0) {
					throw new IllegalArgumentException("not a hex number: " 
							+ line + ", position=" + (j * 2));
				}
				
				buffer.put((byte)((high << 4) + low));
			}
		}
		
		buffer.flip();
		
		return buffer;
	}

}
