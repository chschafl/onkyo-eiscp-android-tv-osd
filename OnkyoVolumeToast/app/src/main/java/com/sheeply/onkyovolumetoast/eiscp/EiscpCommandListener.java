package com.sheeply.onkyovolumetoast.eiscp;

public interface EiscpCommandListener {
    void onDisconnected();
    void onVolumeChanged(int volume);
    void onMutedChanged(boolean muted);
}
