package io.github.darealturtywurty.turtybot.util.math;

public class Either3<L, M, R> {

    public final L left;
    public final M middle;
    public final R right;

    private Either3(final L left, final M middle, final R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    public static Either3<?, ?, ?> left(final Object left) {
        return new Either3<>(left, null, null);
    }

    public static Either3<?, ?, ?> middle(final Object middle) {
        return new Either3<>(null, middle, null);
    }

    public static Either3<?, ?, ?> right(final Object right) {
        return new Either3<>(null, null, right);
    }

    public Object getNonnull() {
        return this.left == null ? this.middle == null ? this.right : this.middle : this.left;
    }
}
