package com.auction.common.model;
public class Seller extends User {
    private double balance = 0.0;
    public Seller(String username, String password) {
        super(username, password, Role.SELLER);
    }
    public double getBalance() {
        return balance;
    }
    public void receiveBalance(double amount) {
        this.balance += amount;
    }
}
