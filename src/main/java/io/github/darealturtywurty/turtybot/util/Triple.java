package io.github.darealturtywurty.turtybot.util;

public class Triple<L, M, R> {

	public final L left;
	public final M middle;
	public final R right;

	public Triple(L left, M middle, R right) {
		this.left = left;
		this.middle = middle;
		this.right = right;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Triple)) {
			return false;
		}
		Triple<?, ?, ?> triple = (Triple<?, ?, ?>) obj;
		return left.equals(triple.left) && middle.equals(triple.middle) && right.equals(triple.right);
	}

	private static boolean equals(Object obj1, Object obj2) {
		return (obj1 == null && obj2 == null) || (obj1 != null && obj1.equals(obj2));
	}

	@Override
	public int hashCode() {
		return (left == null ? 0 : left.hashCode()) ^ (middle == null ? 0 : middle.hashCode())
				^ (right == null ? 0 : right.hashCode());
	}

	public static <L, M, R> Triple<L, M, R> create(L left, M middle, R right) {
		return new Triple<>(left, middle, right);
	}
}