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
        double discountCoef = discount//ecriture ternaire
                                ? 0.95
                                :1;

        Double duration = (outHour - inHour) / oneHour;

        if (duration < 0.5) {//si durée inferieur à 30min
            ticket.setPrice(0);
            return;
        }

        //Si prix nominal
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR*discountCoef);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR*discountCoef);
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}