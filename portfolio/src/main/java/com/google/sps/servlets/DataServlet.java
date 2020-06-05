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


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

  private final ArrayList<String> comments = new ArrayList<>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("year", SortDirection.DESCENDING)
                                      .addSort("month", SortDirection.DESCENDING)
                                      .addSort("day", SortDirection.DESCENDING);

    PreparedQuery results = datastore.prepare(query);

    String amountParam = request.getParameter("comment-amount");
    int commentAmount = amountParam == null ? 10 : Integer.parseInt(amountParam);

    List<Comment> comments = new ArrayList<>();
    Iterable<Entity> newComments = results.asIterable(FetchOptions.Builder.withLimit(commentAmount));    
    
    for (Entity entity : newComments) {

      String text = (String) entity.getProperty("text");
      long day = (long) entity.getProperty("day");
      long month = (long) entity.getProperty("month");
      long year = (long) entity.getProperty("year");
      
      Comment comment = new Comment(text, day, month, year);
      comments.add(comment);
    }
    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }


  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException { 

    Entity commentEntity = new Entity("Comment");

    commentEntity.setProperty("day", Calendar.getInstance().get(Calendar.DATE));
    commentEntity.setProperty("month", Calendar.getInstance().get(Calendar.MONTH));
    commentEntity.setProperty("text", request.getParameter("comment"));
    commentEntity.setProperty("year", Calendar.getInstance().get(Calendar.YEAR));

    datastore.put(commentEntity);

    response.sendRedirect("/index.html");
  }
  
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException { 

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query("Comment").setKeysOnly();
    PreparedQuery results = datastore.prepare(query);

    results.asList(FetchOptions.Builder.withLimit(Integer.MAX_VALUE)).stream().forEach(entity -> datastore.delete(entity.getKey()));

    response.sendRedirect("/index.html");
  }
}
