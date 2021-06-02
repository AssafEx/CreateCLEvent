package removeSOAP;

import okhttp3.Credentials;
import org.apache.log4j.Logger;

import javax.xml.soap.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class SOAPClient {

	private static final SOAPClient soapClientInstance = new SOAPClient();
    private static final Logger log = Logger.getLogger(SOAPClient.class);
    private final static String CCL_BASIC_AUTH_USER = "application_sec_user";
    private final static String CCL_BASIC_AUTH_PASS = "connect";
    private static final String host = "https://idp.ombiel.co.uk";
    private static final String endpoint = "/demo/services/CampusMUniversityService/";
    private static final String url = host + endpoint;

    private SOAPClient() {
	}
	
	public static SOAPClient getInstance() {
		return soapClientInstance;
	}
	
	public SOAPMessage callSoapWebService(SOAPMessage soapMessage) {
	
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("Authorization", Credentials.basic(CCL_BASIC_AUTH_USER, CCL_BASIC_AUTH_PASS));
        
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            ByteArrayOutputStream bosRequest = new ByteArrayOutputStream();
            soapMessage.writeTo(bosRequest);
            System.out.println("SOAP Request Message: \n" + bosRequest.toString());
            
            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(soapMessage, url);
           
            SOAPFault soapFault = soapResponse.getSOAPPart().getEnvelope().getBody().getFault();
            if(soapFault != null) {
                ByteArrayOutputStream bosResponse = new ByteArrayOutputStream();
                soapResponse.writeTo(bosResponse);
                System.out.println("SOAP Response Message: \n" + bosResponse.toString());
            	throw new RuntimeException("SOAP Response Fault Code:" + soapFault.getFaultCode());
            }
            
            soapConnection.close();
            
            return soapResponse;
            
        } catch (SOAPException | IOException e) {
            System.out.println(e.getClass().getName()+": "+ e.getMessage());
            e.printStackTrace();
            
            throw new RuntimeException(e);
        }
    }


	
}
