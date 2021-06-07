package br.com.ottimizza.application.domain.mappers;

import br.com.ottimizza.application.domain.dtos.ProductDTO;
import br.com.ottimizza.application.model.product.Product;

/**
 * @formatter:off
 * 
 * Classe responsável pelo mapeamento de Entity para DTO e vice-versa. 
 * Normalmente utilizada em Services para converter um DTO recebido pelo 
 * Controller em uma Entity, e para converter uma Entity em DTO novamente 
 * após todas as regras de negócio. Desta forma o cliente nunca tem acesso 
 * direto a camada de banco de dados.
 */
public class ProductMapper { // @formatter:off

    /**
     * Método responsável pela conversão de uma Entity para DTO.
     * 
     * @param Product Entity que será convertida.
     * @return DTO (Data Transfer Object). 
     */
    public static ProductDTO fromEntity(Product product) { 
        return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .appUrl(product.getAppUrl())
            .imageUrl(product.getImageUrl())
            .group(product.getGroup())
            .aboutUrl(product.getAboutUrl())
            .classification(product.getClassification())
            .build();
    }

    /**
     * Método responsável pela conversão de um DTO para Entity.
     * 
     * @param ProductDTO Classe DTO que será convertida.
     * @return Entity (Classe que reflete a camada de negócio - banco de dados). 
     */
    public static Product fromDTO(ProductDTO product) {
        return Product.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .appUrl(product.getAppUrl())
            .imageUrl(product.getImageUrl())
            .group(product.getGroup())
            .aboutUrl(product.getAboutUrl())
            .classification(product.getClassification())
            .build();
    }

}
