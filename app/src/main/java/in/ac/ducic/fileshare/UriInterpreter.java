/*
 * Contatins code from https://github.com/k9mail/k-9/blob/master/src/com/fsck/k9/activity/MessageCompose.java
 * APACHE 2.0 License.
 *
 */
package in.ac.ducic.fileshare;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLDecoder;

import in.ac.ducic.fileshare.activities.MainActivity;

public class UriInterpreter {

	private long size = -1;
	private String name = null;
    private String path = null;
	private String mime;
	private boolean isDirectory = false;
	private Uri uri;
    private ContentResolver contentResolver;

	public InputStream getInputStream() throws FileNotFoundException {
		return contentResolver.openInputStream(uri);
	}

	public UriInterpreter(Uri uri) {
		this.uri = uri;

		contentResolver = MainActivity.context.getContentResolver();
		Cursor metadataCursor = contentResolver.query(uri, new String[] {
				OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE }, null,
				null, null);

		if (metadataCursor != null) {
			try {
				if (metadataCursor.moveToFirst()) {
					path = name = metadataCursor.getString(0);
					size = metadataCursor.getInt(1);
				}
			} finally {
				metadataCursor.close();
			}
		}

		if (name == null) {
			name = uri.getLastPathSegment();
            path = uri.toString();
		}

		getMime(uri, contentResolver);

		getFileSize(uri);

	}

	private void getFileSize(Uri uri) {
		if (size <= 0) {
			String uriString = uri.toString();
			if (uriString.startsWith("file://")) {
				File f = new File(uriString.substring("file://".length()));
				isDirectory = f.isDirectory();
				if (isDirectory) {
					// Log.v(Util.myLogName, "We are dealing with a directory.");
					size = 0;
					return;
				}
				size = f.length();
				if (size == 0) {
					Log.e("uriString",uriString);
					uriString = URLDecoder.decode(uriString).substring(
							"file://".length());
					Log.e("uriString2",uriString);
					f = new File(uriString);
					size = f.length();
				}


			} else {
				try {
					File f = new File(uriString);
					isDirectory = f.isDirectory();
					return;
				} catch (Exception e) {

					e.printStackTrace();
				}


			}
		}
	}

	private void getMime(Uri uri, ContentResolver contentResolver) {
		mime = contentResolver.getType(uri);
		if (mime == null) {
			mime = "application/octet-stream";
		}
		if (mime.equals("application/octet-stream")) {
			// we can do better than that
			int pos = name.lastIndexOf('.');
			if (pos < 0)
				return;
			String extension = name.substring(pos).toLowerCase();
			if (extension.equals(".jpg")) {
				mime = "image/jpeg";
				return;
			}
			if (extension.equals(".png")) {
				mime = "image/png";
				return;
			}
			if (extension.equals(".gif")) {
				mime = "image/gif";
				return;
			}
			if (extension.equals(".mp4")) {
				mime = "video/mp4";
				return;
			}
			if (extension.equals(".avi")) {
				mime = "video/avi";
				return;
			}
			if (extension.equals(".mov")) {
				mime = "video/mov";
				return;
			}
			if (extension.equals(".vcf")) {
				mime = "text/x-vcard";
				return;
			}
			if (extension.equals(".txt")) {
				mime = "text/plain";
				return;
			}
			if (extension.equals(".html")) {
				mime = "text/html";
				return;
			}
			if (extension.equals(".json")) {
				mime = "application/json";
				return;
			}

		}

	}

    public long getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getMime() {
        return mime;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public Uri getUri() {
        return uri;
    }
}
