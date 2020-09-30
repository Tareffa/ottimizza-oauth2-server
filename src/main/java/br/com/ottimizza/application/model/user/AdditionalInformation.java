package br.com.ottimizza.application.model.user;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class AdditionalInformation implements Serializable {

	private static final long serialVersionUID = 1L;

	private String role;
	
	private String birthDate;
	
	private String accountingDepartment;
}
