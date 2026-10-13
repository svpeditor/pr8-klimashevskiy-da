package ru.edu.pr05;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BusinessTripService {
    private final List<BusinessTrip> trips = new ArrayList<>();

    public BusinessTrip create(String id, LocalDate start, LocalDate end) {
        if (findById(id) != null) {
            throw new IllegalArgumentException("Командировка " + id + " уже есть");
        }
        BusinessTrip trip = new BusinessTrip(id, start, end);
        trips.add(trip);
        return trip;
    }

    public BusinessTrip findById(String id) {
        for (BusinessTrip trip : trips) {
            if (trip.getId().equals(id)) {
                return trip;
            }
        }
        return null;
    }

    public List<BusinessTrip> findByStatus(TripStatus status) {
        List<BusinessTrip> result = new ArrayList<>();
        for (BusinessTrip trip : trips) {
            if (trip.getStatus() == status) {
                result.add(trip);
            }
        }
        return result;
    }
}
