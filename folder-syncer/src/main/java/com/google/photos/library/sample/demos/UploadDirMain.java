package com.google.photos.library.sample.demos;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.photos.library.sample.factories.PhotosLibraryClientFactory;
import com.google.photos.library.v1.PhotosLibraryClient;

public class UploadDirMain {

    public static final String ALBUM_TITLE = "Photos album sample app";
    public static final String ALBUM_SAMPLE_IMAGE_RESOURCE = "/assets/album.png";

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadDirMain.class);

    private static UploadContext context;

    private static final List<String> REQUIRED_SCOPES =
            ImmutableList.of(
                    "https://www.googleapis.com/auth/photoslibrary.readonly",
                    "https://www.googleapis.com/auth/photoslibrary.appendonly",
                    "https://www.googleapis.com/auth/photoslibrary.sharing");

    /**
     * Runs the album sample. An optional path to a credentials file can be specified as the first
     * commandline argument.
     */
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        context = createContext();
        PhotosLibraryClient client = PhotosLibraryClientFactory.createClient(context.getCredentialFilePath(), REQUIRED_SCOPES);
        UploadApp app = new UploadApp(client, context);
        app.start();
    }

    private static UploadContext createContext() {
        String credentialFilePath = readEnvironmentVariableWithDefault(UploadConfigConstants.CREDENTIAL_FILE, UploadConfigDefaults.DEFAULT_CRED_FILE);
        String syncDirectory = readEnvironmentVariableWithDefault(UploadConfigConstants.SYNC_DIRECTORY, UploadConfigDefaults.DEFAULT_SYNC_FOLDER);
        String albumName = readEnvironmentVariableWithDefault(UploadConfigConstants.ALBUM_NAME, UploadConfigDefaults.DEFAULT_ALBUM_NAME);
        return new UploadContext(credentialFilePath, syncDirectory, albumName, UploadConfigDefaults.DEFAULT_SYNC_IN_SECONDS);
    }

    public static String readEnvironmentVariableWithDefault(String envVariableName, String defaultValue) {
        String value = System.getenv(envVariableName);
        if (value == null || "".equals(value)) {
            return defaultValue;
        }
        return System.getenv(envVariableName);
    }
}
