package br.com.ottimizza.application.model.user;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.databind.ObjectMapper;

@Converter(autoApply = true)
public class AdditionalInformationConverter implements AttributeConverter<AdditionalInformation, String>{

	@Override
	public String convertToDatabaseColumn(AdditionalInformation attribute) {
		ObjectMapper mapper = new ObjectMapper();
		String additionalInformation = "{}";
	    if (attribute == null)
	    	return null;
	    try {
	        additionalInformation = mapper.writeValueAsString(attribute);
	    } catch (Exception e) {
	        additionalInformation = "{}";
	    }
	    return additionalInformation;
	}

	@Override
	public AdditionalInformation convertToEntityAttribute(String dbData) {
		ObjectMapper mapper = new ObjectMapper();
		AdditionalInformation additionalInformation = null;
        if (dbData == null)
            return null;
        try {
            additionalInformation = mapper.readValue(dbData, AdditionalInformation.class);
        } catch (Exception e) {
        }
        return additionalInformation;
	}

}
