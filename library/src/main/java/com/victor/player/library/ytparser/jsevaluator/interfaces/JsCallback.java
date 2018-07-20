package com.victor.player.library.ytparser.jsevaluator.interfaces;

public interface JsCallback {
	public abstract void onResult(String value);
	public abstract void onError(String errorMessage);
}
