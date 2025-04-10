package ru.emiren.tg_news.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.emiren.tg_news.Service.AI.AiService;

import java.io.IOException;
import java.io.InputStream;

@Controller
@Slf4j
public class CdnController {

    private final Resource imgNewsCrownResource;

    @Autowired
    public CdnController(ClassPathResource imgNewsCrownClassPathResource) {
        this.imgNewsCrownResource = imgNewsCrownClassPathResource;
    }

    @GetMapping("image/newspaper_crown")
    public ResponseEntity<Resource> imageCrown() {
        try {
            Resource resource = new UrlResource(imgNewsCrownResource.getURI());
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.IMAGE_PNG).body(resource);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}

