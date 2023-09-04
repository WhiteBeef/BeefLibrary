package ru.whitebeef.beeflibrary.annotations;


import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.utils.LoggerUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AnnotationPreprocessor implements Listener {

    private static AnnotationPreprocessor instance;

    public AnnotationPreprocessor() {
        instance = this;
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).forEach(plugin -> scanPlugin(plugin, LoadType.PRE_ENABLE));
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        scanPlugin(event.getPlugin(), LoadType.AFTER_ENABLE);
    }

    public static AnnotationPreprocessor getInstance() {
        return instance;
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
            String urlString = URLDecoder.decode(pkgUrl.getFile(), StandardCharsets.UTF_8);
            String protocol = pkgUrl.getProtocol().toLowerCase();
            if ("file".equals(protocol)) {
                File pkgDir = new File(urlString);
                if (pkgDir.isDirectory()) {
                    if (includeSubPackages) {
                        findClassNamesRecursive(pkgDir, result,
                                qualifiedNameBuilder,
                                qualifiedNamePrefixLength);
                    } else {
                        for (String fileName : pkgDir.list()) {
                            String simpleClassName = fixClassName(fileName);
                            if (simpleClassName != null) {
                                qualifiedNameBuilder
                                        .setLength(qualifiedNamePrefixLength);
                                qualifiedNameBuilder
                                        .append(simpleClassName);
                                Class<?> clazz = Class
                                        .forName(qualifiedNameBuilder
                                                .toString());
                                if (!clazz.isInterface()
                                        && !clazz.isAnnotation()) {
                                    result.add(clazz);
                                }
                            }
                        }
                    }
                }
            } else if ("jar".equals(protocol)) {
                // somehow the connection has no close method and can NOT be disposed
                JarURLConnection connection = (JarURLConnection) pkgUrl
                        .openConnection();
                JarFile jarFile = connection.getJarFile();
                Enumeration<JarEntry> jarEntryEnumeration = jarFile
                        .entries();
                while (jarEntryEnumeration.hasMoreElements()) {
                    JarEntry jarEntry = jarEntryEnumeration.nextElement();
                    String absoluteFileName = jarEntry.getName();
                    if (absoluteFileName.endsWith(".class")) {
                        if (absoluteFileName.startsWith("/")) {
                            absoluteFileName.substring(1);
                        }
                        // special treatment for WAR files...
                        // "WEB-INF/lib/" entries should be opened directly in contained jar
                        if (absoluteFileName.startsWith("WEB-INF/classes/")) {
                            // "WEB-INF/classes/".length() == 16
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
                                    Class<?> clazz = Class
                                            .forName(className);
                                    if (!clazz.isInterface()
                                            && !clazz.isAnnotation()) {
                                        result.add(clazz);
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

    private static void findClassNamesRecursive(File pkgDir,
                                                Set<Class<?>> result, StringBuilder qualifiedNameBuilder,
                                                int qualifiedNamePrefixLength) throws ClassNotFoundException {
        for (File childFile : pkgDir.listFiles()) {
            String fileName = childFile.getName();
            if (childFile.isDirectory()) {
                qualifiedNameBuilder.setLength(qualifiedNamePrefixLength);
                StringBuilder subBuilder = new StringBuilder(
                        qualifiedNameBuilder);
                subBuilder.append(fileName);
                subBuilder.append('.');
                findClassNamesRecursive(childFile, result, subBuilder,
                        subBuilder.length());
            } else {
                String simpleClassName = fixClassName(fileName);
                if (simpleClassName != null) {
                    qualifiedNameBuilder
                            .setLength(qualifiedNamePrefixLength);
                    qualifiedNameBuilder.append(simpleClassName);
                    Class<?> clazz = Class.forName(qualifiedNameBuilder
                            .toString());
                    if (!clazz.isInterface() && !clazz.isAnnotation()) {
                        result.add(clazz);
                    }
                }
            }
        }
    }

    private static String fixClassName(String fileName) {
        if (fileName.endsWith(".class")) {
            // remove extension (".class".length() == 6)
            String nameWithoutExtension = fileName.substring(0,
                    fileName.length() - 6);
            // handle inner classes...
            return nameWithoutExtension;
        }
        return null;
    }

    public void scanPlugin(Plugin plugin, LoadType currentLoadType) {
        if (!plugin.getDescription().getDepend().contains("BeefLibrary") &&
                !plugin.getDescription().getSoftDepend().contains("BeefLibrary") &&
                !plugin.getName().equals("BeefLibrary")) {
            return;
        }
        LoggerUtils.debug(BeefLibrary.getInstance(), "Starting annotating plugin " + plugin.getName());
        BeefLibrary.loadConfig(plugin);
        FileConfiguration config = plugin.getConfig();
        getAnnotatedElements(plugin.getClass().getPackageName(), plugin.getClass().getClassLoader(), Set.of(ConfigProperty.class, IntProperty.class, BooleanProperty.class, StringProperty.class)).stream().map(annotatedElement -> (Field) annotatedElement)
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        String path = null;
                        LoadType loadType = LoadType.PRE_ENABLE;
                        if (field.isAnnotationPresent(ConfigProperty.class)) {
                            path = field.getAnnotation(ConfigProperty.class).value();
                            loadType = field.getAnnotation(ConfigProperty.class).loadType();
                        }
                        Object defaultValue = null;
                        if (field.isAnnotationPresent(IntProperty.class)) {
                            path = field.getAnnotation(IntProperty.class).value();
                            defaultValue = field.getAnnotation(IntProperty.class);
                            loadType = field.getAnnotation(IntProperty.class).loadType();
                        }
                        if (field.isAnnotationPresent(BooleanProperty.class)) {
                            path = field.getAnnotation(BooleanProperty.class).value();
                            defaultValue = field.getAnnotation(BooleanProperty.class).defaultValue();
                            loadType = field.getAnnotation(BooleanProperty.class).loadType();
                        }
                        if (field.isAnnotationPresent(StringProperty.class)) {
                            path = field.getAnnotation(StringProperty.class).value();
                            defaultValue = field.getAnnotation(StringProperty.class).defaultValue();
                            loadType = field.getAnnotation(StringProperty.class).loadType();
                        }
                        if (field.isAnnotationPresent(StringArrayProperty.class)) {
                            path = field.getAnnotation(StringArrayProperty.class).value();
                            defaultValue = field.getAnnotation(StringArrayProperty.class).defaultValue();
                            loadType = field.getAnnotation(StringArrayProperty.class).loadType();
                        }
                        if (loadType != currentLoadType) {
                            return;
                        }
                        Object configValue = null;
                        if (path != null && config.isSet(path)) {
                            configValue = config.get(path);
                        }
                        if (Modifier.isStatic(field.getModifiers())) {
                            if (configValue != null) {
                                field.set(field, configValue);
                            } else {
                                if (field.get(field) == null) {
                                    field.set(field, defaultValue);
                                }
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
                            } else {
                                if (field.get(obj) == null) {
                                    field.set(obj, defaultValue);
                                }
                            }
                        }

                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });
    }

    private Collection<AnnotatedElement> getAnnotatedElements(String packageName, ClassLoader classLoader, Class<? extends Annotation> annotation) {
        return getAnnotatedElements(packageName, classLoader, Set.of(annotation));
    }

    private Collection<AnnotatedElement> getAnnotatedElements(String packageName, ClassLoader classLoader, Set<Class<? extends Annotation>> annotations) {
        Set<Class<?>> allClasses = new HashSet<>();
        try {
            findClassesByPackage(packageName, classLoader, true, allClasses);
        } catch (Exception exception) {
            return Collections.emptyList();
        }
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
    }
}
