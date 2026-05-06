package com.auction.common.model;

public class Bidder extends User{
    private double availableBalance = 0;
    private double frozenBalance = 0;
    public Bidder (String username, String password){
        super(username, password,Role.BIDDER);
    }
    public double getAvailableBalance(){
        return availableBalance;
    }
    public double getFrozenBalance(){
        return frozenBalance;
    }
    public synchronized void deposit(double amount){
        if (amount <= 0){
            throw new IllegalArgumentException("Số tiền nhập vào không hợp lệ!");
        }
        else{
            setAvailableBalance(this.availableBalance + amount);
        }
    }
    public synchronized void withdraw(double amount){
        if (amount <= 0){
            throw new IllegalArgumentException("Số tiền rút không hợp lệ!");
        }
        if (amount > availableBalance){
            throw new IllegalArgumentException("Số tiền rút vượt quá số dư khả dụng!");
        }
        else{
            setAvailableBalance(this.availableBalance - amount);
        }
    }
    public synchronized void freezeMoney(double amount){
        if (amount <= 0){
            throw new IllegalArgumentException("Số tiền đóng băng không hợp lệ");
        }
        else{
            setFrozenBalance(this.frozenBalance + amount);
            setAvailableBalance(this.availableBalance - amount);
        }
    }
    public synchronized void releaseMoney(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền hoàn trả không hợp lệ");
        } else {
            setFrozenBalance(this.frozenBalance - amount);
            setAvailableBalance(this.availableBalance + amount);
        }
    }
    private synchronized void setAvailableBalance(double amount){
            this.availableBalance = amount;
    }
    private synchronized void setFrozenBalance(double amount){
            this.frozenBalance = amount;
    }
    public synchronized void paid(double amount){
        if (amount > 0 && amount <= this.frozenBalance) {
            setFrozenBalance(this.frozenBalance - amount);
        }
    }
}
