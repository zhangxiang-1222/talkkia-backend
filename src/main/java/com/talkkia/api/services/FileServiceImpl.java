package com.talkkia.api.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {

    private final Path rootLocation;
    private final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    public FileServiceImpl() throws IOException {
        this.rootLocation = Paths.get("upload-dir");
        Files.createDirectories(this.rootLocation.toAbsolutePath().resolve(Paths.get("abc/def/ghi/")).normalize().toAbsolutePath());
    }

    @Override
    public Boolean store(MultipartFile file, String dir) {
        logger.info(dir);
        try {
            Path destinationFile;
            if (dir.length() > 11) {
                destinationFile = this.rootLocation.resolve(
                        Paths.get(dir + file.getOriginalFilename()))
                        .normalize().toAbsolutePath();
            } else {
                destinationFile = this.rootLocation.resolve(
                        Paths.get(dir + "profile.png"))
                        .normalize().toAbsolutePath();
            }
            logger.info(destinationFile.toString());
            try (InputStream inputStream = file.getInputStream()) {
                if (!Files.isDirectory(this.rootLocation.toAbsolutePath().resolve(Paths.get(dir)).normalize().toAbsolutePath())) {
                    logger.info(dir + " is not a directory");
                    Files.createDirectories(this.rootLocation.toAbsolutePath().resolve(Paths.get(dir)).normalize().toAbsolutePath());
                }
                logger.info("files copy start");
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
                logger.info("files copy end");
                return true;
            }
        } catch (IOException e) {
            logger.error("error",e);
            //logger.error(e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            return null;
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Map loadAsResource(String filename) {
        Map<String, Object> response = new HashMap<>();
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                response.put("success", true);
                response.put("status", "fe01");
                response.put("contentType", Files.probeContentType(file));
                response.put("data", resource);
                return response;
            } else {
                response.put("success", false);
                response.put("status", "fe02");
                response.put("message", "file doesn't exist.");
                return response;

            }
        } catch (MalformedURLException e) {
            logger.error(e.getMessage());
            response.put("success", false);
            response.put("status", "fe03");
            response.put("message", "wrong file name.");
            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            response.put("success", false);
            response.put("status", "fe03");
            response.put("message", "File System Error.");
            return response;
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {

        }
    }
}
