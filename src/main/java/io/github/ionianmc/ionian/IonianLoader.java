package io.github.ionianmc.ionian;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.tuple.Pair;

import io.github.ionianmc.ionian.api.IonianModSetup;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.launch.common.FabricLauncherBase;

public class IonianLoader {
	IonianLoader() {
		this.modsDir = new File(FabricLoader.getInstance().getGameDirectory().getPath() + "/mods");
		this.loader = FabricLauncherBase.getLauncher().getTargetClassLoader();
		this.modMethods = new ArrayList<>();
		instance = this;
	}

	private final File modsDir;
	private ClassLoader loader;
	private final List<Pair<String, String>> modMethods;

	void discoverMods() {
		List<ZipFile> jars = new ArrayList<>();
		List<URL> yes = new ArrayList<>(); // yes

		for (File file : this.modsDir.listFiles()) {
			if (file.exists() && !file.isDirectory()) {
				if (file.getName().endsWith(".jar")) { // if jar
					try {
						jars.add(new ZipFile(file));
						yes.add(file.toURI().toURL());
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			}
		}

		// add jars that may not have been loaded by fabric
		this.loader = new URLClassLoader(yes.toArray(new URL[yes.size()]), this.loader);

		// go through jars and discover mods
		jars.forEach(f -> {
			Enumeration<? extends ZipEntry> entries = f.entries();

			while(entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();

				if (name.endsWith(".class")) { // if it's a class file
					name = name.replace('/', '.');
					name = name.substring(0, name.length() - 6);

					try {
						Class<?> declaredClass = Class.forName(name, false, this.loader);

						Method[] methods = declaredClass.getDeclaredMethods();

						for (Method m : methods) {
							m.setAccessible(true);

							if (Modifier.isStatic(m.getModifiers())) {
								Class<?>[] parameters = m.getParameterTypes();

								if (parameters.length == 1) {
									if (parameters[0].isAssignableFrom(IonianModSetup.class)) {
										this.addMethod(declaredClass.getName(), m.getName());
									}
								}
							}
						}
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
	}

	// load mods
	void loadMods(Function<String, IonianModSetup> setup) {
		this.modMethods.forEach(entry -> {
			try {
				Class<?> loadedClass = Class.forName(entry.getLeft());
				loadedClass.getDeclaredMethod(entry.getRight(), IonianModSetup.class).invoke(null, setup.apply("minecraft"));
			} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public void addMethod(String clazz, String method) {
		this.modMethods.add(Pair.of(clazz, method));
	}

	public static IonianLoader getInstance() {
		return instance;
	}

	private static IonianLoader instance;
}
