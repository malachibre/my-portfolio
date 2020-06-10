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
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Servlet handles retrieving comments from the datastore
 * and containing them in a JSON string. 
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    Query query = new Query("Comment").addSort("year", SortDirection.DESCENDING)
                                      .addSort("month", SortDirection.DESCENDING)
                                      .addSort("day", SortDirection.DESCENDING);

    PreparedQuery results = datastore.prepare(query);

    String amountParam = request.getParameter("comment-amount");

    /** Defailt value for commentAmount is 10 in case there's a parsing error. */
    int commentAmount = amountParam == null ? 10 : Integer.parseInt(amountParam);

    Iterable<Entity> newComments = results.asIterable(FetchOptions.Builder.withLimit(commentAmount));    
    
    /** /data page is update to contain JSON file of all imgaes up to commentAmount. */
    List<Comment> comments = results.asList(FetchOptions.Builder.withLimit(commentAmount))
        .stream()
        .map(entity -> {
            String title = (String) entity.getProperty("title");
            String text = (String) entity.getProperty("text");
            String imageUrl = (String) entity.getProperty("imageUrl");
            long day = (long) entity.getProperty("day");
            long month = (long) entity.getProperty("month");
            long year = (long) entity.getProperty("year");

            return new Comment(title, text, imageUrl, day, month, year);
        })
        .collect(Collectors.toList());
      
    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException { 

    Entity commentEntity = new Entity("Comment");

    commentEntity.setProperty("imageUrl", getUploadedFileUrl(request, "image"));    
    commentEntity.setProperty("day", Calendar.getInstance().get(Calendar.DATE));
    commentEntity.setProperty("month", Calendar.getInstance().get(Calendar.MONTH));
    commentEntity.setProperty("text", request.getParameter("text"));
    commentEntity.setProperty("title", request.getParameter("title"));
    commentEntity.setProperty("year", Calendar.getInstance().get(Calendar.YEAR));

    datastore.put(commentEntity);

    response.sendRedirect("/index.html");
  }
  
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException { 

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    removeEntities("Comment");
    removeEntities("__BlobInfo__");
    removeEntities("__BlobUploadSession__");

    response.sendRedirect("/index.html");
  }

  /** Removes entities from datastore based off a query string. */
  private void removeEntities(String query) {
    datastore.prepare(new Query(query))
             .asList(FetchOptions.Builder.withLimit(Integer.MAX_VALUE))
             .stream()
             .forEach(entity -> datastore.delete(entity.getKey()));                                          
  }
  
 /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
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
    }
  }
}
