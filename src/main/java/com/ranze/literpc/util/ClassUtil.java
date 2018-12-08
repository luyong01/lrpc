package com.ranze.literpc.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

@Slf4j
public class ClassUtil {
    public static Set<Class<?>> getClassesWithAnnotation(String packageName, Class<? extends Annotation> annotationClass) {
        Set<Class<?>> classes = getClasses(packageName, true);
        Set<Class<?>> classesWithAnnotation = new HashSet<>();
        for (Class<?> clz : classes) {
            Annotation annotation = clz.getAnnotation(annotationClass);
            if (annotation != null) {
                classesWithAnnotation.add(clz);
            }
        }
        return classesWithAnnotation;
    }

    public static Set<Class<?>> getClasses(String packageName, boolean recursive) {
        Set<Class<?>> classes = new HashSet<>();
        String packageDir = packageName.replace(".", File.separator);
        try {
            Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDir);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if (protocol.equals("file")) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findClassesByFile(packageName, filePath, recursive, classes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    private static void findClassesByFile(String packageName, String packagePath, boolean recursive, Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles(file -> (recursive && file.isDirectory()) || file.getName().endsWith(".class"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                findClassesByFile(packageName.equals("") ? file.getName() : packageName + "." + file.getName(),
                        file.getAbsolutePath(), recursive, classes);
            } else {
                // 去除class文件末尾的".class"
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    if ("".equals(packageName)) {
                        classes.add(Thread.currentThread().getContextClassLoader().loadClass(className));
                    } else {
                        classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("class '{}' not found", className);
                }
            }
        }
    }
}
