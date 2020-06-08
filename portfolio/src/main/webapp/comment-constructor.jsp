<%--
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
   String uploadUrl = blobstoreService.createUploadUrl("/comment-handler"); %>

<!DOCTYPE html>
<meta charset="utf-8">
<title>My Portfolio</title>
<link rel="stylesheet" href="comment-style.css">
<div id="content-container">
  <form method="post"  enctype="multipart/form-data action"> 
    <p>Title</p>
    <input class="form-input" type="text">
    <p>Text</p>
    <textarea class="form-input" name="message"></textarea>
    <p>Upload an image (optional)</p>
    <input type="file" name="image">
    <input type="submit">Add Comment
  </form>
</div>
