package pl.postgresql_indexer.indexer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.postgresql_indexer.indexer.domain.Document;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Service
public class DocumentService {

    private ObjectMapper objectMapper;

    private RestHighLevelClient client;

    @Autowired
    public DocumentService(RestHighLevelClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void parseToDocument(){
        System.out.println("Started parsing CSV -> List<Document>");
        ObjectMapper mapper = new ObjectMapper();
        String csvFile = "D:\\data.csv";
        String csvSplitBy = ",";
        try (
                BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            int i = 0;
            List<Document> documents = new LinkedList<>();
            String line = "";
            while ((line = br.readLine()) != null) {
                if (i != 0) {
                    String[] inputs = line.split(csvSplitBy);
                    documents.add(new Document(inputs));
                }
                i++;
                if (i % 100000 == 0) {
                    long j = i;
                    BulkRequest request = new BulkRequest();
                    for (Document document : documents) {
                        String value = mapper.writeValueAsString(document);
                        request.add(new IndexRequest("bicycles").id(String.valueOf(j))
                                .source(value, XContentType.JSON));
                        j = j + 1L;
                    }
                    documents.clear();
                    BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
                    if (bulkResponse.hasFailures()) {
                        System.out.println("Iteration csv record: " + i + " | " + bulkResponse.status());
                    } else {
                        System.out.println("Iteration csv record: " + i + " | " + "OK");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public List<Document> getDocuments() throws IOException {
//
//        SearchRequest searchRequest = new SearchRequest().indices("bicycles");
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//        searchRequest.source(searchSourceBuilder);
//
//        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
//
//        SearchHits hits = searchResponse.getHits();
//        LinkedList<Document> documents = new LinkedList<>();
//
//        for (SearchHit hit : hits.getHits()) {
//            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//            documents.add(returnedDocument);
//        }
//
//        return documents;
//    }

//    public String getDocuments(String phrase) throws IOException {
//
//        String searchScriptQuery = "{\n" +
//                " \"query\": {\n" +
//                "    \"match\": {                                             \n" +
//                "      \"{{field}}\": {                                          \n" +
//                "        \"query\": \"{{value}}\",\n" +
//                "        \"fuzziness\": \"2\",\n" +
//                "        \"operator\":  \"or\"\n" +
//                "      }\n" +
//                "    }                                              \n" +
//                "  }\n" +
//                "}";
//
//        SearchTemplateRequest request = new SearchTemplateRequest();
//        request.setRequest(new SearchRequest("bicycles"));
//
//        request.setScriptType(ScriptType.INLINE);
//        request.setScript(searchScriptQuery);
//
//        Map<String, Object> scriptParams = new HashMap<>();
//
//        scriptParams.put("field", "description");
//        scriptParams.put("value", phrase);
//
//        request.setScriptParams(scriptParams);
//
//        SearchTemplateResponse searchTemplateResponse = client.searchTemplate(request, RequestOptions.DEFAULT);
//
//        SearchResponse searchResponse = searchTemplateResponse.getResponse();
//
//        return searchResponse.toString();
//    }
}
