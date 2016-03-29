package ua.nure.serdiuk;

import java.util.ArrayList;

import ua.nure.serdiuk.Data.Block;

public class Data extends ArrayList<Block> {

	private static final long serialVersionUID = 8236678665551329693L;

	static class Block {

		private final long data;

		public Block(long data) {
			this.data = data;
		}

		public long getData() {
			return data;
		}

		@Override
		public String toString() {
			return Long.toHexString(data);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		this.forEach(b -> sb.append(b));
		return sb.toString();
	}
}
