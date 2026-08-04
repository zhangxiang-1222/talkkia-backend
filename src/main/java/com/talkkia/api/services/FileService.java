package com.talkkia.api.services;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    void init();

    Boolean store(MultipartFile file, String dir);

    Stream<Path> loadAll();

    Path load(String filename);

    Map loadAsResource(String filename);

    void deleteAll();
}
