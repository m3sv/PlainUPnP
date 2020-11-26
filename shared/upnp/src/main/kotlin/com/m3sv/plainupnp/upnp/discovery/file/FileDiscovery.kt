package com.m3sv.plainupnp.upnp.discovery.file

import java.util.*


class FileTree(private val extractor: FileHierarchyExtractor) {

    private val roots: MutableMap<String, FolderRoot> = TreeMap()

    val fileFolderRoots: Collection<FolderRoot> = roots.values

    private val pathCache = mutableSetOf<String>()

    /**
     * We assume that first inserted path defines root
     */
    fun insertPath(path: String) {
        if (pathCache.contains(path))
            return

        val splitPath = path.split("/")
        extractor.extract(roots, LinkedList(splitPath))
        pathCache.add(path)
    }
}
