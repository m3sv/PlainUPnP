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

package org.droidupnp.legacy.cling;

import com.m3sv.plainupnp.upnp.UpnpRendererStateModel;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;


import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;
import timber.log.Timber;

public class UpnpRendererState extends Observable<UpnpRendererStateModel> {

    private UpnpInnerState innerState;

    @Override
    protected void subscribeActual(Observer<? super UpnpRendererStateModel> observer) {
        innerState = new UpnpInnerState(observer);
        observer.onSubscribe(innerState);
    }

    public void setVolume(int volume) {
        if (innerState != null) {
            innerState.setVolume(volume);
        }
    }

    public boolean isMute() {
        if (innerState != null) {
            return innerState.mute;
        }

        return false;
    }

    public void setPositionInfo(PositionInfo positionInfo) {
        if (innerState != null) {
            innerState.setPositionInfo(positionInfo);
        }
    }

    public long getDurationSeconds() {
        if (innerState != null) {
            return innerState.getDurationSeconds();
        }

        return 0L;
    }

    @Nullable
    public com.m3sv.plainupnp.data.upnp.UpnpRendererState.State getState() {
        if (innerState != null) {
            return innerState.getState();
        }

        return null;
    }

    public void setMuted(boolean muted) {
        if (innerState != null) {
            innerState.setMute(muted);
        }
    }

    public void setMediaInfo(MediaInfo mediaInfo) {
        if (innerState != null) {
            innerState.setMediaInfo(mediaInfo);
        }
    }

    public void setTransportInfo(TransportInfo transportInfo) {
        if (transportInfo != null) {
            innerState.setTransportInfo(transportInfo);
        }
    }

    private class UpnpInnerState extends MainThreadDisposable implements com.m3sv.plainupnp.data.upnp.UpnpRendererState {
        private final Observer<? super UpnpRendererStateModel> observer;

        // / Player info
        private State state;
        private int volume;
        private boolean mute;
        private int repeatMode; // TODO enum with different mode
        private int randomMode; // TODO enum with different mode

        // / Track info
        private PositionInfo positionInfo;
        private MediaInfo mediaInfo;
        private TransportInfo transportInfo;

        public UpnpInnerState(Observer<? super UpnpRendererStateModel> observer) {
            super();
            this.observer = observer;
            state = State.STOP;
            volume = -1;
            resetTrackInfo();

            updateState();
        }

        private void updateState() {
            Timber.d("Update renderer state");
            if (!isDisposed())
                observer.onNext(
                        new UpnpRendererStateModel(state,
                                getRemainingDuration(),
                                getPosition(),
                                getElapsedPercent(),
                                getTitle(),
                                getArtist(),
                                volume,
                                mute,
                                positionInfo,
                                mediaInfo,
                                transportInfo));
        }

        @Override
        protected void onDispose() {

        }

        @Override
        public State getState() {
            return state;
        }

        @Override
        public void setState(State state) {
            if (this.state == state)
                return;

            if (state == State.STOP && (this.state == State.PLAY || this.state == State.PAUSE)) {
                // Stop !
                resetTrackInfo();
            }

            this.state = state;
            updateState();
        }

        @Override
        public int getVolume() {
            return volume;
        }

        @Override
        public void setVolume(int volume) {
            if (this.volume == volume)
                return;

            this.volume = volume;
            updateState();
        }

        @Override
        public boolean isMute() {
            return mute;
        }

        @Override
        public void setMute(boolean mute) {
            if (this.mute == mute)
                return;

            this.mute = mute;
            updateState();
        }

        public void setPositionInfo(PositionInfo positionInfo) {
            try {
                if (this.positionInfo.getRelTime().compareTo(positionInfo.getRelTime()) == 0
                        && this.positionInfo.getAbsTime().compareTo(positionInfo.getAbsTime()) == 0)
                    return;

                this.positionInfo = positionInfo;
                updateState();
            } catch (Exception e) {
                Timber.e((e.getMessage() == null) ? "Exception !" : e.getMessage());
                for (StackTraceElement m : e.getStackTrace())
                    Timber.e(m.toString());
            }

        }

        public MediaInfo getMediaInfo() {
            return mediaInfo;
        }

        public void setMediaInfo(MediaInfo mediaInfo) {
            if (this.mediaInfo.hashCode() == mediaInfo.hashCode())
                return;

            this.mediaInfo = mediaInfo;
            // notifyAllObservers();
        }

        public TransportInfo getTransportInfo() {
            return transportInfo;
        }

        public void setTransportInfo(TransportInfo transportInfo) {
            this.transportInfo = transportInfo;

            if (transportInfo.getCurrentTransportState() == TransportState.PAUSED_PLAYBACK
                    || transportInfo.getCurrentTransportState() == TransportState.PAUSED_RECORDING)
                setState(State.PAUSE);
            else if (transportInfo.getCurrentTransportState() == TransportState.PLAYING)
                setState(State.PLAY);
            else
                // if(transportInfo.getCurrentTransportState() == TransportState.STOPPED)
                setState(State.STOP);
        }

        private TrackMetadata getTrackMetadata() {
            return new TrackMetadata(positionInfo.getTrackMetaData());
        }

        private String formatTime(long h, long m, long s) {
            return ((h >= 10) ? "" + h : "0" + h) + ":" + ((m >= 10) ? "" + m : "0" + m) + ":"
                    + ((s >= 10) ? "" + s : "0" + s);
        }

        @Override
        public String getRemainingDuration() {
            long t = positionInfo.getTrackRemainingSeconds();
            long h = t / 3600;
            long m = (t - h * 3600) / 60;
            long s = t - h * 3600 - m * 60;
            return "-" + formatTime(h, m, s);
        }

        @Override
        public String getDuration() {
            long t = positionInfo.getTrackDurationSeconds();
            long h = t / 3600;
            long m = (t - h * 3600) / 60;
            long s = t - h * 3600 - m * 60;
            return formatTime(h, m, s);
        }

        @Override
        public String getPosition() {
            long t = positionInfo.getTrackElapsedSeconds();
            long h = t / 3600;
            long m = (t - h * 3600) / 60;
            long s = t - h * 3600 - m * 60;
            return formatTime(h, m, s);
        }

        @Override
        public long getDurationSeconds() {
            return positionInfo.getTrackDurationSeconds();
        }

        public void resetTrackInfo() {
            positionInfo = new PositionInfo();
            mediaInfo = new MediaInfo();
        }

        @Override
        public String toString() {
            return "UpnpRendererState [state=" + state + ", volume=" + volume + ", repeatMode=" + repeatMode + ", randomMode="
                    + randomMode + ", positionInfo=" + positionInfo + ", mediaInfo=" + mediaInfo + ", trackMetadata="
                    + new TrackMetadata(positionInfo.getTrackMetaData()) + "]";
        }

        @Override
        public int getElapsedPercent() {
            return positionInfo.getElapsedPercent();
        }

        @Override
        public String getTitle() {
            return getTrackMetadata().getTitle();
        }

        @Override
        public String getArtist() {
            return getTrackMetadata().getArtist();
        }
    }
}
