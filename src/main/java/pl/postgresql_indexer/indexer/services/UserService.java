package pl.postgresql_indexer.indexer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.postgresql_indexer.indexer.domain.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private ObjectMapper objectMapper;

    private RestHighLevelClient client;

    @Autowired
    public UserService(RestHighLevelClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public String createDocument(Document document) throws Exception {

        JSONObject src = new JSONObject();
        src.put("url", document.getUrl());
        src.put("description", document.getDescription());
        src.put("title", document.getTitle());

        IndexRequest request = new IndexRequest("newdatabase").source(src.toString(),XContentType.JSON);

        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

        return indexResponse
                .getResult()
                .name();
    }

    public List<Document> getDocuments() throws IOException {

        SearchRequest searchRequest = new SearchRequest().indices("newdatabase");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits hits = searchResponse.getHits();
        LinkedList<Document> documents = new LinkedList<>();

        for (SearchHit hit : hits.getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Document returnedDocument = new Document((String)sourceAsMap.get("url"),
                                                    (String)sourceAsMap.get("title"),
                                                    (String)sourceAsMap.get("description"));
            documents.add(returnedDocument);
        }

        return documents;
    }

    public String getDocuments(String phrase) throws IOException {

        String searchScriptQuery = "{\n" +
                " \"query\": {\n" +
                "    \"match\": {                                             \n" +
                "      \"{{field}}\": {                                          \n" +
                "        \"query\": \"{{value}}\",\n" +
                "        \"fuzziness\": \"2\",\n" +
                "        \"operator\":  \"or\"\n" +
                "      }\n" +
                "    }                                              \n" +
                "  }\n" +
                "}";

        SearchTemplateRequest request = new SearchTemplateRequest();
        request.setRequest(new SearchRequest("newdatabase"));

        request.setScriptType(ScriptType.INLINE);
        request.setScript(searchScriptQuery);

        Map<String, Object> scriptParams = new HashMap<>();

        scriptParams.put("field", "description");
        scriptParams.put("value", phrase);

        request.setScriptParams(scriptParams);

        SearchTemplateResponse searchTemplateResponse = client.searchTemplate(request, RequestOptions.DEFAULT);

        SearchResponse searchResponse = searchTemplateResponse.getResponse();

        return searchResponse.toString();
    }
}
