package com.talkkia.api.controller;

import static com.talkkia.api.controller.UserController.CLIENT_TOKEN;
import com.talkkia.api.services.FileService;
import com.talkkia.api.services.UserService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

/*this code is written by another developer and need to review to map and match 
to our code convention and standard.*/
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;
    @Autowired
    UserService userService;
    
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(@RequestHeader Map<String, String> headers, @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals(CLIENT_TOKEN) && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                if (file.isEmpty()) {
                    response.put("success", false);
                    response.put("status", "fe01");
                    response.put("message", "File is not available!");
                    return ResponseEntity.badRequest().body(response);
                }
                String dir = headers.get("userid");
                logger.info(file.getContentType());
                if (file.getContentType().contains("image")) {
                    dir = dir + "/images/";
                } else if (file.getContentType().contains("audio")) {
                    dir = dir + "/audios/";
                } else if (file.getContentType().contains("video")) {
                    dir = dir + "/videos/";
                } else {
                    dir = dir + "/docs/";
                }
                if (fileService.store(file, dir)) {
                    response.put("success", true);
                    response.put("status", "fe02");
                    response.put("fileUrl", dir + file.getOriginalFilename());
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("status", "fe00");
                    response.put("message", "File can't be saved!");
                    return ResponseEntity.unprocessableEntity().body(response);
                }
            }
            response.put("success", false);
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/changeprofile")
    public ResponseEntity<Object> changeProfilePicture(@RequestHeader Map<String, String> headers,
            @RequestParam("file") MultipartFile file
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals(CLIENT_TOKEN) && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                if (file.isEmpty()) {
                    response.put("success", false);
                    response.put("status", "fe01");
                    response.put("message", "File is not available!");
                    return ResponseEntity.badRequest().body(response);
                }

                if (!file.getContentType().contains("image")) {
                    response.put("success", false);
                    response.put("status", "fe04");
                    response.put("message", "File must be in image format.");
                    return ResponseEntity.badRequest().body(response);
                }

                String dir = headers.get("userid") + "/";
                if (fileService.store(file, dir)) {
                    response.put("success", true);
                    response.put("status", "fe02");
                    response.put("fileUrl", dir + "profile.png");
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("status", "fe00");
                    response.put("message", "File can't be saved!");
                    return ResponseEntity.unprocessableEntity().body(response);
                }
            }
            response.put("success", false);
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/uploadall")
    public ResponseEntity<Object> uploadAllFiles(@RequestHeader Map<String, String> headers, @RequestParam("file") MultipartFile[] files) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals(CLIENT_TOKEN) && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                List<String> urls = new ArrayList<>();
                Arrays.asList(files).stream().forEach(file -> {
                    if (fileService.store(file, "files")) {
                        urls.add("https://" + headers.get("host") + "/api/file/files/" + file.getOriginalFilename());
                    }
                });

                return ResponseEntity.ok(urls);
            }
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Object> downloadFile(@RequestHeader Map<String, String> headers, @RequestParam String file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (headers.getOrDefault("client-token", "null").equals(CLIENT_TOKEN) && userService.authenticateUser(Long.parseLong(headers.getOrDefault("userid", "null")), headers.getOrDefault("token", "111"))) {
                Map fileObj;
                if (headers.getOrDefault("compress", "false").equals("false")) {
                    fileObj = fileService.loadAsResource(file);
                } else {
                    if (file.contains("/profile.png")) {
                        fileObj = fileService.loadAsResource(file.replace("profile", "compress"));
                    } else {
                        fileObj = fileService.loadAsResource(file.replace("/images/", "/images/thumbnails/"));
                    }
                }
                if (fileObj.get("success").equals(Boolean.FALSE)) {
                    return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(fileObj);
                }
                if (file.contains("/profile")) {
                    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(fileObj.get("data"));
                }
                if (file.contains("/audios/")) {
                    return ResponseEntity.ok().contentType(MediaType.asMediaType(MimeType.valueOf(fileObj.get("contentType").toString()))).body(fileObj.get("data"));
                }
                if (file.contains("images")) {
                    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(fileObj.get("data"));
                }
                if (file.contains("docs")) {
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(fileObj.get("data"));
                }
                if (file.contains("videos")) {
                    return ResponseEntity.ok().contentType(MediaType.parseMediaType("video/mp4")).body(fileObj.get("data"));
                }
            }
            response.put("success", false);
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON).body(response);
        }
    }

    @GetMapping("/files")
    public ResponseEntity<Object> getListFiles(@RequestHeader Map<String, String> headers, Model model) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.getOrDefault("client-token", "null").equals(CLIENT_TOKEN) && userService.authenticateUser(Long.parseLong(headers.getOrDefault("userid", "null")), headers.getOrDefault("token", "111"))) {
                model.addAttribute("files", fileService.loadAll().map(
                        path -> MvcUriComponentsBuilder.fromMethodName(FileController.class, "serveFile",
                                path.getFileName().toString(), headers).build().toUri().toString()
                ).collect(Collectors.toList()));
                return ResponseEntity.ok(model.getAttribute("files"));
            }
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (Exception e) {
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON).body(response);
        }
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Object> serveFile(@PathVariable String filename, @RequestHeader Map<String, String> headers) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals(CLIENT_TOKEN) && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(fileService.loadAsResource(filename));
            }
            response.put("success", false);
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }

        /*
        Resource file = (Resource) fileService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
         */
    }
}
