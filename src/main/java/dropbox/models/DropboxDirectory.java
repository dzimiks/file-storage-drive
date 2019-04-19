package dropbox.models;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v2.users.FullAccount;
import models.Arhive;
import models.Directory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author dzimiks
 * Date: 13-04-2019 at 19:16
 */
public class DropboxDirectory implements Directory {

	private DbxRequestConfig config = null;
	private DbxClientV2 client = null;
	private FullAccount account = null;
	private String ACCESS_TOKEN;

	public DropboxDirectory(String accessToken) {
		this.ACCESS_TOKEN = accessToken;
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
		try {
			DbxDownloader<DownloadZipResult> result = client.files().downloadZip(src);
			result.download(new FileOutputStream(dest));
		} catch (DbxException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void upload(String src, String dest) {
		Arhive arhive = new Arhive();
//		String path = "/Users/dzimiks/Desktop/projects/file-storage-drive/src/main/java/dropbox/models/dropbox_zip";
		String name = src.substring(src.lastIndexOf(File.separator) + 1);
		System.out.println(name);

		try {
			arhive.zipDirectory(new File("/Users/dzimiks/Desktop/projects/file-storage-drive/" + src), name, ".");
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			InputStream in = new FileInputStream(src + File.separator + name + ".zip");
			FileMetadata metadata = client.files().uploadBuilder(dest).uploadAndFinish(in);
		} catch (IOException | DbxException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public void uploadMultiple(ArrayList<File> files, String dest,String name) {

	}

	@Override
	public void uploadMultipleZip(ArrayList<File> files, String dest,String name) {

	}

	@Override
	public void move(String src, String dest) {

	}

	@Override
	public void rename(String name, String path) {

	}

	@Override
	public void listFiles(String s, boolean b) {
		ListFolderBuilder listFolderBuilder = client.files().listFolderBuilder("");
		ListFolderResult result = null;

		try {
			result = listFolderBuilder.withRecursive(true).start();
		} catch (DbxException e) {
			e.printStackTrace();
		}

		while (true) {
			if (result != null) {
				for (Metadata entry : result.getEntries()) {
					if (entry instanceof FileMetadata) {
						System.out.println("Added file: " + entry.getPathLower());
					}
				}

				if (!result.getHasMore()) {
					return;
				}

				try {
					result = client.files().listFolderContinue(result.getCursor());
				} catch (DbxException e) {
					System.out.println("Couldn't get listFolderContinue");
				}
			}
		}
	}

	@Override
	public void listFilesWithExtensions(String s, String[] strings, boolean b) {

	}

	public ArrayList<String> listFilesWithGivenExtensions(String s, String[] strings, boolean b) {
		ArrayList<String> files = new ArrayList<>();

		try {
			for (String query : strings) {
				SearchResult searchResult = client.files().search(s, query);

				for (SearchMatch match : searchResult.getMatches()) {
					Metadata metadata = match.getMetadata();
					files.add(metadata.getPathDisplay());
				}
			}
		} catch (DbxException e) {
			e.printStackTrace();
		}

		if (b) {
			Collections.sort(files);
		}

		return files;
	}

	@Override
	public ArrayList<File> listDirs(String s, boolean b) {
		ArrayList<File> directories = new ArrayList<>();
		ListFolderBuilder folderMetadata = client.files().listFolderBuilder(s);

		try {
			ListFolderResult result = folderMetadata.start();

			for (Metadata data : result.getEntries()) {
//				System.out.println(data.getPathDisplay());
				directories.add(new File(data.getPathDisplay()));
			}
		} catch (DbxException e) {
			e.printStackTrace();
		}
		return directories;
	}
}
