package dropbox.models;

import exceptions.CreateFileException;

import java.io.File;

/**
 * @author dzimiks
 * Date: 13-04-2019 at 19:19
 */
public class Main {

	public static void main(String[] args) {
		DropboxDirectory storage = new DropboxDirectory();
		String storageName = File.separator + "UUP2018-januar";

//		storage.create(storageName, null);
//		storage.delete(storageName);

//		storage.create(storageName + File.separator + "grupa1", null);
//		storage.create(storageName + File.separator + "grupa2", null);

//		storage.upload(
//				"/Users/dzimiks/Desktop/projects/file-storage-drive/src/main/java/dropbox",
//				storageName + File.separator + "dropbox.zip"
//		);

		DropboxFile dropboxFile = new DropboxFile(storage.getClient());

		try {
			dropboxFile.create("test.txt", storageName);
		} catch (CreateFileException e) {
			e.printStackTrace();
		}

//		dropboxFile.upload(
//				"/Users/dzimiks/Desktop/projects/file-storage-drive/src/main/java/main/dzimiks.jpg",
//				"/dropbox/dzimiks.jpg");
	}
}
