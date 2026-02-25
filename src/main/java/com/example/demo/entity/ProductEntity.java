package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
@Entity
//@Table("Product")
public class ProductEntity {
	
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message = "商品名は必須です")
	private String name;
	
	@NotNull(message = "価格は必須です")
	private String price;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private UserEntity user;

}
