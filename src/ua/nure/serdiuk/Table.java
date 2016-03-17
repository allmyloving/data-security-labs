package ua.nure.serdiuk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Table {

	private final static List<Pair> pairs;

	static {
		pairs = new ArrayList<>();

		for (int i = 0; i <= 3; i++) {
			for (int j = 0; j < 16; j++) {
				pairs.add(new Pair(i, j));
			}
		}
	}

	private Map<Pair, Integer> table;

	public Table() {
		table = new TreeMap<>();
	}

	public void add(int row, int col, int value) {
		table.put(pairs.get(pairs.indexOf(new Pair(row, col))), value);
	}

	public int get(int row, int col) {
		return table.get(new Pair(row, col));
	}

	private static class Pair implements Comparable<Pair> {
		private int row;

		private int column;

		public Pair(int row, int column) {
			this.row = row;
			this.column = column;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + column;
			result = prime * result + row;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair other = (Pair) obj;
			if (column != other.column)
				return false;
			if (row != other.row)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "[" + row + ", " + column + "]";
		}

		@Override
		public int compareTo(Pair o) {
			int row = this.row - o.row;
			return row != 0 ? row : this.column - o.column;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		table.forEach((k, v) -> sb.append(k).append(" -> ").append(v).append('\t'));

		return sb.toString();
	}

}
