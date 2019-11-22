package com.google.photos.library.sample.demos;

public class UploadContext {

    private String credentialFilePath;

    private String syncDirectory;

    private String albumName;

    private int syncIntervalInSeconds;

    public UploadContext(String credentialFilePath, String syncDirectory, String albumName, int syncIntervalInSeconds) {
        this.credentialFilePath = credentialFilePath;
        this.syncDirectory = syncDirectory;
        this.albumName = albumName;
        this.syncIntervalInSeconds = syncIntervalInSeconds;
    }

    public String getCredentialFilePath() {
        return credentialFilePath;
    }

    public String getSyncDirectory() {
        return syncDirectory;
    }

    public int getSyncIntervalInSeconds() {
        return syncIntervalInSeconds;
    }

    public String getAlbumName() {
        return albumName;
    }
}
