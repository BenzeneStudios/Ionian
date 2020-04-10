package io.github.ionianmc.loader;

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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.ionianmc.loader.api.IonianModSetup;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.launch.common.FabricLauncherBase;

public class IonianLoader {
	IonianLoader() {
		this.modsDir = ((net.fabricmc.loader.FabricLoader) FabricLoader.getInstance()).getModsDirectory();
		System.out.println(this.modsDir);
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
					tryAddMods(name);
				}
			}
		});

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			LOGGER.info("Ionian has detected this is a development environment. Searching for Workspace Mods!");
			this.boostrapDev();
		}
	}

	private void boostrapDev() {
		// common output folder names are "target," "out," "bin", however we account for others
		File workspaceDir = FabricLoader.getInstance().getGameDirectory().getParentFile().getParentFile();

		for (File dir : workspaceDir.listFiles((file, name) -> new File(name).isDirectory() && !name.contains("src") && !name.equals("run") && name.charAt(0) != '.')) {
			searchForClassFiles(dir, name -> {
				try {
					tryAddMods(name);
				} catch (RuntimeException e) {
					throw new RuntimeException("Failed to add workspace mods from file: " + name + " (dir: " + dir.getName() + ")!", e);
				}
			}, "");
		}
	}

	private static void searchForClassFiles(File dir, Consumer<String> callback, String prefix) {
		for (File sub : dir.listFiles(file -> file.isDirectory() || file.getName().endsWith(".class"))) {
			if (sub.isDirectory()) {
				searchForClassFiles(sub, callback, prefix + sub.getName() + ".");
			} else {
				callback.accept(prefix + sub.getName());
			}
		}
	}

	private void tryAddMods(String className) {
		className = className.substring(0, className.length() - 6);

		try {
			Class<?> declaredClass = Class.forName(className, false, this.loader);

			Method[] methods = declaredClass.getDeclaredMethods();

			if (methods.length == 0) {
				System.out.println(className);
			}
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

	// load mods
	void loadMods(Function<String, IonianModSetup> setup) {
		this.modMethods.forEach(entry -> {
			try {
				Class<?> loadedClass = Class.forName(entry.getLeft());
				String modId = modId(entry.getLeft());
				loadedClass.getDeclaredMethod(entry.getRight(), IonianModSetup.class).invoke(null, setup.apply(modId));
			} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void addMethod(String clazz, String method) {
		LOGGER.info("Discovered Mod: " + clazz + "#" + method);
		this.modMethods.add(Pair.of(clazz, method));
	}

	private static String modId(String className) {
		String[] parts = className.split("\\.");
		StringBuilder sb = new StringBuilder();

		// create mod_id_like_this from something like my.package.mod.ModInit
		for (char c : parts[parts.length - 1].toCharArray()) {
			if (Character.isUpperCase(c)) {
				if (!sb.toString().isEmpty()) {
					sb.append("_");
				}

				sb.append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}

		String pseudoResult = sb.toString();

		// now remove the words "init" or "main" or similar if they have that in the name
		sb = new StringBuilder();
		boolean flag = false;

		for (String sub : pseudoResult.split("_")) {
			if (!sub.contains("init") && !sub.contains("main")) {
				if (flag) {
					sb.append("_");
				}
				sb.append(sub);

				flag = true;
			}
		}

		String result = sb.toString();

		if (!result.isEmpty()) {
			return result;
		} else if (!pseudoResult.isEmpty()) {
			return pseudoResult;
		} else {
			throw new RuntimeException("IonianMC could not construct a valid mod id from class name: " + className + "!");
		}
	}

	public static IonianLoader getInstance() {
		return instance;
	}

	private static IonianLoader instance;
	public static final Logger LOGGER = LogManager.getLogger("Ionian Loader");
}
