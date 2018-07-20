package com.victor.player.library.ytparser.jsevaluator.interfaces;

import android.webkit.WebView;

public interface JsEvaluatorInterface {
	public void callFunction(String jsCode, JsCallback resultCallback, String name, Object... args);

	public void evaluate(String jsCode);

	public void evaluate(String jsCode, JsCallback resultCallback);

	public void destroy();

	public WebView getWebView();
}
