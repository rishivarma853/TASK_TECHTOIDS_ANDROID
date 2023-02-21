package com.techtoids.nota.view.custom;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.inputmethod.EditorInfoCompat;

public class EditorTextView extends WebView {

    private static final String ASSETS_EDITOR_HTML = "file:///android_asset/editor.html";

    private boolean isReady;

    private boolean isIncognitoModeEnabled;
    private String content;
    private OnTextChangeListener onTextChangeListener;
    private boolean isContentEmpty;
    private boolean isFullScreen;
    private int maximumHeight = 0;
    private int lastHeight = 0;

    public EditorTextView(Context context) {
        super(context);
    }

    public EditorTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditorTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void init() {
        WebSettings settings = getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        setWebChromeClient(new WebChromeClient());
        setWebViewClient(new EditorTextViewWebViewClient());
        addJavascriptInterface(this, "EditorTextView");
        loadUrl(ASSETS_EDITOR_HTML);
    }

    @Override
    public void destroy() {
        exec("javascript:destroy();");

        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
        super.destroy();
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection inputConnection = super.onCreateInputConnection(outAttrs);
        if (isIncognitoModeEnabled) {
            outAttrs.imeOptions = outAttrs.imeOptions
                    | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING;
        }
        return inputConnection;
    }

    public void setIncognitoModeEnabled(boolean enabled) {
        this.isIncognitoModeEnabled = enabled;
    }

    public void setOnTextChangeListener(@Nullable OnTextChangeListener listener) {
        this.onTextChangeListener = listener;
    }

    @JavascriptInterface
    public void onEditorContentChanged(String content, boolean isEmpty) {
        if (onTextChangeListener != null) {
            onTextChangeListener.onTextChanged(content);
        }
        this.content = content;
        this.isContentEmpty = isEmpty;
    }

    @JavascriptInterface
    public void updateCurrentStyle(String style) {
        // TODO: Update buttons state
    }

    public void enable() {
        exec("javascript:enable();");
    }

    public void disable() {
        exec("javascript:disable();");
    }

    public void undo() {
        exec("javascript:undo();");
    }

    public void redo() {
        exec("javascript:redo();");
    }

    public void clear() {
        exec("javascript:clear();");
    }

    public void focus() {
        exec("javascript:setFocus();");
    }

    public String getHtml() {
        return content;
    }

    public void setHtml(@NonNull String html) {
        exec("javascript:setHtml('" + html + "');");
    }

    public void enableFullscreen() {
        isFullScreen = true;
        exec("javascript:enableFullscreen();");
    }

    public void setBold() {
        exec("javascript:setBold();");
    }

    public void setItalic() {
        exec("javascript:setItalic();");
    }

    public void setUnderline() {
        exec("javascript:setUnderline();");
    }

    public void setStrikeThrough() {
        exec("javascript:setStrikeThrough();");
    }

    public void removeFormat() {
        exec("javascript:removeFormat();");
    }

    public void setFontSize(int sizeInPx) {
        exec("javascript:setFontSize(" + sizeInPx + ");");
    }

    public void setNormal() {
        exec("javascript:setNormal();");
    }

    public void setHeading(@IntRange(from = 1, to = 6) int value) {
        exec("javascript:setHeading('" + value + "');");
    }

    public void setLineHeight(int heightInPx) {
        exec("javascript:setLineHeight(" + heightInPx + ");");
    }

    public void setSuperscript() {
        exec("javascript:setSuperscript();");
    }

    public void setSubscript() {
        exec("javascript:setSubscript()");
    }

    public void setTextColor(@ColorInt int color) {
        setTextColor(String.format("#%06X", (0xFFFFFF & color)));
    }

    public void setTextColor(@NonNull String hexColor) {
        exec("javascript:setTextForeColor('" + hexColor + "');");
    }

    public void setTextBackgroundColor(@ColorInt int color) {
        setTextBackgroundColor(String.format("#%06X", (0xFFFFFF & color)));
    }

    public void setTextBackgroundColor(@NonNull String hexColor) {
        exec("javascript:setTextBackColor('" + hexColor + "');");
    }

    public void setBlockCode() {
        exec("javascript:setBlockCode();");
    }

    public void setUnorderedList() {
        exec("javascript:insertUnorderedList();");
    }

    public void setOrderedList() {
        exec("javascript:insertOrderedList();");
    }

    public void setBlockQuote() {
        exec("javascript:setBlockQuote();");
    }

    public void setAlignLeft() {
        exec("javascript:setAlignLeft();");
    }

    public void setAlignCenter() {
        exec("javascript:setAlignCenter();");
    }

    public void setAlignRight() {
        exec("javascript:setAlignRight();");
    }

    public void setAlignJustify() {
        exec("javascript:setAlignJustify();");
    }

    public void insertHorizontalRule() {
        exec("javascript:insertHorizontalRule();");
    }

    public void setIndent() {
        exec("javascript:indent();");
    }

    public void setOutdent() {
        exec("javascript:outdent();");
    }

    public void insertTable(int colCount, int rowCount) {
        exec("javascript:insertTable('" + colCount + "x" + rowCount + "');");
    }

    public void insertLink(@Nullable String title, @NonNull String url) {
        exec("javascript:insertLink('" + title + "','" + url + "');");
    }

    public void setUnlink() {
        exec("javascript:unlink();");
    }

    public void insertText(@NonNull String text) {
        exec("javascript:insertText('" + text + "');");
    }

    public void insertImage(@NonNull String url) {
        exec("javascript:insertImage('" + url + "');");
    }

    public void editHtml() {
        exec("javascript:editHtml();");
    }

    public boolean isEmpty() {
        return isContentEmpty;
    }

    protected void exec(@NonNull final String trigger) {
        if (isReady) {
            load(trigger);
        } else {
            postDelayed(() -> exec(trigger), 100);
        }
    }

    private void load(@NonNull String trigger) {
        evaluateJavascript(trigger, null);
    }

    public int getCurrentHeight() {
        return lastHeight;
    }

    public int getMaximumHeight() {
        return maximumHeight;
    }

    public void setMaximumHeight(int maximumHeight) {
        this.maximumHeight = maximumHeight;
        resize(lastHeight);
    }

    @JavascriptInterface
    public void resize(final float height) {
        this.lastHeight = (int) height;
        if (!isFullScreen) {
            ((Activity) getContext()).runOnUiThread(() -> {
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.height = Math.max(
                        (int) (height * getResources().getDisplayMetrics().density),
                        getMinimumHeight());
                if (getMaximumHeight() > -1) {
                    layoutParams.height = Math.min(layoutParams.height, getMaximumHeight());
                }
                setLayoutParams(layoutParams);
            });
        }
    }

    public interface OnTextChangeListener {
        void onTextChanged(String content);
    }

    private class EditorTextViewWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            isReady = url.equalsIgnoreCase(ASSETS_EDITOR_HTML);
            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }
    }
}