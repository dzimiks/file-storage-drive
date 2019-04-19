package dropbox.models;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import exceptions.*;
import models.BasicFile;

import java.io.*;
import java.util.List;

/**
 * @author dzimiks
 * Date: 13-04-2019 at 19:04
 */
public class DropboxFile implements BasicFile {
	/**
	 * Use this variable to make remote calls to the Dropbox API user endpoints.
	 */
	private DbxClientV2 client = null;

	/**
	 * Dropbox file constructor
	 * @param client contains access token
	 */
	public DropboxFile(DbxClientV2 client) {
		this.client = client;
	}

	@Override
	public void create(String name, String path) throws CreateFileException {

	}

	@Override
	public void delete(String path) throws DeleteException {
		try {
			Metadata metadata = client.files().delete(path);
		} catch (DbxException dbxe) {
			dbxe.printStackTrace();
			throw new DeleteException();
		}
	}

	@Override
	public void download(String src, String dest) throws DownloadException {
		try {
			OutputStream downloadFile = new FileOutputStream(dest);

			try {
				FileMetadata metadata = client.files()
						.downloadBuilder(src)
						.download(downloadFile);
			} finally {
				downloadFile.close();
			}
		} catch (DbxException | IOException e) {
			System.out.println("Unable to download file to local system\n Error: " + e);
			throw new DownloadException();
		}
	}

	@Override
	public void upload(String src, String dest) throws UploadException {
		try {
			InputStream in = new FileInputStream(src);
			FileMetadata metadata = client.files().uploadBuilder(dest).uploadAndFinish(in);
		} catch (IOException | DbxException ioe) {
			ioe.printStackTrace();
			throw new UploadException();
		}
	}

	@Override
	public void uploadMultiple(List<File> files, String dest, String name) throws UploadMultipleException {

	}

	@Override
	public void uploadMultipleZip(List<File> files, String dest,String name) throws UploadMultipleZipException {

	}

	@Override
	public void move(String src, String dest) throws MoveException{

	}

	@Override
	public void rename(String name, String path) throws RenameException{

	}
}
