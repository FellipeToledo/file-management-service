package com.azvtech.file_management.storage;

import com.azvtech.file_management.exception.StorageException;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class GridFsService {
    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    public GridFsService(GridFsTemplate gridFsTemplate, GridFsOperations gridFsOperations) {
        this.gridFsTemplate = gridFsTemplate;
        this.gridFsOperations = gridFsOperations;
    }

    public String storeFile(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            ObjectId id = gridFsTemplate.store(
                    inputStream,
                    file.getOriginalFilename(),
                    file.getContentType()
            );
            return id.toString();
        } catch (IOException e) {
            throw new StorageException("Failed to store file in GridFS", e);
        }
    }

    public InputStream getFileStream(String id) throws IOException {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        return gridFsOperations.getResource(file).getInputStream();
    }

    public void deleteFile(String id) {
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(id)));
    }
}
