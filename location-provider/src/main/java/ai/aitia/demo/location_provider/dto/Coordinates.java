package ai.aitia.demo.location_provider.dto;

import java.io.Serializable;

public class Coordinates implements Serializable {

    private double longitude;
    private double latitude;

	public Coordinates(final double latitude, final double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

	public double getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

}
