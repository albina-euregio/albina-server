package org.caaml.v6;

/**
 * Custom data can be used to add arbitrary additional information. There can be AWS
 * specific custom data types defined. The required `type` identifies the custom data type.
 */
public interface CustomData {

	String getType();

	void setType(String value);

}
