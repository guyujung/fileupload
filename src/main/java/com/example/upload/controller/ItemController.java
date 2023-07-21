package com.example.upload.controller;

import com.example.upload.domain.Item;
import com.example.upload.domain.ItemRepository;
import com.example.upload.domain.UploadFile;
import com.example.upload.file.FileStore;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;



@Slf4j
@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    @GetMapping("/items/new")
    public String newItem(@ModelAttribute ItemForm form) {
        return "item-form";
    }

    //단일 파일 업로드
    @PostMapping("/items/new")
    public ResponseEntity<ResponseMessage> saveItem(@RequestParam("file") MultipartFile file,
                           @Valid ItemForm form) throws IOException {


        String message = "";
        try {
            form.setAttachFile(file);

            UploadFile attachFile = fileStore.storeFile(form.getAttachFile());


            //데이터베이스에 저장
            Item item = new Item();
            item.setItemName(form.getItemName());
            item.setAttachFile(attachFile);
            itemRepository.save(item);

            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    //다중 파일 업로드
    @PostMapping("/items/news")
    public ResponseEntity<ResponseMessage> saveItems(@RequestParam("files") List<MultipartFile> files,
                                                    @Valid ItemForm form) throws IOException {


        String message = "";
        try {

            form.setImageFiles(files);

            List<UploadFile> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

            //데이터베이스에 저장
            Item item = new Item();
            item.setItemName(form.getItemName());
            item.setImageFiles(storeImageFiles);
            itemRepository.save(item);

            message = "Uploaded the file successfully: "+item.getId();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }



    public class ResponseMessage {
        private String message;

        public ResponseMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }
/*
    //보여주기
    @GetMapping("/items/{id}")
    public String items(@PathVariable Long id) {
        Item item = itemRepository.findById(id);
        model.addAttribute("item", item);
        return "item-view";
    }
*/

    //이미지 보여주기
    @ResponseBody
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource>  downloadImage(@PathVariable String filename) throws MalformedURLException {
        Resource file= new UrlResource("file:" + fileStore.getFullPath(filename));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }



    //item을 접근할 수 있는 사용자만 다운로드 할 수 있음
    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId) throws MalformedURLException {
        Item item = itemRepository.findById(itemId);
        String storeFileName = item.getAttachFile().getStoreFileName();
        String uploadFileName = item.getAttachFile().getUploadFileName();

        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));

        log.info("uploadFileName={}", uploadFileName);

        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }


    //다운로드 기능+ 이미지 보여주기
    @GetMapping("/downloadFile/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename, HttpServletRequest request)  throws MalformedURLException {
        // Load file as Resource
        Resource resource= new UrlResource("file:" + fileStore.getFullPath(filename));
        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {

        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
