package com.m3sv.plainupnp.upnp.navigator

import com.m3sv.plainupnp.data.upnp.Directory
import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.BrowseToModel
import com.m3sv.plainupnp.upnp.ContentState
import com.m3sv.plainupnp.upnp.UpnpFactory
import com.m3sv.plainupnp.upnp.UpnpServiceController
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import java.util.*
import java.util.concurrent.Future

class DefaultUpnpNavigator(private val factory: UpnpFactory, private val controller: UpnpServiceController) : UpnpNavigator {

    private val contentSubject = PublishSubject.create<ContentState>()

    private val selectedDirectory = PublishSubject.create<Directory>()

    private val browseTo: Subject<BrowseToModel> = PublishSubject.create()

    private var directoriesStructure = Stack<ContentState>()

    private val currentContentDirectory: UpnpDevice?
        get() = controller.selectedContentDirectory

    init {
        browseTo.doOnNext { contentSubject.onNext(ContentState.Loading) }
                .subscribe(::navigate, Timber::e)
    }

    override fun navigateHome() {
        directoriesStructure.clear()
        browseTo.onNext(BrowseToModel("0", currentContentDirectory?.friendlyName ?: "Home", null))
    }

    private var browseFuture: Future<*>? = null

    override fun navigateTo(model: BrowseToModel) {
        browseTo.onNext(model)
    }

    private fun navigate(model: BrowseToModel) {
        Timber.d("Browse: ${model.id}")

        browseFuture?.cancel(true)
        browseFuture = factory.createContentDirectoryCommand()?.browse(model.id, null) {
            val successState = ContentState.Success(model.directoryName, it ?: listOf())
            directoriesStructure.push(successState)
            contentSubject.onNext(successState)

            when (model.id) {
                "0" -> {
                    selectedDirectory.onNext(Directory.Home)
                }
                else -> {
                    val subDirectory = Directory.SubDirectory(model.id)
                    selectedDirectory.onNext(subDirectory)

                    Timber.d("Adding subdirectory: $subDirectory")
                }
            }
        }
    }

    override fun navigatePrevious(): Boolean {
        return if (!directoriesStructure.empty()) {
            contentSubject.onNext(directoriesStructure.pop())
            true
        } else {
            false
        }
    }
}