package io.github.ionianmc.ionian.api;

public interface Localisation<T extends Localisation<T>> {
	T localisedName(String language, String name);
}
