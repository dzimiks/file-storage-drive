package dropbox.models;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import exceptions.CreateFileException;
import models.BasicFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author dzimiks
 * Date: 13-04-2019 at 19:04
 */
public class DropboxFile implements BasicFile {

	private DbxClientV2 client = null;

	public DropboxFile(DbxClientV2 client) {
		this.client = client;
	}

	@Override
	public void create(String name, String path) throws CreateFileException {

	}

	@Override
	public void delete(String path) {
		try {
			Metadata metadata = client.files().delete(path);
		} catch (DbxException dbxe) {
			dbxe.printStackTrace();
		}
	}

	@Override
	public void download(String src, String dest) {

	}

	@Override
	public void upload(String src, String dest) {
		try {
			InputStream in = new FileInputStream(src);
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
}
