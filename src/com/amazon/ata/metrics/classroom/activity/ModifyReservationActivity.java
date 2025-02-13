package com.amazon.ata.metrics.classroom.activity;

import com.amazon.ata.metrics.classroom.dao.ReservationDao;
import com.amazon.ata.metrics.classroom.dao.models.UpdatedReservation;
import com.amazon.ata.metrics.classroom.metrics.MetricsConstants;
import com.amazon.ata.metrics.classroom.metrics.MetricsPublisher;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

import java.time.ZonedDateTime;
import javax.inject.Inject;

/**
 * Handles requests to modify a reservation
 */
public class ModifyReservationActivity {

    private ReservationDao reservationDao;
    private MetricsPublisher metricsPublisher;

    /**
     * Construct ModifyReservationActivity.
     * @param reservationDao Dao used for modify reservations.
     */
    @Inject
    public ModifyReservationActivity(ReservationDao reservationDao, MetricsPublisher metricsPublisher) {
        this.reservationDao = reservationDao;
        this.metricsPublisher = metricsPublisher;
    }

    /**
     * Modifies the given reservation.
     *           and update the modifedREservationCountMetric
     *           and update the reservationRevenue metric
     * @param reservationId Id to modify reservations for
     * @param checkInDate modified check in date
     * @param numberOfNights modified number of nights
     * @return UpdatedReservation that includes the old reservation and the updated reservation details.
     */
    public UpdatedReservation handleRequest(final String reservationId, final ZonedDateTime checkInDate,
                                            final Integer numberOfNights) {

        UpdatedReservation updatedReservation = reservationDao.modifyReservation(reservationId, checkInDate,
            numberOfNights);

        //update the bookedReservation metric count
        metricsPublisher.addMetric(MetricsConstants.MODIFY_COUNT, 1, StandardUnit.Count);

        //Update the ReservationRevenue metric with the total cost of the Reservation
        //The Updatedreservation is stores in the response upon return from the ReservationDao
        //  and contains the original reservation and the modified reservation
        // of we subtract the totalCost from the original reservation from the modified reservation
        //  we willl have the difference in revenue fro the metric

        // calculate the revenue differnce due to the mofifired
        double revenueDifference = updatedReservation.getModifiedReservation().getTotalCost().
                            subtract(updatedReservation.getOriginalReservation().getTotalCost()).doubleValue();

        //Update the resrvationRevnue metric
        metricsPublisher.addMetric(MetricsConstants.RESERVATION_REVENUE, revenueDifference, StandardUnit.None);

        return updatedReservation;
    }
}
