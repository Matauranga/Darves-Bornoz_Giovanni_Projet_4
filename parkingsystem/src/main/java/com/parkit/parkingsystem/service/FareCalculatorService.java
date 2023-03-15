package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
    final static double oneHour = 3_600_000d;

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();
        double price = 0;
        double discountCoef = discount
                ? 0.95
                : 1;
        Double duration = (outHour - inHour) / oneHour;

        if (duration < 0.5) {//si durée inferieur à 30min
            ticket.setPrice(price);
            return;
        }

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                price = Math.round((duration * Fare.CAR_RATE_PER_HOUR) * 100.0) / 100.0;
                break;
            }
            case BIKE: {
                price = Math.round((duration * Fare.BIKE_RATE_PER_HOUR) * 100.0) / 100.0;
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }

        price = Math.round((price * discountCoef) * 100.0) / 100.0;
        ticket.setPrice(price);
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}