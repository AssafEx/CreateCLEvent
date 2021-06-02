package removeSOAP;

import org.apache.log4j.Logger;
import javax.xml.soap.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


public class ConnectLayerSOAP {
    private static final Logger log = Logger.getLogger(ConnectLayerSOAP.class);

    private static final String namespace  = "cam";
    private static final String namespaceURI = "http://campusm.gw.com/campusm";
    private static final String host = "https://idp.ombiel.co.uk";
    private static final String endpoint = "/demo/services/CampusMUniversityService/";
    private static final String url = host + endpoint;
    private final static String CCL_BASIC_AUTH_USER = "application_sec_user";
    private final static String CCL_BASIC_AUTH_PASS = "connect";
    private final static String username = "priya";
    private final static String password = "campusm";
    private final static List<String> eventsTypes = Arrays.asList("TT", "ET", "UE", "ST");
    public final static String STUDENT =  "student";
    public final static String TEACHER =  "teacherGeneral";
    public final static String USER =  "userGeneral";
    public static DateTimeFormatter fullZoned = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    public static ZoneId zoneIsr = ZoneId.of("Israel");
    public static ZoneId zoneLondon = ZoneId.of("Europe/London");

    public static void main(String[] args) {
        removeEvents(Arrays.asList("p3riya"));
    }

    public static void removeEvents(List <String> users) {
        users.forEach(user->{
            eventsTypes.forEach(type->{
                try {
                    SOAPClient.getInstance().callSoapWebService(createSOAPRequest(user, password, type));
                } catch (SOAPException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            });
        });
    }

    private static SOAPMessage createSOAPRequest(String username, String password, String calType) throws SOAPException {

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(namespace, namespaceURI);

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("removeTimetableEntries", namespace);

        soapBodyElem.addChildElement("username", namespace).addTextNode(username);

        soapBodyElem.addChildElement("password", namespace).addTextNode(password);

        soapBodyElem.addChildElement("calType", namespace).addTextNode(calType);

        ZonedDateTime now = ZonedDateTime.of(LocalDateTime.of(LocalDate.now(zoneIsr), LocalTime.MIDNIGHT), zoneIsr);

        soapBodyElem.addChildElement("start", namespace).addTextNode(now.minusYears(2).format(fullZoned));

        soapBodyElem.addChildElement("end", namespace).addTextNode(now.plusYears(2).format(fullZoned));

        return soapMessage;
    }
}

