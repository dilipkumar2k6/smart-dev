package androidx;

/**
 * Provide key-value item for Spinner.
 * TODO any other controls else?
 * @author 
 *
 */
public class SpinnerItem<K, V> {
	private K id;
	private Object value;

	public SpinnerItem(K id, Object value) {
		super();
		this.id = id;
		this.value = value;
	}

	public K getId() {
		return id;
	}

	public void setId(K id) {
		this.id = id;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
