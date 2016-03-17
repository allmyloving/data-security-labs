package ua.nure.serdiuk;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

import ua.nure.serdiuk.util.Util;

public class SBox {

	private Map<Integer, Table> boxes;

	public SBox() {
		boxes = new HashMap<>();
	}

	public void add(int key, Table value) {
		boxes.put(key, value);
	}

	public Table get(int key) {
		return boxes.get(key);
	}

	public long process(int boxNum, long num) {
//		System.out.println(Long.toBinaryString(num));
		int row = (int) (((num & 0b100000) >>> 4) | (num & 1));
		int col = (int) ((num >> 1) & 0b1111);
//
//		System.out.println(String.format("boxnum: %d, row: %d, col:%d", boxNum, row, col));

		return boxes.get(boxNum).get(row, col);
	}

}
