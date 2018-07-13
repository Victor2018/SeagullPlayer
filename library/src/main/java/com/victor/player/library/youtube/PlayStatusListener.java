package com.victor.player.library.youtube;

/**
 * @Author Victor
 * @Date Create on 2018/5/18 14:41.
 * @Describe
 */

public interface PlayStatusListener {
    void onPlayStatus(int status, int duration);
}
