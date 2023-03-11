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

        //TODO: Some tests are failing here. Need to check if this logic is correct
        Double duration = (outHour - inHour) / oneHour;

        if (duration < 0.5) {//si durée inferieur à 30min
            ticket.setPrice(0);
            return;
        }
        if (discount) {// Si reduction de 5%
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice((duration * Fare.CAR_RATE_PER_HOUR) * 0.95);
                    break;
                }
                case BIKE: {
                    ticket.setPrice((duration * Fare.BIKE_RATE_PER_HOUR) * 0.95);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unkown Parking Type");
            }
            return;
        }
        //Si prix nominal
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
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