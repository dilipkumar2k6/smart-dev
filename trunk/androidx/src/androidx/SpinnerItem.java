package androidx;

/**
 * Provide key-value item for Spinner.
 * TODO any other controls else?
 * @author yuxing
 *
 */
public class SpinnerItem {
	private long id;
	private Object value;

	public SpinnerItem(long id, Object value) {
		super();
		this.id = id;
		this.value = value;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
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
