package com.tiwpr.rest.garbageCollector;

import com.tiwpr.rest.model.dao.Reservation;
import com.tiwpr.rest.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class ReservationGarbageCollector implements Runnable{
    private Long reservationId;
    private ReservationRepository reservationRepository;


    public ReservationGarbageCollector(long reservationId, ReservationRepository reservationRepository){
        this.reservationId = reservationId;
        this.reservationRepository = reservationRepository;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000*60*1);
            Optional<Reservation> reservationOpt = reservationRepository.findById(this.reservationId);
            if (reservationOpt.isPresent()){
                if (reservationOpt.get().getDate() == null){
                    reservationRepository.delete(reservationOpt.get());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
