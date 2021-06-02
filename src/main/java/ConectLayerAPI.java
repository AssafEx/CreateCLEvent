import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConectLayerAPI {
    private final static String CCL_HOST = "https://idp.ombiel.co.uk";
    private final static String CCL_ENDPOINT_WSDL = "/demo/services/CampusMUniversityService/";
    private final static String CCL_BASIC_AUTH_USER = "application_sec_user";
    private final static String CCL_BASIC_AUTH_PASS = "connect";
    private final static List<String> eventsTypes = Arrays.asList("TT", "ET", "UE", "ST");
    private final static String USERNAME =  "MultiDevice%s";
    private final static String EMAIL_PREFIX =  "@exlibrisgroup.com";
    private final static String EMAIL =  USERNAME + EMAIL_PREFIX;
    private final static String STUDENT_SSO = "hannan.abunamous" + EMAIL_PREFIX;
    private final static String STUDENT_LDAP = "hannan";

    private final static String PASSWORD =  "campusm";
    private final static int USER_BASE = 322;
    public static ZoneId zoneIsr = ZoneId.of("Israel");

    private static final Logger log = Logger.getLogger(ConectLayerAPI.class);
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static DateTimeFormatter formatterDay = DateTimeFormatter.ofPattern("e");

    public static void main(String[] args) {
        createStudentEvent("MultiDevice1");
        createStaffEvent(323);  //creating event for student hannan and teacher 323 - AKA MultiDevice1. password:campusm

    }

    private static void createEvent(String userName, int teacherId) {
        LocalTime midnight = LocalTime.MIDNIGHT;
        LocalDate today = LocalDate.now(zoneIsr);
        JSONObject event = readFromJsonFileToObject("src/main/resources/eventsOverlapFull.json").getJSONArray("events").getJSONObject(0);
        Map<String, Object> parameters = event.toMap();
        parameters.compute("start", (key, val) ->
            ZonedDateTime
                .of(LocalDateTime.of(today, midnight)
                    .withHour(Integer.parseInt((String)val)), zoneIsr)
                .format(formatter));
        String parametersString =   parameters.entrySet()
            .stream()
            .map(entry -> "&" + entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining());
        String payload =  String.format("createTimetableEntry?username=%s&password=%s", userName, PASSWORD);
        String URL = CCL_HOST + CCL_ENDPOINT_WSDL + payload + parametersString;
        URL += teacherId ==0? "": "&teacher_id=" + teacherId;
        HttpResponse<String> res = Unirest.get(URL)
            .basicAuth(CCL_BASIC_AUTH_USER, CCL_BASIC_AUTH_PASS)
            .asString();
        Document doc = Jsoup.parse(res.getBody(), "", Parser.xmlParser());
        System.out.println("creating event for :" + userName + ": " + res.getStatus() + " " + doc.select("ns1|desc").text());
    }
    private static void createStudentEvent(String userName) {
        createEvent(userName,0);
    }


    private static void createStaffEvent(int teacherId) {
        createEvent("hannan",teacherId);
    }
    public static JSONObject readFromJsonFileToObject(String path) {
        try {
            if (!(new File(path)).exists()) {
                throw new Exception("no such file: " + path );
            }
            InputStream is = new FileInputStream(path);
            return new JSONObject(IOUtils.toString(is, "UTF-8"));
        } catch (Exception e) {
            try {
                throw new Exception("unable to read json from file: " + e );
            } catch (Exception ex) {
            }
        }
        return null;
    }

}
