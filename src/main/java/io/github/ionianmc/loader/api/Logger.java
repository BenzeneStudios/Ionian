package io.github.ionianmc.loader.api;

public interface Logger<T extends Logger<T>> {
	T logInfo(String msg);
	T logWarn(String msg);
	T logError(String msg);
}
