package androidx;

public interface Callback {

	public void invoke();
	
	public void invoke(Object... args);
}
