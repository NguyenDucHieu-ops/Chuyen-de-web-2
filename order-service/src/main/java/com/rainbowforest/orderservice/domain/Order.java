package com.rainbowforest.orderservice.domain;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private BigDecimal total;
	private LocalDate orderedDate;
	private String status;

	private String customerName;
	private String phoneNumber;
	private String shippingAddress;
	private String paymentMethod;
	private String userName;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"), inverseJoinColumns = @JoinColumn(name = "item_id"))
	@ToString.Exclude // ✅ THÊM DÒNG NÀY
	@EqualsAndHashCode.Exclude // ✅ THÊM DÒNG NÀY
	private List<Item> items;
}