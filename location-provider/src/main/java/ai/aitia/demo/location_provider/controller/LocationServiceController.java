package ai.aitia.demo.location_provider.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ai.aitia.demo.location_provider.CarProviderConstants;
import ai.aitia.demo.location_provider.dto.Coordinates;


@RestController
@RequestMapping(CarProviderConstants.LOCATION_URI)
public class LocationServiceController {
	
	//=================================================================================================
	// members	
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping
    public Coordinates location() {
        return new Coordinates(65.34704242176377, 21.408441463261536);
    }
	
}
