package com.victor.player.library.ytparser.jsevaluator.interfaces;

import android.webkit.WebView;

public interface WebViewWrapperInterface {
	public void loadJavaScript(String javascript);

	public void destroy();

	public WebView getWebView();
}
