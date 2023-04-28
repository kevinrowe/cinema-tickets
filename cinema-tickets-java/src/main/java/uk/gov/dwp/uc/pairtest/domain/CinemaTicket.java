package uk.gov.dwp.uc.pairtest.domain;

public abstract class CinemaTicket {
    protected int price = 0;
    protected int noSeats = 0;

    public int getPrice() {
        return price;
    }

    public int getNoSeats() {
        return noSeats;
    }
}