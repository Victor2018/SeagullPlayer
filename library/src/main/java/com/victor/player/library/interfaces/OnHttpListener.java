package com.victor.player.library.interfaces;

public interface OnHttpListener<T> {
	void onComplete(int videoType, T data, String msg);
}
