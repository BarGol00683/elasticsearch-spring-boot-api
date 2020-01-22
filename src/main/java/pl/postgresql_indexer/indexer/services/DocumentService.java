package pl.postgresql_indexer.indexer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.search.TotalHits;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Min;
import org.elasticsearch.search.aggregations.metrics.MinAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.postgresql_indexer.indexer.domain.Document;

import javax.annotation.PostConstruct;
import java.io.*;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class DocumentService {

    private ObjectMapper objectMapper;

    private RestHighLevelClient client;

    @Autowired
    public DocumentService(RestHighLevelClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

//    @PostConstruct
//    public void parseToDocument() throws IOException {
//        System.out.println("Checking if indices exists...");
//        GetIndexRequest checkIndiceRequest = new GetIndexRequest("bicycles");
//        boolean exists = client.indices().exists(checkIndiceRequest, RequestOptions.DEFAULT);
//        long start = System.nanoTime();
//        if (!exists) {
//            System.out.println("Indices empty...");
//            System.out.println("Started parsing CSV -> List<Document>");
//            ObjectMapper mapper = new ObjectMapper();
//            String csvFile = "D:\\data.csv";
//            String csvSplitBy = ",";
//            try (
//                    BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
//                int i = 0;
//                List<Document> documents = new LinkedList<>();
//                String line = "";
//                while ((line = br.readLine()) != null) {
//                    if (i != 0) {
//                        String[] inputs = line.split(csvSplitBy);
//                        documents.add(new Document(inputs));
//                    }
//                    i++;
//                    if (i % 1000 == 0) {
//                        long j = i;
//                        BulkRequest request = new BulkRequest();
//                        for (Document document : documents) {
//                            String value = mapper.writeValueAsString(document);
//                            request.add(new IndexRequest("bicycles").id(String.valueOf(j))
//                                    .source(value, XContentType.JSON));
//                            j = j + 1L;
//                        }
//                        documents.clear();
//                        BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
//                        if (bulkResponse.hasFailures()) {
//                            System.out.println("Iteration csv record: " + i + " | " + bulkResponse.status());
//                        } else {
//                            System.out.println("Iteration csv record: " + i + " | " + "OK");
//                        }
//                    }
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }else {
//            System.out.println("Indice already exists");
//        }
//        long elapsedTime = System.nanoTime() - start;
//        long convert = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
//        long minutes = convert / 60;
//        long lastSeconds = (minutes * 60 - convert);
//
//        System.out.println("Time: " + minutes + ":" + lastSeconds);
//    }

    private String getDayOfWeek2(int value) {
        String day = "";
        switch (value) {
            case 1:
                day = "Sunday";
                break;
            case 2:
                day = "Monday";
                break;
            case 3:
                day = "Tuesday";
                break;
            case 4:
                day = "Wednesday";
                break;
            case 5:
                day = "Thursday";
                break;
            case 6:
                day = "Friday";
                break;
            case 7:
                day = "Saturday";
                break;
        }
        return day;
    }

    @PostConstruct
    public void getDocumentsBetweenDates() throws IOException {
        //Create blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        //Create a blank sheet
        XSSFSheet spreadsheet = workbook.createSheet( " Bicycles rent info");

        //Create row object
        XSSFRow row;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone( ZoneId.systemDefault() );
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone( ZoneId.systemDefault() );

        Map<String, Object[]> rentedBicyclesData = new TreeMap<>();
        long rowid = 1;

        CountRequest countRequest = new CountRequest();
        countRequest.indices("bicycles");
        RangeQueryBuilder queryDate;
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        CountResponse countResponse;

        Instant start = ZonedDateTime.parse("2014-06-30T20:00:00Z").toInstant();
        Instant maxDate = ZonedDateTime.parse("2015-06-30T20:00:00Z").toInstant();
        while (start.isBefore(maxDate)) {
            start = start.plus(1, ChronoUnit.HOURS);
            queryDate = QueryBuilders.rangeQuery("starttime.epochSecond").from(start.getEpochSecond()).to(start.plus(1, ChronoUnit.HOURS).getEpochSecond());
            sourceBuilder.query(queryDate);
            countRequest.source(sourceBuilder);
            countResponse = client.count(countRequest, RequestOptions.DEFAULT);
            long count = countResponse.getCount();
            //System.out.println("ILOSC STARTÓW " + start + " | " + count);

            Object object = new Object[] {
                    dateFormatter.format(start), getDayOfWeek2(start.atZone(ZoneId.systemDefault()).getDayOfWeek().getValue()), timeFormatter.format(start), count
            };
            rentedBicyclesData.put(String.valueOf(rowid), new Object[] {
                    dateFormatter.format(start), getDayOfWeek2(start.atZone(ZoneId.systemDefault()).getDayOfWeek().getValue()), timeFormatter.format(start), count
            });
            rowid++;
        }
        Set <String> keyid = rentedBicyclesData.keySet();
        int rowID = 0;
        for (String key : keyid) {
            row = spreadsheet.createRow(rowID++);
            Object [] objectArr = rentedBicyclesData.get(key);
            int cellid = 0;

            for (Object obj : objectArr){
                Cell cell = row.createCell(cellid++);
                if (obj instanceof Long) {
                    cell.setCellValue(String.valueOf(obj));
                } else {
                    cell.setCellValue((String)obj);
                }
            }

        }

        //Write the workbook in file system
        FileOutputStream out = new FileOutputStream(
                new File("D:\\Writesheet2.xlsx"));

        workbook.write(out);
        out.close();
        System.out.println("Writesheet.xlsx written successfully");

    }

//    @PostConstruct
//    public void deleteBicycleIndices() throws IOException {
//        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("bicycles");
//        AcknowledgedResponse deleteIndexResponse = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
//        if (deleteIndexResponse.isAcknowledged()) {
//            System.out.println("Usunięto bicycles");
//        }
//    }

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
