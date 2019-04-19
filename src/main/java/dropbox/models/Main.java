package dropbox.models;

import exceptions.CreateFileException;
import exceptions.ListFilesException;

import java.io.File;
import java.util.ArrayList;

/**
 * @author dzimiks
 * Date: 13-04-2019 at 19:19
 */
public class Main {

	public static void main(String[] args) {
		DropboxDirectory storage = new DropboxDirectory("75JP0V7E00AAAAAAAAAAwSkAclPzcUjOLxmQ31IHbn0OpmejKrgIrmCtrLDtrGr1");
		String storageName = File.separator + "UUP2018-januar";

		// TODO
		try {
			storage.listFiles("", true);
		} catch (ListFilesException e) {
			e.printStackTrace();
		}

		// TODO
//		ArrayList<String> files = storage.listFilesWithGivenExtensions("/UUP2018-januar", new String[]{"zip", "txt"}, true);
//
//		for (String f : files) {
//			System.out.println(f);
//		}

		// TODO
//		storage.listDirs("/UUP2018-januar", false);

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
