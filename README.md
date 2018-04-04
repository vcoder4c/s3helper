# s3helper
This is a small library for supporting to handle the process when we works with Amazon S3 service

## 1. Why?
Amazon S3 is a good cloud service for file storage. But it's required a little knowledge about their APIs or SDKS to implement. 

## 2. What s3helper support ?
I try to wrap AWS S3 Java SDK to support when I need to work with S3. At this moment, I support two methods:

- Get Upload Form Params for client side, and client will use these to upload resource direct to S3
- Get Resource URL for retrieve the resource

## 3. What next?
- Get Upload Form Params for each kinds of resources (photo, audio, video, etc..) --> DONE (v0.0.3)
- Mark a resource as public if required --> DONE (v0.0.3)

## 4. How to use?
I have already published this library on maven central repository, so you can use it very easily. Please visit [this place](https://mvnrepository.com/artifact/me.vcoder/s3helper/0.0.3) for integrating with your project.
