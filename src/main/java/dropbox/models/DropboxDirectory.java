package dropbox.models;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v2.users.FullAccount;
import exceptions.*;
import models.Arhive;
import models.Directory;
import models.LocalDirectory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author dzimiks
 * Date: 13-04-2019 at 19:16
 */
public class DropboxDirectory implements Directory {
	/**
	 * A grouping of a few configuration parameters for how we should make requests to the Dropbox servers.
	 */
	private DbxRequestConfig config = null;
	/**
	 * Use this variable to make remote calls to the Dropbox API user endpoints.
	 */
	private DbxClientV2 client = null;
	/**
	 * Detailed information about the current user's account.
	 */
	private FullAccount account = null;
	/**
	 * Token used for establishing connection with app's directory in dropbox.
	 */
	private String ACCESS_TOKEN;

	/**
	 * Dropbox directory constructor.
	 *
	 * @param accessToken sets access token read from config file.
	 */
	public DropboxDirectory(String accessToken) {
		this.ACCESS_TOKEN = accessToken;
		initClient("file-storage-remote");
	}

	/**
	 * Initializing client.
	 *
	 * @param clientID
	 */
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
	public void create(String name, String path) throws CreateDirectoryException {
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
//			System.out.print("Some other Exception occurred...");
//			System.out.print(err.toString());
			throw new CreateDirectoryException();
		}
	}

	@Override
	public void delete(String path) throws DeleteException {
		try {
			client.files().deleteV2(path);
		} catch (DbxException e) {
			e.printStackTrace();
			throw new DeleteException();
		}
	}

	@Override
	public void download(String src, String dest) throws DownloadException {
		try {
			DbxDownloader<DownloadZipResult> result = client.files().downloadZip(src);
			result.download(new FileOutputStream(dest));
		} catch (DbxException | IOException e) {
			e.printStackTrace();
			throw new DownloadException();
		}
	}

	@Override
	public void upload(String src, String dest) throws UploadException {
		Arhive arhive = new Arhive();
//		String path = "/Users/dzimiks/Desktop/projects/file-storage-drive/src/main/java/dropbox/models/dropbox_zip";
		String name = src.substring(src.lastIndexOf(File.separator) + 1);
		System.out.println(name);

		try {
			arhive.zipDirectory(new File("/Users/dzimiks/Desktop/projects/file-storage-drive/" + src), name, ".");
		} catch (IOException e) {
			e.printStackTrace();
			throw new UploadException();
		}

		try {
			InputStream in = new FileInputStream(src + File.separator + name + ".zip");
			FileMetadata metadata = client.files().uploadBuilder(dest).uploadAndFinish(in);
		} catch (IOException | DbxException ioe) {
			ioe.printStackTrace();
			throw new UploadException();
		}
	}

	@Override
	public void uploadMultiple(ArrayList<File> files, String dest, String name) throws UploadMultipleException {

	}

	@Override
	public void uploadMultipleZip(ArrayList<File> files, String dest, String name) throws UploadMultipleZipException {
		Arhive arhive = new Arhive();
		LocalDirectory localDirectory = new LocalDirectory();
		System.out.println(name);

		try {
			localDirectory.uploadMultiple(files, "/Users/dzimiks/Desktop/projects/file-storage-drive/src/dropbox_zip", name);
		} catch (UploadMultipleException e) {
			e.printStackTrace();
		}

		try {
			arhive.zipDirectory(new File("/Users/dzimiks/Desktop/projects/file-storage-drive/src/dropbox_zip"), name, "./");
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			InputStream in = new FileInputStream(name + ".zip");
			FileMetadata metadata = client.files().uploadBuilder(dest + File.separator + name + ".zip").uploadAndFinish(in);
		} catch (IOException | DbxException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public void move(String src, String dest) throws MoveException {

	}

	@Override
	public void rename(String name, String path) throws RenameException {

	}

	@Override
	public ArrayList<File> listFiles(String s, boolean b) throws ListFilesException{
		ArrayList<File> files = new ArrayList<>();
		ListFolderBuilder listFolderBuilder = client.files().listFolderBuilder("");
		ListFolderResult result = null;

		try {
			result = listFolderBuilder.withRecursive(true).start();
		} catch (DbxException e) {
			e.printStackTrace();
			throw new ListFilesException();
		}

		while (true) {
			if (result != null) {
				for (Metadata entry : result.getEntries()) {
					if (entry instanceof FileMetadata) {
						System.out.println("Added file: " + entry.getPathLower());
						files.add(new File(entry.getPathDisplay()));
					}
				}

				if (!result.getHasMore()) {
					return files;
				}

				try {
					result = client.files().listFolderContinue(result.getCursor());
				} catch (DbxException e) {
					System.out.println("Couldn't get listFolderContinue");
					throw new ListFilesException();
				}
			}
		}
	}

	@Override
	public ArrayList<File> listFilesWithExtensions(String s, String[] strings, boolean b) throws ListFilesException{
		return null;
	}

	public ArrayList<String> listFilesWithGivenExtensions(String s, String[] strings, boolean b) throws ListFilesException {
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
			throw new ListFilesException();
		}

		if (b) {
			Collections.sort(files);
		}

		return files;
	}

	@Override
	public ArrayList<File> listDirs(String s, boolean b) throws ListDirectoryException {
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
			throw new ListDirectoryException();
		}
		return directories;
	}
}
