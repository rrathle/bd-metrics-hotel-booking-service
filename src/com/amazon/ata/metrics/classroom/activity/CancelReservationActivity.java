package com.amazon.ata.metrics.classroom.activity;

import com.amazon.ata.metrics.classroom.dao.ReservationDao;
import com.amazon.ata.metrics.classroom.dao.models.Reservation;
import com.amazon.ata.metrics.classroom.metrics.MetricsConstants;
import com.amazon.ata.metrics.classroom.metrics.MetricsPublisher;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

import javax.inject.Inject;

/**
 * Handles requests to cancel a reservation.
 */
public class CancelReservationActivity {

    private ReservationDao reservationDao;
    private MetricsPublisher metricsPublisher;

    /**
     * Constructs a CancelReservationActivity
     * @param reservationDao Dao used to update reservations.
     */
    @Inject
    public CancelReservationActivity(ReservationDao reservationDao, MetricsPublisher metricsPublisher) {
        this.reservationDao = reservationDao;
        this.metricsPublisher = metricsPublisher;
    }

    /**
     * Cancels the given reservation.
     *      and updates the CAnceledReservationCount Metric
     *      and update the ReservationRevenue metri
     * @param reservationId of the reservation to cancel.
     * @return canceled reservation
     */
    public Reservation handleRequest(final String reservationId) {

        Reservation response = reservationDao.cancelReservation(reservationId);

        //update the bookedReservation metric count
        metricsPublisher.addMetric(MetricsConstants.CANCEL_COUNT, 1, StandardUnit.Count);


        //Update the ReservationRevenue metric with the total cost of the Reservation
        // the total cost in the Reservation is negative if we lost all we need to do is store
        //The reservation is stores in the response upon return from the ReservationDao

        metricsPublisher.addMetric(MetricsConstants.RESERVATION_REVENUE,
                response.getTotalCost().doubleValue(),
                StandardUnit.None);

        return response;
    }
}
