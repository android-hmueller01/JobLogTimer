/**
 * $Id: FileUtils.java 178 2016-12-09 10:41:22Z hmueller $
 * Taken from http://stackoverflow.com/questions/6540906/simple-export-and-import-of-a-sqlite-database-on-android
 */
package de.euhm.jlt.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Helper class to copy files.
 * 
 * @author Austyn Mahoney
 */
public class FileUtils {
	/**
	 * Creates the specified <code>toFile</code> as a byte for byte copy of the
	 * <code>fromFile</code>. If <code>toFile</code> already exists, then it
	 * will be replaced with a copy of <code>fromFile</code>. The name and path
	 * of <code>toFile</code> will be that of <code>toFile</code>.<br/>
	 * <br/>
	 * <i> Note: <code>fromFile</code> and <code>toFile</code> will be closed by
	 * this function.</i>
	 * 
	 * @param fromFile
	 *            - FileInputStream for the file to copy from.
	 * @param toFile
	 *            - FileInputStream for the file to copy to.
	 */
	public static void copyFile(FileInputStream fromFile,
			FileOutputStream toFile) throws IOException {
		FileChannel fromChannel = null;
		FileChannel toChannel = null;
		try {
			fromChannel = fromFile.getChannel();
			toChannel = toFile.getChannel();
			fromChannel.transferTo(0, fromChannel.size(), toChannel);
		} finally {
			try {
				if (fromChannel != null) {
					fromChannel.close();
				}
			} finally {
				if (toChannel != null) {
					toChannel.close();
				}
			}
		}
	}
}