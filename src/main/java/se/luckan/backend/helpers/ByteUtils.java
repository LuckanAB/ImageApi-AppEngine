package se.luckan.backend.helpers;

import java.nio.ByteBuffer;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;

public class ByteUtils {
	public static String longToBase64(final long plain) {
		return Base64.encodeBase64URLSafeString(longToBytesStripped(plain));
	}

	public static byte[] longToBytesStripped(final long l) {
		final ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
		buffer.putLong(0, l);
		final byte[] a = buffer.array();
		int i;
		for(i = 0; i < a.length; i++) {
			if(a[i] != 0) {
				break;
			}
		}
		return Arrays.copyOfRange(a, i, a.length);
	}
}
