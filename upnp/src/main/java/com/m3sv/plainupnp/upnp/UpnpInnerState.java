package com.m3sv.plainupnp.upnp;

import com.m3sv.plainupnp.data.upnp.UpnpItemType;
import com.m3sv.plainupnp.data.upnp.UpnpRendererState;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;
import timber.log.Timber;

class UpnpInnerState extends MainThreadDisposable implements UpnpRendererState {
    private final Observer<? super UpnpRendererState> observer;

    // Player info
    private State state;
    private int volume;
    private boolean mute;
    private int repeatMode; // TODO enum with different mode
    private int randomMode; // TODO enum with different mode

    // Track info
    private PositionInfo positionInfo;
    private MediaInfo mediaInfo;
    private TransportInfo transportInfo;

    private final String itemId;
    private final String itemUri;
    private final UpnpItemType itemType;

    public UpnpInnerState(Observer<? super UpnpRendererState> observer,
                          String itemId,
                          String itemUri,
                          UpnpItemType itemType) {
        super();
        this.itemId = itemId;
        this.itemUri = itemUri;
        this.itemType = itemType;

        Timber.d("Initial state!");
        this.observer = observer;
        state = State.INITIALIZING;
        volume = -1;
        resetTrackInfo();

        updateState();
    }

    private void updateState() {
        if (!isDisposed()) {
            observer.onNext(this);
        }
    }

    @Override
    protected void onDispose() {

    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(@NotNull State state) {
        if (this.state == state)
            return;

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
            Timber.e(e);
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

    @NotNull
    @Override
    public String getId() {
        return itemId;
    }

    @Nullable
    @Override
    public String getUri() {
        return itemUri;
    }

    @NotNull
    @Override
    public UpnpItemType getType() {
        return itemType;
    }
}
