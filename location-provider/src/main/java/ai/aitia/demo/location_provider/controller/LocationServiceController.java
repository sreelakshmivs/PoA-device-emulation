package ai.aitia.demo.location_provider.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ai.aitia.demo.location_provider.LocationProviderConstants;
import ai.aitia.demo.location_provider.dto.Coordinates;


@RestController
@RequestMapping(LocationProviderConstants.LOCATION_URI)
public class LocationServiceController {

	@Value("${device-longitude}")
    private double DEVICE_LONGITUDE;

    @Value("${device-latitude}")
    private double DEVICE_LATITUDE;
	
	//=================================================================================================
	// members	
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping
    public Coordinates getLocation() {
		System.out.println("Providing location!");
		return new Coordinates(DEVICE_LATITUDE, DEVICE_LONGITUDE);
	}

}
