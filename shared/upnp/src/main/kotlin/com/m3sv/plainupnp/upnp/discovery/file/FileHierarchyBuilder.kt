package com.m3sv.plainupnp.upnp.discovery.file

import android.content.ContentResolver
import android.net.Uri
import com.m3sv.plainupnp.upnp.mediacontainers.BaseContainer

typealias ContainerBuilder = (
    parentId: String?,
    containerName: String,
    path: String
) -> BaseContainer

class FileHierarchyBuilder {
    private val fileTree = FileTree(FileHierarchyExtractor())

    fun populate(
        contentResolver: ContentResolver,
        parentContainer: BaseContainer,
        column: String,
        uri: Uri,
        containerBuilder: ContainerBuilder,
    ) {
        contentResolver.query(
            uri,
            arrayOf(column),
            null,
            null,
            null
        )?.use { cursor ->
            val pathColumn = cursor.getColumnIndexOrThrow(column)

            while (cursor.moveToNext()) {
                var path = cursor.getString(pathColumn)

                if (path.startsWith("/")) {
                    path = path.drop(1)
                }

                if (path.endsWith("/")) {
                    path = path.dropLast(1)
                }

                fileTree.insertPath(path)
            }
        }

        fileTree.fileFolderRoots.forEach { folderRoot ->
            traverseTree(
                parentContainer = parentContainer,
                folderRoot = folderRoot,
                containerBuilder = containerBuilder
            )
        }
    }

    private fun traverseTree(
        parentContainer: BaseContainer,
        folderRoot: FolderContainer,
        containerBuilder: ContainerBuilder,
    ) {
        val newContainer = containerBuilder(
            null,
            folderRoot.name,
            folderRoot.path
        )

        parentContainer.addContainer(newContainer)

        folderRoot.children.forEach {
            when (val value = it.value) {
                is FolderContainer -> traverseTree(newContainer, value, containerBuilder)

                is FolderLeaf -> {
                    val leafContainer =
                        containerBuilder(
                            newContainer.id.split("$").last(),
                            it.key,
                            value.path
                        )

                    newContainer.addContainer(leafContainer)
                }
            }
        }
    }
}
