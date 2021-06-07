package br.com.ottimizza.application.domain.dtos;

import java.io.Serializable;
import java.math.BigInteger;

import br.com.ottimizza.application.model.product.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private String name;

    private String description;

    private String appUrl;

    private String imageUrl;

    private String group;
    
    private String aboutUrl;

    private Short classification;

    public Product patch(Product product) {
        if (this.name != null && !this.name.equals(""))
            product.setName(this.name);

        if (this.description != null && !this.description.equals(""))
            product.setDescription(this.description);

        if (this.appUrl != null && !this.appUrl.equals(""))
            product.setAppUrl(this.appUrl);

        if (this.imageUrl != null && !this.imageUrl.equals(""))
            product.setImageUrl(this.imageUrl);

        if (this.group != null && !this.group.equals(""))
            product.setGroup(this.group);

        if(this.classification != null)
            product.setClassification(this.classification);

        return product;
    }

	public ProductDTO(BigInteger id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

    
}
