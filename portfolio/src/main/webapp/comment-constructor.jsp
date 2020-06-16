<%--=
Copyright 2019 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>

<%-- The Java code in this JSP file runs on the server when the user navigates
     to the comment constructor page. --%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<% BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
   String uploadUrl = blobstoreService.createUploadUrl("/data"); %>

<!DOCTYPE html>
<meta charset="utf-8">
<title>My Portfolio</title>
<link rel="stylesheet" href="comment-style.css">
<div id="content-container">
  <form action="<%= uploadUrl %>" enctype="multipart/form-data"  method="post" >  
    <p>Title</p>
    <input class="form-input" type="text" name="title"></input>
    <p>Text</p>
    <textarea class="form-input" name="text" type="text"></textarea>
    <p>Upload an image (optional)</p>
    <input type="file" name="image">
    <input type="submit">Add Comment
  </form>
</div>
