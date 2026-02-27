package org.profiling.model;

public class Order {
    private Long id;
    private Long userId;
    private String productName;
    private Integer quantity;
    private Double totalPrice;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    @Override
    public String toString() {
        return "Order{id=" + id + ", userId=" + userId + ", productName='" +
                productName + "', quantity=" + quantity + ", totalPrice=" + totalPrice + "}";
    }
}