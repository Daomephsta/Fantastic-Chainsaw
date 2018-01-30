package leviathan143.fantasticchainsaw.base;

import java.util.Observable;

public class ObservableValue<T> extends Observable
{
	private T value;

	public T getValue()
	{
		return value;
	}

	public void setValue(T value)
	{
		if (this.value == value) return;
		this.value = value;
		setChanged();
		notifyObservers();
		clearChanged();
	}
}
