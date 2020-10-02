package br.com.ottimizza.application.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
public class AdditionalInformationDTO {

    private List<String> departments;

    private List<String> roles;
}
