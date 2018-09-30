/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * <p>
 * This file is part of DroidUPNP.
 * <p>
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.droidupnp.view;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.m3sv.common.Utils;
import com.m3sv.droidupnp.R;
import com.m3sv.droidupnp.upnp.DIDLObjectDisplay;
import com.m3sv.droidupnp.upnp.IDIDLObject;

import org.droidupnp.controller.upnp.UpnpServiceController;
import org.droidupnp.model.upnp.CallableContentDirectoryFilter;
import org.droidupnp.model.upnp.DeviceDiscoveryObserver;
import org.droidupnp.model.upnp.Factory;
import org.droidupnp.model.upnp.IContentDirectoryCommand;
import org.droidupnp.model.upnp.IRendererCommand;
import org.droidupnp.model.upnp.IUpnpDevice;
import org.droidupnp.model.upnp.didl.DIDLDevice;
import org.droidupnp.model.upnp.didl.IDIDLContainer;
import org.droidupnp.model.upnp.didl.IDIDLItem;
import org.droidupnp.model.upnp.didl.IDIDLParentContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;

import timber.log.Timber;

public class ContentDirectoryFragment extends ListFragment implements Observer {
    private static final String TAG = ContentDirectoryFragment.class.getSimpleName();

    private ArrayAdapter<DIDLObjectDisplay> contentList;
    private LinkedList<String> tree = null;
    private String currentID = null;
    private IUpnpDevice device;

    private IContentDirectoryCommand contentDirectoryCommand;

    private SwipeRefreshLayout swipeContainer;

    static final String STATE_CONTENTDIRECTORY = "contentDirectory";
    static final String STATE_TREE = "tree";
    static final String STATE_CURRENT = "current";

    UpnpServiceController controller;

    Factory factory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        MainActivity.setContentDirectoryFragment(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.browsing_list_fragment, container, false);
    }

    /**
     * This update the search visibility depending on current content directory capabilities
     */
    public void updateSearchVisibility() {
        final Activity a = getActivity();
        if (a != null) {
            a.runOnUiThread(() -> {
                try {
//                        MainActivity.setSearchVisibility(contentDirectoryCommand != null && contentDirectoryCommand.isSearchAvailable());
                } catch (Exception e) {
                    Timber.e(e);
                }
            });
        }
    }

    private DeviceObserver deviceObserver;

    public class DeviceObserver implements DeviceDiscoveryObserver {
        ContentDirectoryFragment cdf;

        public DeviceObserver(ContentDirectoryFragment cdf) {
            this.cdf = cdf;
        }

        @Override
        public void addedDevice(IUpnpDevice device) {
            if (controller.getSelectedContentDirectory() == null)
                cdf.update();
        }

        @Override
        public void removedDevice(IUpnpDevice device) {
            if (controller.getSelectedContentDirectory() == null)
                cdf.update();
        }
    }

    public class CustomAdapter extends ArrayAdapter<DIDLObjectDisplay> {
        private final int layout;
        private LayoutInflater inflater;
        private ArrayList<String> videos = new ArrayList<>();
        private ArrayList<Bitmap> thumbnails = new ArrayList<>();

        public CustomAdapter(Context context) {
            super(context, 0);
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.layout = R.layout.browsing_list_item;
            videos.addAll(Utils.getAllMedia(getContext()));
            for (String video : videos) {
                thumbnails.add(ThumbnailUtils.createVideoThumbnail(video, MediaStore.Video.Thumbnails.MINI_KIND));
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = inflater.inflate(layout, null);

            // Item
            final DIDLObjectDisplay entry = getItem(position);

            ImageView imageView = convertView.findViewById(R.id.icon);
            imageView.setImageResource(entry.getIcon());

            for (int i = 0; i < videos.size(); i++) {
                String video = videos.get(i);
                if (video.substring(video.lastIndexOf("/") + 1, video.lastIndexOf(".")).equals(entry.getTitle()))
                    imageView.setImageBitmap(thumbnails.get(i));
            }

            TextView text1 = convertView.findViewById(R.id.text1);
            text1.setText(entry.getTitle());

            TextView text2 = convertView.findViewById(R.id.text2);
            text2.setText((entry.getDescription() != null) ? entry.getDescription() : "");

            TextView text3 = convertView.findViewById(R.id.text3);
            text3.setText(entry.getCount());

            return convertView;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        contentList = new CustomAdapter(this.getView().getContext());

        setListAdapter(contentList);

        deviceObserver = new DeviceObserver(this);
        controller.getContentDirectoryDiscovery().addObserver(deviceObserver);

        // Listen to content directory change
        controller.addSelectedContentDirectoryObserver(this);

        if (savedInstanceState != null
                && savedInstanceState.getStringArray(STATE_TREE) != null
                && controller.getSelectedContentDirectory() != null
                && 0 == controller.getSelectedContentDirectory().getUID()
                .compareTo(savedInstanceState.getString(STATE_CONTENTDIRECTORY))) {
            Log.i(TAG, "Restore previews state");

            // Content directory is still the same => reload context
            tree = new LinkedList<>(Arrays.asList(savedInstanceState.getStringArray(STATE_TREE)));
            currentID = savedInstanceState.getString(STATE_CURRENT);

            device = controller.getSelectedContentDirectory();
            contentDirectoryCommand = factory.createContentDirectoryCommand();
        }

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
                Log.v(TAG, "On long-click event");

                IDIDLObject didl = contentList.getItem(position).getDIDLObject();

                if (didl instanceof IDIDLItem) {
                    IDIDLItem ididlItem = (IDIDLItem) didl;
                    final Activity a = getActivity();
                    final Intent intent = new Intent(Intent.ACTION_VIEW);

                    Uri uri = Uri.parse(ididlItem.getURI());
                    intent.setDataAndType(uri, didl.getDataType());

                    try {
                        a.startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(), R.string.failed_action, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.no_action_available, Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        Log.d(TAG, "Force refresh");
        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        controller.delSelectedContentDirectoryObserver(this);
        controller.getContentDirectoryDiscovery().removeObserver(deviceObserver);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setBackgroundColor(getResources().getColor(R.color.grey));

        swipeContainer = view.findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    @Override
    public void onDestroyView() {
        swipeContainer.setRefreshing(false);
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "Save instance state");

        if (controller.getSelectedContentDirectory() == null)
            return;

        savedInstanceState.putString(STATE_CONTENTDIRECTORY, controller.getSelectedContentDirectory()
                .getUID());

        if (tree != null) {
            String[] arrayTree = new String[tree.size()];
            int i = 0;
            for (String s : tree)
                arrayTree[i++] = s;

            savedInstanceState.putStringArray(STATE_TREE, arrayTree);
            savedInstanceState.putString(STATE_CURRENT, currentID);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        contentList.clear();
        refresh();
    }

    public Boolean goBack() {
        if (tree == null || tree.isEmpty()) {
            if (controller.getSelectedContentDirectory() != null) {
                // Back on device root, unselect device
                controller.setSelectedContentDirectory(null);
                return false;
            } else {
                // Already at the upper level
                return true;
            }
        } else {
            Log.d(TAG, "Go back in browsing");
            currentID = tree.pop();
            update();
            return false;
        }
    }

    public void printCurrentContentDirectoryInfo() {
        Log.i(TAG, "Device : " + controller.getSelectedContentDirectory().getDisplayString());
        controller.getSelectedContentDirectory().printService();
    }

    public class RefreshCallback implements Callable<Void> {
        public Void call() throws java.lang.Exception {
            Log.d(TAG, "Stop refresh");
            final Activity a = getActivity();
            if (a != null) {
                a.runOnUiThread(() -> {
                    try {
                        swipeContainer.setRefreshing(false);
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                });
            }
            return null;
        }
    }

    public class ContentCallback extends RefreshCallback {
        private ArrayAdapter<DIDLObjectDisplay> contentList;
        private ArrayList<DIDLObjectDisplay> content;

        public ContentCallback(ArrayAdapter<DIDLObjectDisplay> contentList) {
            this.contentList = contentList;
            this.content = new ArrayList<>();
        }

        public void setContent(ArrayList<DIDLObjectDisplay> content) {
            this.content = content;
        }

        public Void call() throws java.lang.Exception {
            final Activity a = getActivity();
            if (a != null) {
                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Empty the list
                            contentList.clear();
                            // Fill the list
                            contentList.addAll(content);
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                    }
                });
            }
            // Refresh
            return super.call();
        }

    }

    public void setEmptyText(CharSequence text) {
        ((TextView) getListView().getEmptyView()).setText(text);
    }

    public synchronized void refresh() {
        Log.d(TAG, "refresh");

        setEmptyText(getString(R.string.loading));

        swipeContainer.setRefreshing(true);

        // Update search visibility
        updateSearchVisibility();

        if (controller.getSelectedContentDirectory() == null) {
            // List here the content directory devices
            setEmptyText(getString(R.string.device_list_empty));

            if (device != null) {
                Log.i(TAG, "Current content directory have been removed");
                device = null;
                tree = null;
            }

            // Fill with the content directory list
            final Collection<IUpnpDevice> upnpDevices = controller.getServiceListener()
                    .getFilteredDeviceList(new CallableContentDirectoryFilter());

            ArrayList<DIDLObjectDisplay> list = new ArrayList<DIDLObjectDisplay>();
            for (IUpnpDevice upnpDevice : upnpDevices)
                list.add(new DIDLObjectDisplay(new DIDLDevice(upnpDevice)));

            try {
                ContentCallback cc = new ContentCallback(contentList);
                cc.setContent(list);
                cc.call();
            } catch (Exception e) {
                Timber.e(e);
            }

            return;
        }

        Log.i(TAG, "device " + device + " device " + ((device != null) ? device.getDisplayString() : ""));
        Log.i(TAG, "contentDirectoryCommand : " + contentDirectoryCommand);

        contentDirectoryCommand = factory.createContentDirectoryCommand();
        if (contentDirectoryCommand == null)
            return; // Can't do anything if upnp not ready

        if (device == null || !device.equals(controller.getSelectedContentDirectory())) {
            device = controller.getSelectedContentDirectory();

            Log.i(TAG, "Content directory changed !!! "
                    + controller.getSelectedContentDirectory().getDisplayString());

            tree = new LinkedList<String>();

            Log.i(TAG, "Browse root of a new device");
//            contentDirectoryCommand.browse("0", null, new ContentCallback(contentList));
        } else {
            if (tree != null && tree.size() > 0) {
                String parentID = (tree.size() > 0) ? tree.getLast() : null;
                Log.i(TAG, "Browse, currentID : " + currentID + ", parentID : " + parentID);
//                contentDirectoryCommand.browse(currentID, parentID, new ContentCallback(contentList));
            } else {
                Log.i(TAG, "Browse root");
//                contentDirectoryCommand.browse("0", null, new ContentCallback(contentList));
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        IDIDLObject didl = contentList.getItem(position).getDIDLObject();

        try {
            if (didl instanceof DIDLDevice) {
                controller.setSelectedContentDirectory(((DIDLDevice) didl).getDevice(), false);

                // Refresh display
                refresh();
            } else if (didl instanceof IDIDLContainer) {
                // Update position
                if (didl instanceof IDIDLParentContainer) {
                    currentID = tree.pop();
                } else {
                    currentID = didl.getId();
                    String parentID = didl.getParentID();
                    tree.push(parentID);
                }

                // Refresh display
                refresh();
            } else if (didl instanceof IDIDLItem) {
                // Launch item
                launchURI((IDIDLItem) didl);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to finish action after item click");
            Timber.e(e);
        }
    }

    private void launchURI(final IDIDLItem uri) {
        if (controller.getSelectedRenderer() == null) {
            // No renderer selected yet, open a popup to select one
            final Activity a = getActivity();
            if (a != null) {
                a.runOnUiThread(() -> {
                    try {
                        RendererDialog rendererDialog = new RendererDialog();
                        rendererDialog.setCallback(() -> {
                            launchURIRenderer(uri);
                            return null;
                        });
                        rendererDialog.show(getActivity().getFragmentManager(), "RendererDialog");
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                });
            }
        } else {
            // Renderer available, go for it
            launchURIRenderer(uri);
        }

    }

    private void launchURIRenderer(IDIDLItem uri) {
        IRendererCommand rendererCommand = factory.createRendererCommand(factory.createRendererState());
        rendererCommand.launchItem(uri);
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.i(TAG, "ContentDirectory have changed");
        update();
    }

    public void update() {
        final Activity a = getActivity();
        if (a != null) {
            a.runOnUiThread(() -> refresh());
        }
    }
}
