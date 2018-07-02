# s3helper
This is a small library for supporting to handle the process when we works with Amazon S3 service

## 1. Why?
Amazon S3 is a good cloud service for file storage. But it's required a little knowledge about their APIs or SDKS to implement. 

## 2. What s3helper support ?
I try to wrap AWS S3 Java SDK to support when I need to work with S3. At this moment, this library can offer:

- Get Upload Form Params for client side, and client can use these params to upload resource to S3 directly
- Support to upload directly from input stream in case we want the server side take care this part.
- For uploading, support public and private. With public, the resource will be configured as public automatically. With private uploading, the client side have to request server side to generate the link for accessing.
- Support for querying the list of resource on specific directory in the bucket.
- Support to delete resource via its file name.

## 3. What next?
- To be decided.

## 4. How to use?
I have already published this library on maven central repository, so you can use it very easily. Please visit [this place](https://mvnrepository.com/artifact/me.vcoder/s3helper/0.0.5) for integrating with your project.
