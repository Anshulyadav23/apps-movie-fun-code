package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class S3Store implements BlobStore {

    private AmazonS3Client client;
    private String bucketName;

    public S3Store(AmazonS3Client amazonS3Client, String s3BucketName) {
        this.client = amazonS3Client;
        this.bucketName = s3BucketName;
    }

    @Override
    public void put(Blob blob) throws IOException {
        System.out.println("S3Store.put start: Name=" + blob.name + "; bucketName=" + bucketName);
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentType(blob.contentType);
        client.putObject(bucketName, blob.name, blob.inputStream, metaData);
        System.out.println("S3Store.put start: " + blob.name);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        System.out.println("S3Store.get start: " + name);
        S3Object s3Obj = client.getObject(bucketName, name);
        if (s3Obj == null) return Optional.empty();
        Blob blob = new Blob(s3Obj.getKey(), s3Obj.getObjectContent(), s3Obj.getObjectMetadata().getContentType());
        return Optional.of(blob);
    }

    @Override
    public void deleteAll() {
        client.deleteObjects(new DeleteObjectsRequest(bucketName));
    }
}
