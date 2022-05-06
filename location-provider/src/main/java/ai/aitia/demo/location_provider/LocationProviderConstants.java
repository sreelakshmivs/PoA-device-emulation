package ai.aitia.demo.location_provider;

public class LocationProviderConstants {
	
	//=================================================================================================
	// members
	
	public static final String BASE_PACKAGE = "ai.aitia";
	
	public static final String LOCATION_SERVICE_DEFINITION = "location";
	public static final String INTERFACE_SECURE = "HTTP-SECURE-JSON";
	public static final String INTERFACE_INSECURE = "HTTP-INSECURE-JSON";
	public static final String HTTP_METHOD = "http-method";
	public static final String LOCATION_URI = "/location";
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private LocationProviderConstants() {
		throw new UnsupportedOperationException();
	}
}
