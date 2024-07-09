package ru.whitebeef.beeflibrary.annotations;


import com.rylinaux.plugman.api.PlugManAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.plugin.BeefPlugin;
import ru.whitebeef.beeflibrary.utils.LoggerUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AnnotationPreprocessor implements Listener {

    private static AnnotationPreprocessor instance;

    public AnnotationPreprocessor() {
        instance = this;
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).forEach(this::scanPlugin);
    }

    public static AnnotationPreprocessor getInstance() {
        return instance;
    }

    public void scanPlugin(Plugin plugin) {
        if (!(plugin instanceof BeefPlugin)) {
            return;
        }
        LoggerUtils.debug(BeefLibrary.getInstance(), "Starting annotating plugin " + plugin.getName());
        if (plugin.getResource("config.yml") == null) {
            return;
        }
        FileConfiguration config = plugin.getConfig();
        getAnnotatedElements(plugin.getClass().getPackageName(), plugin.getClass().getClassLoader(), Set.of(ConfigProperty.class)).stream().map(Field.class::cast)
                .forEach(field -> {
                    try {
                        if (Bukkit.getPluginManager().isPluginEnabled("PlugmanX")) {
                            PlugManAPI.iDoNotWantToBeUnOrReloaded(plugin.getName());
                        }
                        field.setAccessible(true);
                        String path = null;
                        if (field.isAnnotationPresent(ConfigProperty.class)) {
                            path = field.getAnnotation(ConfigProperty.class).value();
                        }
                        Object configValue = null;
                        if (path != null && config.isSet(path)) {
                            configValue = config.get(path);
                        }
                        if (Modifier.isStatic(field.getModifiers())) {
                            if (configValue != null) {
                                field.set(null, configValue);
                            }
                        } else {
                            Class<?> clazz = field.getDeclaringClass();
                            Object obj = Arrays.stream(clazz.getDeclaredMethods())
                                    .filter(method -> method.getName().equals("getInstance")).findAny().orElseThrow(() -> new RuntimeException("Don't found .getInstance() method"))
                                    .invoke(clazz);
                            if (obj == null) {
                                throw new RuntimeException("On try annotating value " + field.getName() + " in " + field.getDeclaringClass().getName() + " getInstance() returns null object. Maybe you need set instance in onLoad()");
                            }
                            if (configValue != null) {
                                field.set(obj, configValue);
                            }

                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }

                });
        LoggerUtils.debug(BeefLibrary.getInstance(), "[AnnotationPreprocessor.scanPlugin()#267] End scan.");
    }

    public static void findClassesByPackage(String pkgName, ClassLoader loader,
                                            boolean includeSubPackages, Set<Class<?>> result)
            throws IOException, ClassNotFoundException {
        String path = pkgName.replace('.', '/');
        String pathWithPrefix = path + '/';
        Enumeration<URL> urls = loader.getResources(path);
        StringBuilder qualifiedNameBuilder = new StringBuilder(pkgName);
        qualifiedNameBuilder.append('.');
        int qualifiedNamePrefixLength = qualifiedNameBuilder.length();

        while (urls.hasMoreElements()) {
            URL pkgUrl = urls.nextElement();
            String protocol = pkgUrl.getProtocol().toLowerCase();
            if ("jar".equals(protocol)) {
                JarURLConnection connection = (JarURLConnection) pkgUrl
                        .openConnection();
                JarFile jarFile = connection.getJarFile();
                Enumeration<JarEntry> jarEntryEnumeration = jarFile
                        .entries();
                while (jarEntryEnumeration.hasMoreElements()) {
                    JarEntry jarEntry = jarEntryEnumeration.nextElement();
                    String absoluteFileName = jarEntry.getName();
                    if (absoluteFileName.endsWith(".class")) {
                        if (absoluteFileName.startsWith("WEB-INF/classes/")) {
                            absoluteFileName = absoluteFileName
                                    .substring(16);
                        }

                        boolean accept = true;
                        if (absoluteFileName.startsWith(pathWithPrefix)) {
                            String qualifiedName = absoluteFileName
                                    .replace('/', '.');
                            if (!includeSubPackages) {
                                int index = absoluteFileName.indexOf('/',
                                        qualifiedNamePrefixLength + 1);
                                if (index != -1) {
                                    accept = false;
                                }
                            }

                            if (accept) {
                                String className = fixClassName(qualifiedName);
                                if (className != null) {
                                    try {
                                        Class<?> clazz = Class.forName(className);
                                        if (!clazz.isInterface() && !clazz.isAnnotation()) {
                                            result.add(clazz);
                                        }
                                    } catch (Exception e) {
                                        LoggerUtils.debug(BeefLibrary.getInstance(), e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException(
                        "Unsupported protocol : " + protocol);
            }
        }
    }

    private static String fixClassName(String fileName) {
        if (!fileName.endsWith(".class")) {
            return null;
        }
        return fileName.substring(0,
                fileName.length() - 6);
    }

    private Collection<AnnotatedElement> getAnnotatedElements(String packageName, ClassLoader classLoader, Set<Class<? extends Annotation>> annotations) {
        try {
            Set<Class<?>> allClasses = new HashSet<>();
            findClassesByPackage(packageName, classLoader, true, allClasses);

            Set<AnnotatedElement> foundElements = new HashSet<>();

            for (Class<? extends Annotation> annotation : annotations) {
                for (Class<?> clazz : allClasses) {
                    if (clazz.isAnnotationPresent(annotation)) {
                        foundElements.add(clazz);
                    }
                    for (Field field : clazz.getDeclaredFields()) {
                        if (field.isAnnotationPresent(annotation)) {
                            foundElements.add(field);
                        }
                    }
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(annotation)) {
                            foundElements.add(method);
                        }
                    }
                    for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                        if (constructor.isAnnotationPresent(annotation)) {
                            foundElements.add(constructor);
                        }
                    }
                }
            }
            return foundElements;
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }
}