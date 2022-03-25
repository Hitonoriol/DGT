package hitonoriol.ifelse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class If<T> {
	private Map<Predicate<T>, Runnable> conditions = new LinkedHashMap<>(1);
	private Optional<Runnable> elseAction = Optional.empty();

	private If() {
	}

	public If<T> ifThen(Predicate<T> condition, Runnable action) {
		conditions.put(condition, action);
		return this;
	}

	public If<T> orElse(Runnable action) {
		elseAction = Optional.of(action);
		return this;
	}

	public void test(T val) {
		conditions.entrySet().stream()
				.filter(entry -> entry.getKey().test(val))
				.findFirst()
				.ifPresentOrElse(entry -> entry.getValue().run(), elseAction.orElse(() -> {}));
	}

	public static void ifDo(boolean condition, Runnable then) {
		if (condition)
			then.run();
	}

	public static void ifElse(boolean condition, Runnable then, Runnable orElse) {
		if (condition)
			then.run();
		else
			orElse.run();
	}

	public static <T> If<T> create(Consumer<If<T>> initializer) {
		If<T> oif = new If<>();
		initializer.accept(oif);
		return oif;
	}
	
	public static <T> If<T> ifElse(Predicate<T> condition, Runnable then, Runnable orElse) {
		return new If<T>().ifThen(condition, then).orElse(orElse);
	}
}
