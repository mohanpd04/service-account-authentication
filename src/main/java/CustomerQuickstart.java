import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androiddeviceprovisioning.v1.AndroidProvisioningPartner;
import com.google.api.services.androiddeviceprovisioning.v1.model.Company;
import com.google.api.services.androiddeviceprovisioning.v1.model.CustomerListCustomersResponse;
import com.google.api.services.androiddeviceprovisioning.v1.model.CustomerListDpcsResponse;
import com.google.api.services.androiddeviceprovisioning.v1.model.Dpc;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/** This class forms the quickstart introduction to the zero-touch enrollment customer API. */
public class CustomerQuickstart {

  // A single auth scope is used for the zero-touch enrollment customer API.
  private static final List<String> SCOPES =
      Arrays.asList("https://www.googleapis.com/auth/androidworkzerotouchemm");
  private static final String APP_NAME = "Zero-touch Enrollment Java Quickstart";

  // Global shared instances
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static HttpTransport HTTP_TRANSPORT;

  static {
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Creates a GoogleCredentials object with the correct OAuth2 authorization for the service
   * account that calls the reseller API. The service endpoint invokes this method when setting up a
   * new service instance.
   *
   * @return an authorized GoogleCredentials object.
   * @throws IOException
   */
  public static GoogleCredentials authorize() throws IOException {
    // Load service account key.
    InputStream in = CustomerQuickstart.class.getResourceAsStream("/service_account_key.json");

    // Create the credential scoped to the zero-touch enrollment customer APIs.
    GoogleCredentials credential = ServiceAccountCredentials.fromStream(in).createScoped(SCOPES);
    return credential;
  }

  /**
   * Build and return an authorized zero-touch enrollment API client service. Use the service
   * endpoint to call the API methods.
   *
   * @return an authorized client service endpoint
   * @throws IOException
   */
  public static AndroidProvisioningPartner getService() throws IOException {
    GoogleCredentials credential = authorize();
    HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);
    return new AndroidProvisioningPartner.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
        .setApplicationName(APP_NAME)
        .build();
  }

  /**
   * Runs the zero-touch enrollment quickstart app.
   *
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    // Create a zero-touch enrollment API service endpoint.
    AndroidProvisioningPartner service = getService();

    // Get the customer's account. Because a customer might have more
    // than one, limit the results to the first account found.
    AndroidProvisioningPartner.Customers.List accountRequest = service.customers().list();
    accountRequest.setPageSize(1);
    CustomerListCustomersResponse accountResponse = accountRequest.execute();
	
	// Check if accountResponse or its customers list is null
    if (accountResponse == null || accountResponse.getCustomers() == null ||accountResponse.getCustomers().isEmpty()) {
      // No accounts found for the user. Confirm the Google Account
      // that authorizes the request can access the zero-touch portal.
      System.out.println("No zero-touch enrollment account found.");
      System.exit(-1);
    }
    Company customer = accountResponse.getCustomers().get(0);
	
	// Check if customer is null before accessing its methods
    if (customer == null) {
        System.out.println("Customer data is null.");
        System.exit(-1);
    }
	
    String customerAccount = customer.getName();

    // Send an API request to list all the DPCs available using the customer account.
    AndroidProvisioningPartner.Customers.Dpcs.List request =
        service.customers().dpcs().list(customerAccount);
    CustomerListDpcsResponse response = request.execute();
	
	// Check if response or its DPCs list is null
    if (response == null || response.getDpcs() == null) {
        System.out.println("No DPCs found for the customer account.");
        return; // or System.exit(-1);
    }

    // Print out the details of each DPC.
    java.util.List<Dpc> dpcs = response.getDpcs();
    for (Dpc dpcApp : dpcs) {
		if(dpcApp != null){
      System.out.format("Name:%s  APK:%s\n", dpcApp.getDpcName(), dpcApp.getPackageName());
	  }
    }
  }
}