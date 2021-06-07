package br.com.ottimizza.application.model.product;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "products")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    @SequenceGenerator(name = "products_sequence", sequenceName = "products_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_sequence")
    private BigInteger id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "app_url")
    private String appUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "group_name")
    private String group;
    
    @Column(name = "about_url")
    private String aboutUrl;
    
    @Column(name = "product_order")
    private Short productOrder;

    @Column(name = "classification")
    private Short classification;
    
    @Column(name = "external_id")
    private String external_id;

    public static class Classification {
        public static final Integer OTTIMIZZA = 0;
        public static final Integer PARTNER = 1;
    }

}
