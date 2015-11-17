package in.ac.ducic.fileshare.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import in.ac.ducic.fileshare.R;
import in.ac.ducic.fileshare.UriInterpreter;

public class MainActivity extends BaseActivity {

    public static final int REQUEST_CODE = 1024;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();
        setupTextViews();
        setupNavigationViews();
        createViewClickListener();
        setupPickItemView();

    }

    private void setupPickItemView() {
        findViewById(R.id.pick_items).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_CODE);
            }

        });
    }

    private void setViewsVisible() {
        findViewById(R.id.link_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.navigation_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.scrolling_information).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            UriInterpreter fileuri = getFileUris(data);
            populateUriPath(fileuri);
            initHttpServer(fileuri);
            setLinkMessageToView();
            setViewsVisible();
        }
    }



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private UriInterpreter getFileUris(Intent data) {

        Uri dataUri = data.getData();
        if (dataUri != null) {
            return new UriInterpreter(dataUri);
        }
        return null;
    }
}
