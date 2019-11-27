package com.google.photos.library.sample.demos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.rpc.ApiException;
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

public class UploadApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadDirMain.class);

    private UploadContext context;

    private PhotosLibraryClient client;

    public UploadApp(PhotosLibraryClient client, UploadContext context) {
        this.client = client;
        this.context = context;
    }

    public void start() {
        if (!isAlbumExists(client)) {
            createAlbum(client, context.getAlbumName());
        }

        Album album = findAlbumByTitle(client, context.getAlbumName());

        List<String> files = getFilesFromDirectory(context.getSyncDirectory());

        LOGGER.info("List of files found in directory {}", files);

        files.stream()
                .map(filePath -> uploadBytes(client, filePath))
                .map(token -> createMediaItemFromUploadToken(token))
                .forEach(newMediaItem -> positionMediaItemInAlbum(client, album, newMediaItem));
    }

    public String uploadBytes(PhotosLibraryClient client, String pathOfFile) {
        LOGGER.info("Uploading file with path: {}", pathOfFile);
        try {
            // Create a new upload request
            // Specify the filename that will be shown to the user in Google Photos
            // and the path to the file that will be uploaded
            UploadMediaItemRequest uploadRequest =
                    UploadMediaItemRequest.newBuilder()
                            //filename of the media item along with the file extension
                            .setFileName("UploadedFile")
                            .setDataFile(new RandomAccessFile(pathOfFile, "r"))
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

    public Album findAlbumByTitle(PhotosLibraryClient client, String title) {
        ListAlbumsRequest request = ListAlbumsRequest.getDefaultInstance();
        final ListAlbumsSupplier listAlbumsSupplier = new ListAlbumsSupplier(client, request);
        List<Album> listOfAlbums = listAlbumsSupplier.get();
        return listOfAlbums.stream().filter(album -> album.getTitle().equals(title)).findFirst().get();
    }

    public boolean isAlbumExists(PhotosLibraryClient client) {
        ListAlbumsRequest request = ListAlbumsRequest.getDefaultInstance();
        final ListAlbumsSupplier listAlbumsSupplier = new ListAlbumsSupplier(client, request);
        List<Album> listOfAlbums = listAlbumsSupplier.get();
        return listOfAlbums.stream().filter(album -> album.getTitle().equals(context.getAlbumName())).findFirst().isPresent();
    }

    public NewMediaItem createMediaItemFromUploadToken(String uploadToken) {
        LOGGER.info("Recieved upload token: {}", uploadToken);
        return NewMediaItemFactory.createNewMediaItem(uploadToken, "Testing is my life");
    }

    public void positionMediaItemInAlbum(PhotosLibraryClient client, Album album, NewMediaItem newMediaItem) {
        List<NewMediaItem> newItems = Arrays.asList(newMediaItem);
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


    public List<String> getFilesFromDirectory(String directory) {
        try (Stream<Path> walk = Files.walk(Paths.get(directory))) {

            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());

            result.forEach(System.out::println);
            return result;
        } catch (IOException e) {
            LOGGER.error("Can't read files from directory", e);
        }
        return new ArrayList<>();
    }

    public void createAlbum(PhotosLibraryClient client, String name) {
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
}