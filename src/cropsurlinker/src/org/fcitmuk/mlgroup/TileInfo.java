/***
 * @author mistaguy
 * This is our tile GAE model
 */
package org.fcitmuk.mlgroup;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity(name = "TileInfo")
public class TileInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private float lon_ul;
	private float lat_ul;
	private float lon_lr;
	private float lat_lr;
	private String blobkey;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public float getLon_ul() {
		return lon_ul;
	}

	public void setLon_ul(float lon_ul) {
		this.lon_ul = lon_ul;
	}

	public float getLat_ul() {
		return lat_ul;
	}

	public void setLat_ul(float lat_ul) {
		this.lat_ul = lat_ul;
	}

	public float getLon_lr() {
		return lon_lr;
	}

	public void setLon_lr(float lon_lr) {
		this.lon_lr = lon_lr;
	}

	public float getLat_lr() {
		return lat_lr;
	}

	public void setLat_lr(float lat_lr) {
		this.lat_lr = lat_lr;
	}

	public String getFilename() {
		return blobkey;
	}

	public void setFilename(String filename) {
		this.blobkey = filename;
	}


	
}
