package se.hemmagym.app;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class MainActivity extends Activity {

    // Appen serveras från de inbyggda filerna i assets/www under en egen "adress".
    // Det ger localStorage som överlever uppdateringar och fungerar helt utan internet.
    private static final String HOST = "appassets.local";
    private static final String START_URL = "https://" + HOST + "/index.html";

    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0a0a0a"));
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);

        web.addJavascriptInterface(new Bridge(), "AndroidBridge");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (!HOST.equals(uri.getHost())) {
                    return null;
                }
                return serveAsset(uri);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Tillåt bara navigering inom appen.
                return !HOST.equals(request.getUrl().getHost());
            }
        });

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            web.loadUrl(START_URL);
        }
    }

    private WebResourceResponse serveAsset(Uri uri) {
        String path = uri.getPath();
        if (path == null || path.isEmpty() || path.equals("/")) {
            path = "/index.html";
        }
        if (path.contains("..")) {
            return notFound();
        }
        try {
            InputStream in = getAssets().open("www" + path);
            String mime = mimeFor(path);
            boolean text = mime.startsWith("text/") || mime.equals("application/javascript")
                    || mime.equals("application/json");
            return new WebResourceResponse(mime, text ? "utf-8" : null, in);
        } catch (IOException e) {
            return notFound();
        }
    }

    private WebResourceResponse notFound() {
        return new WebResourceResponse("text/plain", "utf-8", 404, "Not Found",
                new HashMap<String, String>(), new ByteArrayInputStream(new byte[0]));
    }

    private String mimeFor(String path) {
        String p = path.toLowerCase();
        if (p.endsWith(".html")) return "text/html";
        if (p.endsWith(".js")) return "application/javascript";
        if (p.endsWith(".css")) return "text/css";
        if (p.endsWith(".json")) return "application/json";
        if (p.endsWith(".woff2")) return "font/woff2";
        if (p.endsWith(".png")) return "image/png";
        if (p.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        // Appen är en enda sida – minimera i stället för att stänga och tappa pågående pass.
        moveTaskToBack(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Anropas från JavaScript för att spara exporterad data i mappen Nedladdningar. */
    private class Bridge {
        @JavascriptInterface
        public boolean saveFile(String name, String content) {
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, name);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/json");
                values.put(MediaStore.Downloads.RELATIVE_PATH, "Download");
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    throw new IOException("Kunde inte skapa filen");
                }
                OutputStream out = getContentResolver().openOutputStream(uri);
                if (out == null) {
                    throw new IOException("Kunde inte öppna filen");
                }
                try {
                    out.write(content.getBytes(StandardCharsets.UTF_8));
                } finally {
                    out.close();
                }
                toast("Sparad i Nedladdningar: " + name);
                return true;
            } catch (Exception e) {
                toast("Kunde inte spara: " + e.getMessage());
                return false;
            }
        }
    }
}
