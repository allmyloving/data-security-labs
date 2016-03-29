package ua.nure.serdiuk;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ua.nure.serdiuk.Data.Block;
import ua.nure.serdiuk.util.Util;

public class DES {

	public static final int[] SUBKEYS_SHIFT_TABLE = { 1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1 };

	public static final int[] ROUND_KEY_SHIFT_TABLE = { 14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4, 26,
			8, 16, 7, 27, 20, 13, 2, 41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48, 44, 49, 39, 56, 34, 53, 46, 42, 50,
			36, 29, 32 };

	private static final int[] ROUND_PERMUTATION_TABLE = { 16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10,
			2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25 };

	private static final int[] KEY_SHRINK_TABLE = { 57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51,
			43, 35, 27, 19, 11, 3, 60, 52, 44, 36, 63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53,
			45, 37, 29, 21, 13, 5, 28, 20, 12, 4 };

	private static final int[] BIT_SELECTION_TABLE = { 32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, 13, 12,
			13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25, 24, 25, 26, 27, 28, 29, 28, 29, 30, 31,
			32, 1 };

	private static final int[] INIT_PERMUTATION_TABLE;

	private static final int[] FINAL_PERMUTATION_TABLE;

	private static final SBox S_BOX;

	static {
		INIT_PERMUTATION_TABLE = Util.fillInitial();
		FINAL_PERMUTATION_TABLE = Util.fillFinal();
		S_BOX = Util.getSBox();
	}

	private static long permute(long input, int[] table, int bits) {
		long result = 0;
		for (int i = 0; i < table.length; i++) {
			long bit = Util.getBit(input, (bits - 1) - (table[i] - 1));
			result = (result << 1) | bit;
		}

		return result;
	}

	private static List<Key> generateRoundKeysEncrypt(long key) {
		List<Key> keys = new ArrayList<>();
		long key56 = permute(key, KEY_SHRINK_TABLE, 64);
		long c0 = key56 >>> 28;
		long d0 = key56 & 0xFFF_FFFF;

		long roundKey;
		for (int i = 0; i < 16; i++) {
			c0 = Util.cycleShiftLeft(c0, SUBKEYS_SHIFT_TABLE[i]);
			d0 = Util.cycleShiftLeft(d0, SUBKEYS_SHIFT_TABLE[i]);

			roundKey = permute(join(c0, d0), ROUND_KEY_SHIFT_TABLE, 56);

			keys.add(new Key(i, roundKey));
		}

		return keys;
	}

	private static List<Key> generateRoundKeysDecrypt(long key) {
		List<Key> keys = generateRoundKeysEncrypt(key);
		Collections.sort(keys, new Comparator<Key>() {
			@Override
			public int compare(Key o1, Key o2) {
				return o2.getNum() - o1.getNum();
			}
		});

		return keys;
	}

	private static long join(long a, long b) {
		return ((a << 28) | b);
	}

	private static Data divide(String input) throws UnsupportedEncodingException {
		Data blocks = new Data();
		byte[] bytes = input.getBytes("utf-8");

		for (int i = 0; i < bytes.length; i += 8) {
			int to = (i + 8 > bytes.length) ? bytes.length : i + 8;
			List<Byte> sub = Util.toByteList(bytes).subList(i, to);
			if (sub.size() != 8) {
				while (sub.size() != 8) {
					sub.add((byte) 0);
				}
			}

			blocks.add(new Block(concat(sub)));
		}
		return blocks;
	}

	public static Data encrypt(String input, long key) throws UnsupportedEncodingException {
		Data data = divide(input);
		System.out.println(String.format("Message: %s", data.toString()));

		return encrypt(data, generateRoundKeysEncrypt(key));
	}

	public static String decrypt(Data input, long key) {
		List<Key> roundKeys = generateRoundKeysDecrypt(key);

		return encrypt(input, roundKeys).toString();
	}

	private static Data encrypt(Data input, List<Key> roundKeys) {
		Data encryptedBlocks = new Data();

		for (Block block : input) {
			Block b = encryptBlock(block.getData(), roundKeys);
			encryptedBlocks.add(b);
		}

		return encryptedBlocks;
	}

	private static Block encryptBlock(long block, List<Key> roundKeys) {
		long permutedBlock = permute(block, INIT_PERMUTATION_TABLE, 64);

		long left = (permutedBlock >>> 32);
		long right = permutedBlock << 32 >>> 32;

		for (int i = 0; i < 16; i++) {
			long temp = right;
			right = left ^ f(right, roundKeys.get(i).value);
			left = temp;
		}
		long result = left | (right << 32);
		return new Block(permute(result, FINAL_PERMUTATION_TABLE, 64));
	}

	private static long f(long right, long key) {
		long expanded = permute(right, BIT_SELECTION_TABLE, 32);
		expanded = expanded ^ key;
		long result = 0;

		for (int i = 8; i > 0; i--) {
			long temp = (expanded >> 6 * (i - 1)) & Util.BIT6_MASK;

			temp = S_BOX.process(8 - i, temp);
			result = (result << 4) | temp;
		}
		result = permute(result, ROUND_PERMUTATION_TABLE, 32);
		return result;
	}

	private static void print(String name, long l) {
		System.out.println(String.format("%s: %s", name, Long.toBinaryString(l)));
	}

	private static void printBlocks(String name, long l) {
		StringBuilder sb = new StringBuilder(Long.toBinaryString(l)).reverse();
		sb = new StringBuilder(sb.toString().replaceAll("(.{8})", "$1_"));
		System.out.println(String.format("%s: %s", name, sb.reverse().toString()));
	}

	private static long concat(List<Byte> bytes) {
		long result = 0;
		for (byte b : bytes) {
			result = (result << 8) | (b & 0xFF_FFFF);
		}

		return result;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		long data = 0x0123456789ABCDEFL;
		long key = 0x133457799BBCDFF1L;
		// long key = 0xFEFE_FEFE_FEFE_FEFEL;
		// long key = 0xe0e0e0e0e0e0e0e0L;

		Data enc = encrypt("asdfsdkjghsjkdhgjksdhg", key);
		System.out.println(String.format("Encrypted: %s", enc));
		System.out.println(String.format("Decrypted: %s", decrypt(enc, key)));

	}

	private static class Key {
		private final long value;

		private final int num;

		public Key(int num, long value) {
			this.value = value;
			this.num = num;
		}

		public int getNum() {
			return num;
		}
	}
}
