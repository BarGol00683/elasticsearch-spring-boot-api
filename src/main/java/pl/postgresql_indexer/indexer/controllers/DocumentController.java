package pl.postgresql_indexer.indexer.controllers;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.postgresql_indexer.indexer.domain.Document;
import pl.postgresql_indexer.indexer.services.UserService;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class DocumentController {

    @Autowired
    UserService userService;

    @PostMapping(value = "/document")
    public ResponseEntity<String> addDocuemnt( @RequestBody Document document) throws Exception {
        String result = userService.createDocument(document);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/document/find/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDocuments() throws Exception {
        JSONArray list = new JSONArray(userService.getDocuments());
        return new ResponseEntity<>(list.toString(), HttpStatus.OK);
    }

    @GetMapping(value = "/document/find/phrase/{phrase}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDocuments(@PathVariable("phrase") String phrase) throws Exception {
        return new ResponseEntity<>(userService.getDocuments(phrase), HttpStatus.OK);
    }
}