package main;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collections;

/**
 * @author dzimiks
 * Date: 13-04-2019 at 16:02
 */
public class DriveSample {

	/**
	 * Be sure to specify the name of your application. If the application name is {@code null} or
	 * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
	 */
	private static final String APPLICATION_NAME = "file-storage-drive";

	private static final String UPLOAD_FILE_PATH = "/Users/dzimiks/Desktop/projects/file-storage-drive/src/main/java/main/dzimiks.jpg";
	private static final String DIR_FOR_DOWNLOADS = "file-storage-drive";
	private static final java.io.File UPLOAD_FILE = new java.io.File(UPLOAD_FILE_PATH);

	/**
	 * Directory to store user credentials.
	 */
	private static final java.io.File DATA_STORE_DIR =
			new java.io.File(System.getProperty("user.home"), ".store/drive_sample");

	/**
	 * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
	 * globally shared instance across your application.
	 */
	private static FileDataStoreFactory dataStoreFactory;

	/**
	 * Global instance of the HTTP transport.
	 */
	private static HttpTransport httpTransport;

	/**
	 * Global instance of the JSON factory.
	 */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/**
	 * Global Drive API client.
	 */
	private static Drive drive;

	/**
	 * Authorizes the installed application to access user's protected data.
	 */
	private static Credential authorize() throws Exception {
		// load client secrets
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
				new InputStreamReader(DriveSample.class.getResourceAsStream("/client_secrets.json")));
		if (clientSecrets.getDetails().getClientId().startsWith("Enter")
				|| clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
			System.out.println(
					"Enter Client ID and Secret from https://code.google.com/apis/console/?api=drive "
							+ "into drive-cmdline-sample/src/main/resources/client_secrets.json");
			System.exit(1);
		}
		// set up authorization code flow
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, JSON_FACTORY, clientSecrets,
				Collections.singleton(DriveScopes.DRIVE_FILE)).setDataStoreFactory(dataStoreFactory)
				.build();
		// authorize
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}

	public static void main(String[] args) {
		Preconditions.checkArgument(
				!UPLOAD_FILE_PATH.startsWith("Enter ") && !DIR_FOR_DOWNLOADS.startsWith("Enter "),
				"Please enter the upload file path and download directory in %s", DriveSample.class);

		try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
			// authorization
			Credential credential = authorize();
			// set up the global Drive instance
			drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
					APPLICATION_NAME).build();

			// run commands

//			View.header1("Starting Resumable Media Upload");
//			File uploadedFile = uploadFile(false);
//
//			View.header1("Updating Uploaded File Name");
//			File updatedFile = updateFileWithTestSuffix(uploadedFile.getId());
//
//			View.header1("Starting Resumable Media Download");
//			downloadFile(false, updatedFile);

			View.header1("Starting Simple Media Upload");
			File uploadedFile = uploadFile(true);

//			View.header1("Starting Simple Media Download");
//			downloadFile(true, uploadedFile);

			View.header1("Success!");
			return;
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		System.exit(1);
	}

	/**
	 * Uploads a file using either resumable or direct media upload.
	 */
	private static File uploadFile(boolean useDirectUpload) throws IOException {
		File fileMetadata = new File();
		fileMetadata.setName(UPLOAD_FILE.getName());

		FileContent mediaContent = new FileContent("image/jpg", UPLOAD_FILE);

		Drive.Files.Create insert = drive.files().create(fileMetadata, mediaContent);
		MediaHttpUploader uploader = insert.getMediaHttpUploader();
		uploader.setDirectUploadEnabled(useDirectUpload);
		uploader.setProgressListener(new FileUploadProgressListener());
		return insert.execute();
	}

	/**
	 * Updates the name of the uploaded file to have a "drivetest-" prefix.
	 */
	private static File updateFileWithTestSuffix(String id) throws IOException {
		File fileMetadata = new File();
		fileMetadata.setName("drivetest-" + UPLOAD_FILE.getName());

		Drive.Files.Update update = drive.files().update(id, fileMetadata);
		return update.execute();
	}

	/**
	 * Downloads a file using either resumable or direct media download.
	 */
	private static void downloadFile(boolean useDirectDownload, File uploadedFile)
			throws IOException {
		// create parent directory (if necessary)
		java.io.File parentDir = new java.io.File(DIR_FOR_DOWNLOADS);
		if (!parentDir.exists() && !parentDir.mkdirs()) {
			throw new IOException("Unable to create parent directory");
		}
		OutputStream out = new FileOutputStream(new java.io.File(parentDir, uploadedFile.getName()));

		MediaHttpDownloader downloader =
				new MediaHttpDownloader(httpTransport, drive.getRequestFactory().getInitializer());
		downloader.setDirectDownloadEnabled(useDirectDownload);
		downloader.setProgressListener(new FileDownloadProgressListener());
		downloader.download(new GenericUrl(uploadedFile.getWebViewLink()), out);
	}
}
