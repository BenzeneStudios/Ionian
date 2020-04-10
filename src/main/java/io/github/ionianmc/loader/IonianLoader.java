package io.github.ionianmc.loader;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
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
import org.spongepowered.asm.mixin.transformer.throwables.IllegalClassLoadError;

import io.github.ionianmc.loader.api.IonianModSetup;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.launch.common.FabricLauncherBase;

public class IonianLoader {
	IonianLoader() {
		this.modsDir = ((net.fabricmc.loader.FabricLoader) FabricLoader.getInstance()).getModsDirectory();
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
					this.tryAddMods(name);
				}
			}
		});

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			LOGGER.info("Ionian has detected this is a development environment. Searching for Workspace Mods!");
			this.boostrapDev();
		}
	}

	private static final int SIZEOF_PACKAGE = "package".length();

	private void boostrapDev() {
		File workspaceDir = FabricLoader.getInstance().getGameDirectory().getParentFile().getParentFile();

		// looking in src for package names is cursed as...
		// but it's better than java.lang.ClassNotFoundException: main.io.github.ionianmc.loader.api.IonianModSetup
		for (File dir : workspaceDir.listFiles((file, name) -> new File(file.getPath() + "/" + name).isDirectory() && name.contains("src"))) {
			searchForFiles(dir, file -> {
				try {
					String source = new String(Files.readAllBytes(file.toPath()));
					// try get package decl
					int packageIndex = source.indexOf("package");

					if (packageIndex == -1) {
						LOGGER.warn("Could not find index of package declaration in a source file!" + file.getPath() + " Perhaps you are using the root package?");
					} else {
						packageIndex += SIZEOF_PACKAGE;
						int endPackageIndex = source.indexOf(';', packageIndex);

						if (endPackageIndex == -1) {
							LOGGER.warn("Could not find end index of package declaration in a source file!" + file.getPath() + "");
						} else {
							String fn = file.getName(); // get file name
							String className = source.substring(packageIndex, endPackageIndex).trim() + "." + fn.substring(0, fn.length() - 5 /* ".java".length() */);
							this.tryAddMods(className);
						}
					}
				} catch (RuntimeException | IOException e) {
					throw new RuntimeException("Failed to add workspace mods from file: " + file.getPath() + " (dir: " + dir.getName() + ")!", e);
				}
			}, ".java");
		}
	}

	private static void searchForFiles(File dir, Consumer<File> callback, String extension) {
		for (File sub : dir.listFiles(file -> file.isDirectory() || file.getName().endsWith(extension))) {
			if (sub.isDirectory()) {
				searchForFiles(sub, callback, extension);
			} else {
				callback.accept(sub);
			}
		}
	}

	private void tryAddMods(String className) {
		try {
			Class<?> declaredClass = Class.forName(className, false, this.loader);

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
		} catch (IllegalClassLoadError e) {
			return; // don't load mixin classes!
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
