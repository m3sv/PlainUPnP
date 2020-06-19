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
        val vettedPath = path.substring(0, path.indexOfLast { it == '/' })

        if (pathCache.contains(vettedPath))
            return

        val splitPath = vettedPath.split("/")
        extractor.extract(roots, LinkedList(splitPath))
        pathCache.add(vettedPath)
    }
}
