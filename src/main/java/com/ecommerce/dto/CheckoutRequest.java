package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

public class CheckoutRequest {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Payment method is required (e.g., CREDIT_CARD, DEBIT_CARD, UPI, COD)")
    private String paymentMethod = "COD";

    public CheckoutRequest() {
    }

    public CheckoutRequest(String shippingAddress, String paymentMethod) {
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
