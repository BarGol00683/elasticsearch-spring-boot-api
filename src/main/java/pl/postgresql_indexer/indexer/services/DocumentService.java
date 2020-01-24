package pl.postgresql_indexer.indexer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.TotalHits;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Min;
import org.elasticsearch.search.aggregations.metrics.MinAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.postgresql_indexer.indexer.domain.Document;
import pl.postgresql_indexer.indexer.domain.Document2;
import pl.postgresql_indexer.indexer.domain.EpochInstant;

import javax.annotation.PostConstruct;
import java.io.*;
import java.lang.reflect.Field;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
//        GetIndexRequest checkIndiceRequest = new GetIndexRequest("bicycles2");
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
//                            request.add(new IndexRequest("bicycles2").id(document.getTrip_id())
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

//    @PostConstruct
//    public long getDocumentsBetweenDates() throws IOException, NoSuchFieldException {
//        //Create blank workbook
//        XSSFWorkbook workbook = new XSSFWorkbook();
//
//        //Create a blank sheet
//        XSSFSheet spreadsheet = workbook.createSheet( " Bicycles rent info");
//
//        //Create row object
//        XSSFRow row;
//
//        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone( ZoneId.systemDefault() );
//        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone( ZoneId.systemDefault() );
//
//        Map<String, Object[]> rentedBicyclesData = new TreeMap<>();
//        long rowid = 1;
//        //SearchRequest searchRequest = new SearchRequest();
////        SearchResponse searchResponse;
////        searchRequest.indices("bicycles");
//
//        Instant beginDate = ZonedDateTime.parse("2017-01-01T00:00:00Z").toInstant();
//        Instant endDate = ZonedDateTime.parse("2017-12-31T23:59:59Z").toInstant();
//
//        CountRequest countRequest = new CountRequest("bicycles");
//        RangeQueryBuilder query = QueryBuilders.rangeQuery("starttime.epochSecond")
//                .from(beginDate.getEpochSecond())
//                .to(endDate.getEpochSecond());
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.query(query);
//
//        countRequest.source(searchSourceBuilder);
//
//        CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
//        return countResponse.getCount();
//
//        Instant start = ZonedDateTime.parse("2017-01-01T00:00:00Z").toInstant();
//        Instant maxDate = ZonedDateTime.parse("2017-12-31T23:59:59Z").toInstant();
//        while (start.isBefore(maxDate)) {
//            start = start.plus(1, ChronoUnit.HOURS);
//            queryDate = QueryBuilders.rangeQuery("starttime.epochSecond").from(start.getEpochSecond()).to(start.plus(1, ChronoUnit.HOURS).getEpochSecond());
//            sourceBuilder.query(queryDate);
//
////            countRequest.source(sourceBuilder);
////            countResponse = client.count(countRequest, RequestOptions.DEFAULT);
//
//            searchRequest.source(sourceBuilder);
//            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
//
//            //long count = countResponse.getCount();
//            SearchHits hits = searchResponse.getHits();
//            TotalHits totalHits = hits.getTotalHits();
//            long numHits = totalHits.value;
//            SearchHit[] searchHits = hits.getHits();
//            for (SearchHit hit : hits.getHits()) {
//                String document = hit.getSourceAsString();
//                Gson gson = new GsonBuilder().create();
//            }
//
//            rentedBicyclesData.put(String.valueOf(rowid), new Object[] {
//                    dateFormatter.format(start),
//                    getDayOfWeek2(start.atZone(ZoneId.systemDefault()).getDayOfWeek().getValue()),
//                    timeFormatter.format(start),
//                    numHits
//            });
//            rowid++;
//        }
//        Set <String> keyid = rentedBicyclesData.keySet();
//        int rowID = 0;
//        for (String key : keyid) {
//            row = spreadsheet.createRow(rowID++);
//            Object [] objectArr = rentedBicyclesData.get(key);
//            int cellid = 0;
//
//            for (Object obj : objectArr){
//                Cell cell = row.createCell(cellid++);
//                if (obj instanceof Long) {
//                    cell.setCellValue(String.valueOf(obj));
//                } else {
//                    cell.setCellValue((String)obj);
//                }
//            }
//
//        }
//
//        //Write the workbook in file system
//        FileOutputStream out = new FileOutputStream(
//                new File("D:\\Wykres_2017.xlsx"));
//
//        workbook.write(out);
//        out.close();
//        System.out.println("Wykres_2017.xlsx written successfully");
//
//    }

    @PostConstruct
    public void countAVG() throws IOException {

        List<Double> tripDurations = new LinkedList<>();
        List<Double> distances = new LinkedList<>();

        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        SearchRequest searchRequest = new SearchRequest("bicycles");
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        Instant start = ZonedDateTime.parse("2017-01-01T00:00:00Z").toInstant();
        Instant stop = ZonedDateTime.parse("2017-12-31T00:00:00Z").toInstant();

        long startTime = System.nanoTime();
        RangeQueryBuilder queryDate = QueryBuilders.rangeQuery("starttime.epochSecond").from(start.getEpochSecond()).to(stop.getEpochSecond());
        searchSourceBuilder.query(queryDate);
        searchSourceBuilder.size(10000);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHits searchHits = searchResponse.getHits();

        int counter = 0;
        while (searchHits.getHits() != null && searchHits.getHits().length > 0) {
            for (SearchHit hit : searchHits.getHits()) {
                String document = hit.getSourceAsString();
                Gson gson = new GsonBuilder().create();
                Document2 document2 = gson.fromJson(document, Document2.class);
                Double tripduration = Double.valueOf(document2.getTripduration());
                Double distance = Haversine.distance(
                        Double.valueOf(document2.getLatitude_start()),
                        Double.valueOf(document2.getLongitude_start()),
                        Double.valueOf(document2.getLatitude_end()),
                        Double.valueOf(document2.getLongitude_end())
                );
                tripDurations.add(tripduration);
                distances.add(distance);
            }
            System.out.print(counter + ", ");
            if (counter % 15 == 0) {
                System.out.print("\n");
            }
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits();
            counter++;
        }


        List<Double> distancesWithoutZeros = distances.stream().filter(s -> !s.equals(0.0)).collect(Collectors.toList());
        Collections.sort(tripDurations);
        Collections.sort(distancesWithoutZeros);
        List<Double> tripDurationsMutliply = new LinkedList<>();
        List<Double> distancesMutliply = new LinkedList<>();

        tripDurations.forEach(i -> {
            tripDurationsMutliply.add((i * 60.0)/10.0);
        });
        distancesWithoutZeros.forEach(i -> {
            distancesMutliply.add(i * 1000.0);
        });

        int[] integers = tripDurationsMutliply.stream().mapToInt(i -> i.intValue()).toArray();
        int[] distancesIntArray = distancesMutliply.stream().mapToInt(i -> i.intValue()).toArray();
        int maxTripDurations = mode(integers);
        int maxDistanceFinal = mode(distancesIntArray);

        System.out.println("---------------------------");
        System.out.println("---------------------------");
        System.out.println("Trip Durations");
        System.out.println("---------------------------");
        System.out.println("Count: " + tripDurations.size());
        System.out.println("Average: " + calculateAverage(tripDurations));
        System.out.println("Median: "  + tripDurations.get((int)tripDurations.size()/2));
        System.out.println("Mode: "  + maxTripDurations);
        System.out.println("Distance");
        System.out.println("---------------------------");
        System.out.println("Count: " + distancesWithoutZeros.size());
        System.out.println("Average: " + calculateAverage(distancesWithoutZeros));
        System.out.println("Median: "  + distancesWithoutZeros.get((int)distancesWithoutZeros.size()/2));
        System.out.println("Mode: "  + maxDistanceFinal);
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

    private double calculateAverage(List<Double> marks) {
        Double sum = 0.0;
        if(!marks.isEmpty()) {
            for (Double mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }

    private String getDayOfWeek2(int value) {
        String day = "";
        switch (value) {
            case 1:
                day = "Monday";
                break;
            case 2:
                day = "Tuesday";
                break;
            case 3:
                day = "Wednesday";
                break;
            case 4:
                day = "Thursday";
                break;
            case 5:
                day = "Friday";
                break;
            case 6:
                day = "Saturday";
                break;
            case 7:
                day = "Sunday";
                break;
        }
        return day;
    }

    public static int mode(int []array)
    {
        HashMap<Integer,Integer> hm = new HashMap<Integer,Integer>();
        int max  = 1;
        int temp = 0;

        for(int i = 0; i < array.length; i++) {

            if (hm.get(array[i]) != null) {

                int count = hm.get(array[i]);
                count++;
                hm.put(array[i], count);

                if(count > max) {
                    max  = count;
                    temp = array[i];
                }
            }

            else
                hm.put(array[i],1);
        }
        return temp;
    }
}
