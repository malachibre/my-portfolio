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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.ZonedDateTime;
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
public final class DataServlet extends HttpServlet {

  private final CommentService commentService = new CommentService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String amountParam = request.getParameter("comment-amount");

    // Default value for commentAmount is 10 in case there's a parsing error. 
    int commentAmount = amountParam == null ? 10 : Integer.parseInt(amountParam);
    List<Comment> comments = commentService.getComments(commentAmount); 
    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException { 

    ZonedDateTime dateTime = ZonedDateTime.now();

    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);
    String postedTime = dateTime.format(formatter) + " UTC";

    UserService userService = UserServiceFactory.getUserService();
    String email = userService.getCurrentUser().getEmail();

    Comment comment = new Comment(postedTime, 
                                  email, 
                                  request.getParameter("title"), 
                                  request.getParameter("text"), 
                                  commentService.getUploadedFileUrl(request, "image"));

    commentService.saveComment(comment);

    response.sendRedirect("/index.html");
  }
  
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException { 

    commentService.deleteAllComments();

    response.sendRedirect("/index.html");
  }

}
