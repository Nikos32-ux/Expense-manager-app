package com.example.ExpenseTracker.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.ExpenseTracker.exception.CloudinaryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImageProfile(MultipartFile file, String email){
       try{
           Map uploadResult = cloudinary.uploader().upload(file.getInputStream(),
                   ObjectUtils.asMap("folder", "user_profiles", "transformation", "c_fill,g_face,w_300,h_300,q_auto,f_auto"
                   ));

           return (String) uploadResult.get("secure_url");
       }
       catch(IOException ex){
           throw new CloudinaryException("Cloudinary failed reading the image bytes");
       }
        }

    public String updloadCsvFile(Path filePath, Long userId){
        try{
            Map uploadResult = cloudinary.uploader().upload(filePath.toFile(),
                    ObjectUtils.asMap(
                            "folder", "user_csv",
                            "resource_type", "raw",
                            "public_id", String.valueOf(userId),
                            "overwrite", true
                    ));
            return (String) uploadResult.get("secure_url");
        }
        catch(IOException ex){
            throw new CloudinaryException("Cloudinary failed reading the file bytes");
        }
    }
    }

