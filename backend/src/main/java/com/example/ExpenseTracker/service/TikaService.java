package com.example.ExpenseTracker.service;
import com.example.ExpenseTracker.exception.FileReadException;
import com.example.ExpenseTracker.exception.InvalidFileTypeException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class TikaService {
    private final Tika tika = new Tika();

    public void validateFile(MultipartFile file){
        try(InputStream inputStream = file.getInputStream()){

           String mime =  tika.detect(inputStream);
           if(!mime.startsWith("image/")){
               throw new InvalidFileTypeException("Invalid file type only images are accepted");
           }
        }
        catch(IOException ex){
            throw new FileReadException("Unable to read the file", ex);
        }
    }
}
