package main;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;

/**
 * @author dzimiks
 * Date: 13-04-2019 at 16:10
 */
public class FileDownloadProgressListener implements MediaHttpDownloaderProgressListener {

	@Override
	public void progressChanged(MediaHttpDownloader downloader) {
		switch (downloader.getDownloadState()) {
			case MEDIA_IN_PROGRESS:
				View.header2("Download is in progress: " + downloader.getProgress());
				break;
			case MEDIA_COMPLETE:
				View.header2("Download is Complete!");
				break;
		}
	}
}