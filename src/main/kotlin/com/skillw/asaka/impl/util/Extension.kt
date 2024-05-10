package com.skillw.asaka.impl.util

import java.io.File
import java.net.JarURLConnection
import java.net.URLDecoder
import java.util.jar.JarFile

val Class<*>.instance: Any?
    get() = runCatching { getField("INSTANCE").get(null) }.getOrNull()

fun getClasses(clazz: Class<*>): List<Class<*>> {
    val classes = ArrayList<Class<*>>()
    val url = clazz.protectionDomain.codeSource.location
    runCatching {
        JarFile(
            runCatching { File(url.toURI()) }
                .getOrElse { File(url.path) }
        ).stream().filter { entry ->
            entry.name.endsWith(".class")
        }.forEach { entry ->
            val className =
                entry.name.replace('/', '.').substring(0, entry.name.length - 6)
            runCatching { classes.add(Class.forName(className, false, clazz.classLoader)) }
        }
    }
    return classes
}


fun getClasses(pack: String): Set<Class<*>> {
    val classes = LinkedHashSet<Class<*>>()
    val recursive = true
    var packageName = pack
    val packageDirName = packageName.replace('.', '/')
    runCatching {
        val dirs = Thread.currentThread().contextClassLoader.getResources(packageDirName)
        while (dirs.hasMoreElements()) {
            val url = dirs.nextElement()
            val protocol = url.protocol
            if ("file" == protocol) {
                val filePath = URLDecoder.decode(url.file, "UTF-8")
                findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes)
            } else if ("jar" == protocol) {
                runCatching {
                    val entries = (url.openConnection() as JarURLConnection).jarFile.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        var name = entry.name
                        if (name[0] == '/') {
                            name = name.substring(1)
                        }
                        if (name.startsWith(packageDirName)) {
                            val idx = name.lastIndexOf('/')
                            if (idx != -1) {
                                packageName = name.substring(0, idx).replace('/', '.')
                            }
                            if (idx != -1 || recursive) {
                                if (name.endsWith(".class") && !entry.isDirectory) {
                                    val className = name.substring(packageName.length + 1, name.length - 6)
                                    runCatching { classes.add(Class.forName("$packageName.$className")) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    return classes
}


fun findAndAddClassesInPackageByFile(
    packageName: String,
    packagePath: String,
    recursive: Boolean,
    classes: MutableSet<Class<*>>,
) {
    val dir = File(packagePath)
    if (!dir.exists() || !dir.isDirectory) return
    val dirFiles = dir.listFiles { file ->
        recursive && file.isDirectory || file.name.endsWith(".class")
    } ?: return
    for (file in dirFiles) {
        if (file.isDirectory) {
            findAndAddClassesInPackageByFile(
                packageName + "." + file.name, file.absolutePath, recursive, classes
            )
            continue
        }
        val className = file.name.substring(0, file.name.length - 6)
        runCatching {
            classes.add(
                Thread.currentThread().contextClassLoader.loadClass("$packageName.$className")
            )
        }
    }
}