package dropbox.models;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.users.FullAccount;
import models.Arhive;
import models.Directory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author dzimiks
 * Date: 13-04-2019 at 19:16
 */
public class DropboxDirectory implements Directory {

	private static final String ACCESS_TOKEN = "xxx";
	private DbxRequestConfig config = null;
	private DbxClientV2 client = null;
	private FullAccount account = null;

	public DropboxDirectory() {
		initClient("file-storage-remote");
	}

	public void initClient(String clientID) {
		this.config = new DbxRequestConfig(clientID);
		this.client = new DbxClientV2(config, ACCESS_TOKEN);

		try {
			FullAccount account = this.client.users().getCurrentAccount();
			System.out.println("Account: " + account.getName().getDisplayName());
		} catch (DbxException dbxe) {
			dbxe.printStackTrace();
		}
	}

	public DbxClientV2 getClient() {
		return client;
	}

	@Override
	public void create(String name, String path) {
		try {
			FolderMetadata dir = client.files().createFolder(name);
			System.out.println(dir.getName());
		} catch (CreateFolderErrorException err) {
			if (err.errorValue.isPath() && err.errorValue.getPathValue().isConflict()) {
				System.out.println("Something already exists at the path.");
				System.out.println(err.errorValue.getPathValue());
			} else {
				System.out.print("Some other CreateFolderErrorException occurred...");
				System.out.print(err.toString());
			}
		} catch (Exception err) {
			System.out.print("Some other Exception occurred...");
			System.out.print(err.toString());
		}
	}

	@Override
	public void delete(String path) {
		try {
			client.files().deleteV2(path);
		} catch (DbxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void download(String src, String dest) {

	}

	@Override
	public void upload(String src, String dest) {
//		Arhive arhive = new Arhive();
		String path = "/Users/dzimiks/Desktop/projects/file-storage-drive/src/main/java/dropbox/models/dropbox_zip";

//		try {
//			arhive.zipDirectory(new File(src), "dir", path);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		try {
			InputStream in = new FileInputStream(path + File.separator + "dir.zip");
			FileMetadata metadata = client.files().uploadBuilder(dest).uploadAndFinish(in);
		} catch (IOException | DbxException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public void uploadMultiple(ArrayList<File> files, String dest) {

	}

	@Override
	public void uploadMultipleZip(ArrayList<File> files, String dest) {

	}

	@Override
	public void move(String src, String dest) {

	}

	@Override
	public void rename(String name, String path) {

	}

	@Override
	public void listFiles(String s, boolean b) {

	}

	@Override
	public void listFilesWithExtensions(String s, String[] strings, boolean b) {

	}

	@Override
	public void listDirs(String s, boolean b) {

	}
}
