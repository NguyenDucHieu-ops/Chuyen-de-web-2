package com.rainbowforest.orderservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "items")
@Data // Tự động tạo Getter, Setter, ToString
@NoArgsConstructor
@AllArgsConstructor
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private Long id;

	@Column(name = "quantity")
	@NotNull
	private int quantity;

	@Column(name = "subtotal")
	@NotNull
	private BigDecimal subTotal;

	// Các trường lưu thông tin sản phẩm tại thời điểm mua
	private String productName;
	private BigDecimal productPrice;

	// ✅ CẢI TIẾN QUAN TRỌNG: Ép kiểu TEXT để không bao giờ bị lỗi "Data too long"
	@Column(columnDefinition = "TEXT")
	private String productImageUrl;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "product_id")
	private Product product;

	@ManyToMany(mappedBy = "items")
	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<Order> orders;

	// Constructor phục vụ việc tạo Item nhanh
	public Item(@NotNull int quantity, Product product, BigDecimal subTotal) {
		this.quantity = quantity;
		this.product = product;
		this.subTotal = subTotal;
	}
}