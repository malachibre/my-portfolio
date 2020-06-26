// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;


import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ImagesServiceFailureException; 
import com.google.appengine.api.images.ServingUrlOptions;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

/** 
 * This class contains the fields and methods
 * required to use the Comment feature.
 */
 public final class CommentService {

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
  public List<Comment> getComments(int limit) {

    Query query = new Query("Comment").addSort("postedTime", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    // Data page is updated to contain JSON file of all images up to limit. 
    List<Comment> comments = results.asList(FetchOptions.Builder.withLimit(limit))
        .stream()
        .map(entity -> {
            
            String postedTime = (String) entity.getProperty("postedTime");
            String email = (String) entity.getProperty("email");
            String title = (String) entity.getProperty("title");
            String text = (String) entity.getProperty("text");
            String imageUrl = (String) entity.getProperty("imageUrl");

            return new Comment(postedTime, email, title, text, imageUrl);
        })
        .collect(Collectors.toList());
    return comments;

  }
  
  /** Adds the comment to the datastore. */
  public void saveComment(Comment comment) {
    Entity commentEntity = new Entity("Comment");

    commentEntity.setProperty("postedTime", comment.getPostedDateTime());
    commentEntity.setProperty("email", comment.getEmail());
    commentEntity.setProperty("title", comment.getTitle());
    commentEntity.setProperty("text", comment.getText());  
    commentEntity.setProperty("imageUrl", comment.getImageUrl());
    
    datastore.put(commentEntity);
  }

  // Removes Comments and Blob Information from datastore. 
  public void deleteAllComments() {
    removeEntities("Comment");
    removeEntities("__BlobInfo__");
    removeEntities("__BlobUploadSession__");
  } 

 // Removes entities from datastore based off a query string. 
  private void removeEntities(String query) {
    datastore.prepare(new Query(query))
             .asList(FetchOptions.Builder.withLimit(Integer.MAX_VALUE))
             .stream()
             .forEach(entity -> datastore.delete(entity.getKey()));                                          
  }

  /**
  * This method was taken copied and pasted from the walkthrough on blobstore at:
  * github.com/googleinterns/step/blob/master/walkthroughs/week-4-libraries/
  * 
  * Returns a URL that points to the uploaded file, or null if the user didn't upload a file.
  */
  public String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // We could check the validity of the file here, e.g. to make sure it's an image file
    // https://stackoverflow.com/q/10779564/873165

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    } catch (ImagesServiceFailureException e){
      return imagesService.getServingUrl(options);
    }
  }

 }
  