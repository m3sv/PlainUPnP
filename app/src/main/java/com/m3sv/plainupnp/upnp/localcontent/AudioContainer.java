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

package com.m3sv.plainupnp.upnp.localcontent;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import org.droidupnp.legacy.mediaserver.ContentDirectoryService;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.seamless.util.MimeType;

import java.util.List;

public class AudioContainer extends DynamicContainer {
    private static final String TAG = "AudioContainer";

    public AudioContainer(String id, String parentID, String title, String creator, String baseURL, Context ctx,
                          String artist, String albumId) {
        super(id, parentID, title, creator, baseURL, ctx, null);
        setUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);

        if (artist != null) {
            setWhere(MediaStore.Audio.Media.ARTIST + "=?");
            setWhereVal(new String[]{artist});
            setOrderBy(MediaStore.Audio.Media.ALBUM);
        }

        if (albumId != null) {
            setWhere(MediaStore.Audio.Media.ALBUM_ID + "=?");
            setWhereVal(new String[]{albumId});
            setOrderBy(MediaStore.Audio.Media.TRACK);
        }
    }

    @Override
    public Integer getChildCount() {
        String[] columns = {MediaStore.Audio.Media._ID};
        Cursor cursor = getCtx().getContentResolver().query(getUri(), columns, getWhere(), getWhereVal(), getOrderBy());
        if (cursor == null)
            return 0;

        int result = cursor.getCount();
        cursor.close();
        return result;
    }

    @Override
    public List<Container> getContainers() {
        String[] columns = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM
        };

        Cursor cursor = getCtx().getContentResolver().query(getUri(), columns, getWhere(), getWhereVal(), getOrderBy());
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String id = ContentDirectoryService.AUDIO_PREFIX + cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String creator = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));

                    String extension = "";
                    int dot = filePath.lastIndexOf('.');
                    if (dot >= 0)
                        extension = filePath.substring(dot).toLowerCase();

                    Res res = new Res(new MimeType(mimeType.substring(0, mimeType.indexOf('/')),
                            mimeType.substring(mimeType.indexOf('/') + 1)), size, "http://" + getBaseURL() + "/" + id + extension);

                    res.setDuration(duration / (1000 * 60 * 60) + ":"
                            + (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
                            + (duration % (1000 * 60)) / 1000);

                    addItem(new MusicTrack(id, parentID, title, creator, album, new PersonWithRole(creator, "Performer"), res));

                    Log.v(TAG, "Added audio item " + title + " from " + filePath);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return containers;
    }

}
