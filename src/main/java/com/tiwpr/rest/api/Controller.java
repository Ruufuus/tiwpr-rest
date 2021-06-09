package com.tiwpr.rest.api;

import com.tiwpr.rest.model.dao.Client;
import com.tiwpr.rest.model.dao.Hotel;
import com.tiwpr.rest.model.dao.Reservation;
import com.tiwpr.rest.model.dao.Room;
import com.tiwpr.rest.model.dto.get.ClientDtoGet;
import com.tiwpr.rest.model.dto.get.HotelDtoGet;
import com.tiwpr.rest.model.dto.get.ReservationDtoGet;
import com.tiwpr.rest.model.dto.get.RoomDtoGet;
import com.tiwpr.rest.model.dto.post.ClientDtoPost;
import com.tiwpr.rest.model.dto.post.HotelDtoPost;
import com.tiwpr.rest.model.dto.post.ReservationDtoPost;
import com.tiwpr.rest.model.dto.post.RoomDtoPost;
import com.tiwpr.rest.model.dto.put.ClientDtoPut;
import com.tiwpr.rest.repository.ClientRepository;
import com.tiwpr.rest.repository.HotelRepository;
import com.tiwpr.rest.repository.ReservationRepository;
import com.tiwpr.rest.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@RequestMapping("api")
@RestController
public class Controller {
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    HotelRepository hotelRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    RoomRepository roomRepository;


    @PostMapping("/clients")
    public ResponseEntity<?> postClient(@Valid @RequestBody ClientDtoPost client) {
        Client cl = new Client(client);
        clientRepository.save(cl);
        return ResponseEntity.ok(new ClientDtoGet(cl));
    }

    @GetMapping("/clients")
    public ResponseEntity<?> getAllClients(@RequestParam Optional<Integer> page) {
        List<ClientDtoGet> clientDtoGets = new ArrayList<>();
        if (!page.isPresent()) {
            clientRepository.findAll()
                    .forEach(client -> clientDtoGets.add(new ClientDtoGet(client)));
        } else {
            if (page.get() < 0) {
                return ResponseEntity.status(416).body("Page number is incorrect!");
            }
            clientRepository.findAll(PageRequest.of(page.get(), 10))
                    .forEach(client -> clientDtoGets.add(new ClientDtoGet(client)));
        }
        return ResponseEntity.ok(clientDtoGets);
    }

    @GetMapping("/clients/{clientId}")
    public ResponseEntity<?> getClientByClientId(@PathVariable long clientId) {
        Optional<Client> clientOpt = clientRepository.findByClientId(clientId);
        if (clientOpt.isPresent()) {
            return ResponseEntity.ok(new ClientDtoGet(clientOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/clients/{clientId}")
    public ResponseEntity<?> putClientByClientId(@PathVariable long clientId, @Valid @RequestBody ClientDtoPut client){
        Optional<Client> clientOpt = clientRepository.findByClientId(clientId);
        if (clientOpt.isPresent()) {
            clientOpt.get().setName(client.getName());
            clientOpt.get().setSurname(client.getSurname());
            AtomicBoolean isCorrectQuerry = new AtomicBoolean(true);
            prepareClientNewReservationList(client, clientOpt, isCorrectQuerry);
            if(isCorrectQuerry.get()){
                clientRepository.save(clientOpt.get());
                return ResponseEntity.ok(new ClientDtoGet(clientOpt.get()));
            }
        }
        return ResponseEntity.notFound().build();
    }

    private void prepareClientNewReservationList(@RequestBody @Valid ClientDtoPut client, Optional<Client> clientOpt, AtomicBoolean isCorrectQuerry) {
        ArrayList<Reservation> newReservations = new ArrayList<>();
        client.getReservations().forEach(reservationId -> {
            Optional<Reservation> reservation = reservationRepository.findByReservationId(reservationId);
            if (reservation.isPresent()){
                newReservations.add(reservation.get());
            }else{
                isCorrectQuerry.set(false);
            }
        });
        clientOpt.get().changeReservationList(newReservations);
    }

    @PatchMapping("/clients/{clientId}")
    public ResponseEntity<?> patchClientByClientId(@PathVariable long clientId, @RequestBody ClientDtoPut client){
        Optional<Client> clientOpt = clientRepository.findByClientId(clientId);
        if (clientOpt.isPresent()) {
            if (client.getName() != null)
                clientOpt.get().setName(client.getName());
            if (client.getSurname() != null)
                clientOpt.get().setSurname(client.getSurname());
            AtomicBoolean isCorrectQuerry = new AtomicBoolean(true);
            if (client.getReservations() != null){
                prepareClientNewReservationList(client, clientOpt, isCorrectQuerry);
            }
            if(isCorrectQuerry.get()){
                clientRepository.save(clientOpt.get());
                return ResponseEntity.ok(new ClientDtoGet(clientOpt.get()));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/clients/{clientId}")
    public ResponseEntity<?> deleteClientByClientId(@PathVariable long clientId){
        Optional<Client> clientOpt = clientRepository.findByClientId(clientId);
        if (clientOpt.isPresent()) {
            clientOpt.get().removeClientFromReservations();
            clientRepository.save(clientOpt.get());
            clientRepository.delete(clientOpt.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/clients/{clientId}/reservations")
    public ResponseEntity<?> getReservationByClientId(@PathVariable long clientId) {
        Optional<Client> clientOpt = clientRepository.findByClientId(clientId);
        if (clientOpt.isPresent()) {
            List<ReservationDtoGet> reservations = new ArrayList<>();
            reservationRepository.findByClients(clientOpt.get())
                    .forEach(reservation -> reservations.add(new ReservationDtoGet(reservation)));
            return ResponseEntity.ok(reservations);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/clients/{clientId}/reservations/{reservationId}")
    public ResponseEntity<?> getReservationByClientIdAndReservationId(@PathVariable long clientId, @PathVariable Long reservationId) {
        Optional<Client> clientOpt = clientRepository.findByClientId(clientId);
        if (clientOpt.isPresent()) {
            Optional<Reservation> reservationOpt = reservationRepository.
                    findByClientsAndReservationId(clientOpt.get(), reservationId);
            if (reservationOpt.isPresent())
                return ResponseEntity.ok(new ReservationDtoGet(reservationOpt.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/clients/{clientId}/reservations")
    public ResponseEntity<?> postReservationByClientId(@PathVariable long clientId, @Valid @RequestBody ReservationDtoPost reservation) {
        Optional<Client> clientOpt = clientRepository.findByClientId(clientId);
        if (clientOpt.isPresent()) {
            Reservation rm = new Reservation(reservation, clientOpt.get().getClientId(), roomRepository, clientRepository);
            AtomicBoolean isCorrectQuerry = new AtomicBoolean(true);
            if(!roomRepository.findByRoomId(reservation.getRoomId()).isPresent()){
                isCorrectQuerry.set(false);
            }
            reservation.getClientIds().forEach(id -> {
                if(!clientRepository.findByClientId(id).isPresent()){
                    isCorrectQuerry.set(false);
                }
            });
            if(isCorrectQuerry.get()){
                reservationRepository.save(rm);
                return ResponseEntity.ok(new ReservationDtoGet(rm));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/hotels")
    public List<HotelDtoGet> getAllHotels() {
        List<HotelDtoGet> hotelDtoGets = new ArrayList<>();
        hotelRepository.findAll()
                .forEach(hotel -> hotelDtoGets.add(new HotelDtoGet(hotel)));
        return hotelDtoGets;
    }

    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<?> getHotelById(@PathVariable long hotelId) {
        Optional<Hotel> hotelOpt = hotelRepository.findByHotelId(hotelId);
        if (hotelOpt.isPresent()) {
            return ResponseEntity.ok(new HotelDtoGet(hotelOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/hotels")
    public ResponseEntity<?> postHotel(@Valid @RequestBody HotelDtoPost hotel) {
        Hotel hl = new Hotel(hotel);
        try {
            hotelRepository.save(hl);
            return ResponseEntity.ok(new HotelDtoGet(hl));
        } catch (Exception e) {
            return ResponseEntity.status(409).body("Hotel with given address already exists in database!");
        }
    }

    @GetMapping("/hotels/{hotelId}/rooms")
    public ResponseEntity<?> getRoomByHotelId(@PathVariable long hotelId) {
        Optional<Hotel> hotelOpt = hotelRepository.findByHotelId(hotelId);
        if (hotelOpt.isPresent()) {
            List<RoomDtoGet> rooms = new ArrayList<>();
            roomRepository.findByHotel(hotelOpt.get())
                    .forEach(room -> rooms.add(new RoomDtoGet(room)));
            return ResponseEntity.ok(rooms);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/hotels/{hotelId}/rooms/{roomId}")
    public ResponseEntity<?> getRoomByHotelIdAndRoomId(@PathVariable long hotelId, @PathVariable long roomId) {
        Optional<Hotel> hotelOpt = hotelRepository.findByHotelId(hotelId);
        if (hotelOpt.isPresent()) {
            Optional<Room> roomOpt = roomRepository.findByHotelAndRoomId(hotelOpt.get(), roomId);
            if (roomOpt.isPresent()){
                return ResponseEntity.ok(new RoomDtoGet(roomOpt.get()));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @Transactional
    @PostMapping("/hotels/{hotelId}/rooms/{roomId}/transfer")
    public ResponseEntity<?> transferFromRoomByHotelIdAndRoomIdToRoom(@PathVariable long hotelId, @PathVariable long roomId, @RequestBody long newRoomId) {
        Optional<Hotel> hotelOpt = hotelRepository.findByHotelId(hotelId);
        if (hotelOpt.isPresent()) {
            Optional<Room> roomOpt = roomRepository.findByHotelAndRoomId(hotelOpt.get(), roomId);
            Optional<Room> newRoomOpt = roomRepository.findByHotelAndRoomId(hotelOpt.get(), newRoomId);
            if (roomOpt.isPresent() && newRoomOpt.isPresent() && roomOpt.get().getHotel().equals(newRoomOpt.get().getHotel())){
                roomOpt.get().setReservations(new ArrayList<>());
                roomRepository.save(roomOpt.get());
                List<Reservation> reservations = reservationRepository.findByRoom(roomOpt.get());
                reservations.forEach(reservation -> {
                    reservation.setRoom(newRoomOpt.get());
                    newRoomOpt.get().getReservations().add(reservation);
                    reservationRepository.save(reservation);
                });
                roomRepository.save(newRoomOpt.get());
                return ResponseEntity.ok("Transfer was successful");
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/hotels/{hotelId}/rooms")
    public ResponseEntity<?> postRoomByHotelId(@PathVariable long hotelId, @Valid @RequestBody RoomDtoPost room) {
        Optional<Hotel> hotelOpt = hotelRepository.findByHotelId(hotelId);
        if (hotelOpt.isPresent()) {
            Room rm = new Room(room, hotelId, hotelRepository);
            roomRepository.save(rm);
            return ResponseEntity.ok(new RoomDtoGet(rm));
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
