package androidx;

/**
 * Callback for any delayed invoking.
 * @author Administrator
 *
 * @param <T>
 */
public interface Callback<T> {

	public void invoke();

	public void invoke(T arg);

	public void invoke(Object... args);

	/**
	 * Adapter for Callback interface.
	 * @author Administrator
	 *
	 * @param <T>
	 */
	public static class CallbackAdapter<T> implements Callback<T> {
		@Override
		public void invoke() {
		}

		@Override
		public void invoke(T arg) {
		}

		@Override
		public void invoke(Object... args) {
		}
	}

}
