package in.ac.ducic.fileshare.activities;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.ac.ducic.fileshare.HttpServer;
import in.ac.ducic.fileshare.R;
import in.ac.ducic.fileshare.UriInterpreter;

public class BaseActivity extends ActionBarActivity {

    protected static HttpServer httpServer = null;
    protected String preferredServerUrl;
    protected LinearLayout linearLayout;
    protected TextView link_msg;
    protected TextView uriPath;
    protected View stopServer;
    public static Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
    }

    protected void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setTitleTextColor(getResources().getColor(R.color.light));
        setSupportActionBar(toolbar);
    }

    protected void setupTextViews() {
        link_msg = (TextView) findViewById(R.id.link_msg);
        uriPath = (TextView) findViewById(R.id.uriPath);
        linearLayout = (LinearLayout) findViewById(R.id.linear);
    }

    protected void setupNavigationViews() {
        stopServer = findViewById(R.id.stop_server);
    }

    protected void createViewClickListener() {
        stopServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HttpServer p = httpServer;
                httpServer = null;
                if (p != null) {
                    p.stopServer();
                }
                setViewsInvisible();
                Snackbar.make(linearLayout, "" + getString(R.string.now_sharing_anymore), Snackbar.LENGTH_LONG).show();
            }
        });

    }
    protected void populateUriPath(UriInterpreter uriList) {
        StringBuilder output = new StringBuilder();
        output.append(uriList.getPath());
        uriPath.setText(output.toString());
    }

    protected void initHttpServer(UriInterpreter myUris) {
        this.context = this.getApplicationContext();
        if (myUris == null || myUris.isDirectory()) {
            finish();
            return;
        }
        httpServer = new HttpServer(9999);
        preferredServerUrl = httpServer.getIPAddress();
        httpServer.SetFiles(myUris);
    }

    protected void setLinkMessageToView() {
        link_msg.setPaintFlags(link_msg.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        link_msg.setText(preferredServerUrl);
    }
    private void setViewsInvisible() {
        findViewById(R.id.link_layout).setVisibility(View.GONE);
        findViewById(R.id.navigation_layout).setVisibility(View.GONE);
        findViewById(R.id.scrolling_information).setVisibility(View.GONE);
    }
}
