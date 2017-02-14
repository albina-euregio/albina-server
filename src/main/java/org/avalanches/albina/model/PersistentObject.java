package org.avalanches.albina.model;

public interface PersistentObject {
	public String getId();

	public void setId(String id);

	public Integer getVersion();

	public void setVersion(Integer version);
}