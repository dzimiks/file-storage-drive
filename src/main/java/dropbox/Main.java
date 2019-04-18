package dropbox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

/**
 * @author dzimiks
 * Date: 13-04-2019 at 16:59
 */
public class Main {

	private static final String ACCESS_TOKEN = "xxx";
	private DbxRequestConfig config = null;
	DbxClientV2 client = null;
	FullAccount account = null;

	public Main() {
		// Create Dropbox client
		config = new DbxRequestConfig("file-storage-remote");
		client = new DbxClientV2(config, ACCESS_TOKEN);

		try {
			FullAccount account = client.users().getCurrentAccount();
			System.out.println(account.getName().getDisplayName());
		} catch (DbxException dbxe) {
			dbxe.printStackTrace();
		}
	}

	public static void main(String args[]) throws DbxException {
		Main m = new Main();
		String folderName = "/storage";
		m.listFolder();
		m.createFolder(folderName);
//		m.uploadFile("/Users/dzimiks/Desktop/projects/file-storage-drive/src/main/java/dropbox/test.txt", folderName + "/test.txt");
		m.uploadFile("/Users/dzimiks/Desktop/projects/file-storage-drive/src/main/java/main/dzimiks.jpg", folderName + "/dzimiks.jpg");
//		m.readFile(folderName + "/Overview of Archival and Purge Process.pdf", "Overview of Archival and Purge Process.pdf");
//		m.deleteFile(folderName + "/Overview of Archival and Purge Process.pdf");
	}

	public void createFolder(String folderName) throws DbxException {
		try {
			FolderMetadata folder = client.files().createFolder(folderName);
			System.out.println(folder.getName());
		} catch (CreateFolderErrorException err) {
			if (err.errorValue.isPath() && err.errorValue.getPathValue().isConflict()) {
				System.out.println("Something already exists at the path.");
			} else {
				System.out.print("Some other CreateFolderErrorException occurred...");
				System.out.print(err.toString());
			}
		} catch (Exception err) {
			System.out.print("Some other Exception occurred...");
			System.out.print(err.toString());
		}
	}

	public void listFolder() {
		try {
			// Get files and folder metadata from Dropbox root directory
			ListFolderResult result = client.files().listFolder("");
			while (true) {
				for (Metadata metadata : result.getEntries()) {
					System.out.println(metadata.getPathLower());
				}

				if (!result.getHasMore()) {
					break;
				}

				result = client.files().listFolderContinue(result.getCursor());
			}
		} catch (DbxException dbxe) {
			dbxe.printStackTrace();
		}
	}

	public void uploadFile(String path, String foldername) {
		// Upload "test.txt" to Dropbox
		try {
			InputStream in = new FileInputStream(path);
			FileMetadata metadata = client.files().uploadBuilder(foldername).uploadAndFinish(in);
		} catch (IOException | DbxException ioe) {
			ioe.printStackTrace();
		}
	}

	public void readFile(String foldername, String filename) {
		try {
			//output file for download --> storage location on local system to download file
			FileOutputStream downloadFile = new FileOutputStream(filename);
			try {
				FileMetadata metadata = client.files().downloadBuilder(foldername).download(downloadFile);
			} finally {
				downloadFile.close();
			}
		}
		//exception handled
		catch (DbxException | IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteFile(String path) {
		try {
			Metadata metadata = client.files().delete(path);
		} catch (DbxException dbxe) {
			dbxe.printStackTrace();
		}
	}
}