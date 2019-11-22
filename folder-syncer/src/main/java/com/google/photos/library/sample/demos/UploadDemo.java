package com.google.photos.library.sample.demos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.rpc.ApiException;
import com.google.common.collect.ImmutableList;
import com.google.photos.library.sample.factories.PhotosLibraryClientFactory;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.proto.AlbumPosition;
import com.google.photos.library.v1.proto.BatchCreateMediaItemsResponse;
import com.google.photos.library.v1.proto.ListAlbumsRequest;
import com.google.photos.library.v1.proto.NewMediaItem;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.library.v1.util.AlbumPositionFactory;
import com.google.photos.library.v1.util.NewMediaItemFactory;
import com.google.photos.types.proto.Album;

public class UploadDemo {

    public static final String ALBUM_TITLE = "Photos album sample app";
    public static final String ALBUM_SAMPLE_IMAGE_RESOURCE = "/assets/album.png";
    public static final String WEDDING_ALBUM_NAME = "Wedding auto";

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadDemo.class);

    private static final List<String> REQUIRED_SCOPES =
            ImmutableList.of(
                    "https://www.googleapis.com/auth/photoslibrary.readonly",
                    "https://www.googleapis.com/auth/photoslibrary.appendonly",
                    "https://www.googleapis.com/auth/photoslibrary.photoslibrary",
                    "https://www.googleapis.com/auth/photoslibrary.sharing");

    /**
     * Runs the album sample. An optional path to a credentials file can be specified as the first
     * commandline argument.
     */
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        // If the first argument is set, it contains the path to the credentials file.
        Optional<String> credentialsFile = Optional.empty();

        if (args.length > 0) {
            credentialsFile = Optional.of(args[0]);
        }

        String credentialsPath = "/Users/laszlonardai/Downloads/client_secret_1065020459899-stj6quehpc22rqf0i85qoqhuslnvseju.apps.googleusercontent.com.json";
        PhotosLibraryClient client = PhotosLibraryClientFactory.createClient(credentialsPath, REQUIRED_SCOPES);

        if (!isAlbumExists(client)) {
            createAlbum(client, WEDDING_ALBUM_NAME);
        }

        Album album = findAlbumByTitle(client, WEDDING_ALBUM_NAME);

        String uploadToken = uploadBytes(client);
        LOGGER.info("Recieved upload token: {}", uploadToken);

        NewMediaItem newMediaItem = NewMediaItemFactory
                .createNewMediaItem(uploadToken, "Testing is my life");
        List<NewMediaItem> newItems = Arrays.asList(newMediaItem);

        LOGGER.info("Created media Item list: {}", newItems);

        try {
            // Create new media items in a specific album, positioned after a media item
            AlbumPosition positionInAlbum = AlbumPositionFactory.createFirstInAlbum();
            BatchCreateMediaItemsResponse response = client
                    .batchCreateMediaItems(album.getId(), newItems, positionInAlbum);
            // Check the response
        } catch (ApiException e) {
            LOGGER.error("ApiException happened when creating media", e);
        }
    }

    public static Album findAlbumByTitle(PhotosLibraryClient client, String title) {
        ListAlbumsRequest request = ListAlbumsRequest.getDefaultInstance();
        final ListAlbumsSupplier listAlbumsSupplier = new ListAlbumsSupplier(client, request);
        List<Album> listOfAlbums = listAlbumsSupplier.get();
        return listOfAlbums.stream().filter(album -> album.getTitle().equals(title)).findFirst().get();
    }

    public static boolean isAlbumExists(PhotosLibraryClient client) {
        ListAlbumsRequest request = ListAlbumsRequest.getDefaultInstance();
        final ListAlbumsSupplier listAlbumsSupplier = new ListAlbumsSupplier(client, request);
        List<Album> listOfAlbums = listAlbumsSupplier.get();
        return listOfAlbums.stream().filter(album -> album.getTitle().equals(WEDDING_ALBUM_NAME)).findFirst().isPresent();
    }


    public static void createAlbum(PhotosLibraryClient client, String name) {
        try {
            LOGGER.info("Creating album because it does not exist with name {}", name);
            Album createdAlbum = client.createAlbum(name);
            // The createdAlbum object contains properties of an album
            String productUrl = createdAlbum.getProductUrl();
            // coverPhotoBaseUrl shouldn't be used as is. Append parameters to base URLs before use
            String albumCoverImage = createdAlbum.getCoverPhotoBaseUrl() + "=w2048-h1024";
            boolean isWriteable = createdAlbum.getIsWriteable();
        } catch (ApiException e) {
            LOGGER.error("ApiException happened when creating album", e);
        }
    }

    public static String uploadBytes(PhotosLibraryClient client) {
        try {
            // Create a new upload request
            // Specify the filename that will be shown to the user in Google Photos
            // and the path to the file that will be uploaded
            UploadMediaItemRequest uploadRequest =
                    UploadMediaItemRequest.newBuilder()
                            //filename of the media item along with the file extension
                            .setFileName("UploadedFile")
                            .setDataFile(new RandomAccessFile("/Users/laszlonardai/Downloads/pic/IMG_8789.JPG", "r"))
                            .build();
            // Upload and capture the response
            UploadMediaItemResponse uploadResponse = client.uploadMediaItem(uploadRequest);
            if (uploadResponse.getError().isPresent()) {
                // If the upload results in an error, handle it
                UploadMediaItemResponse.Error error = uploadResponse.getError().get();
            } else {
                // If the upload is successful, get the uploadToken
                return uploadResponse.getUploadToken().get();
                // Use this upload token to create a media item
            }
        } catch (ApiException | FileNotFoundException e) {
            LOGGER.error("ApiException happened or File problem", e);
        }
        return null;
    }

}
