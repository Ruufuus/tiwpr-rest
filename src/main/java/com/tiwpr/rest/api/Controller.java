package com.tiwpr.rest.api;

import com.tiwpr.rest.garbageCollector.ReservationGarbageCollector;
import com.tiwpr.rest.model.TransferRequestBody;
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
import com.tiwpr.rest.model.dto.post.RoomDtoPost;
import com.tiwpr.rest.model.dto.put.ClientDtoPut;
import com.tiwpr.rest.model.dto.put.ReservationDtoPut;
import com.tiwpr.rest.repository.ClientRepository;
import com.tiwpr.rest.repository.HotelRepository;
import com.tiwpr.rest.repository.ReservationRepository;
import com.tiwpr.rest.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
    public ResponseEntity<?> getClientByClientId(@PathVariable long clientId) throws NoSuchAlgorithmException {
        Optional<Client> clientOpt = clientRepository.findByClientId(clientId);
        if (clientOpt.isPresent()) {
            byte[] bytesOfMessage = new ClientDtoGet(clientOpt.get()).toString().getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("MD5");
            return ResponseEntity.ok().eTag(DatatypeConverter.printHexBinary(md.digest(bytesOfMessage))).body(new ClientDtoGet(clientOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/clients/{clientId}")
    public ResponseEntity<?> putClientByClientId(@RequestHeader(value = "If-Matching", required = false)
                                                         String ifMatching, @PathVariable long clientId,
                                                 @Valid @RequestBody ClientDtoPut client) throws NoSuchAlgorithmException {
        if (ifMatching != null) {
            Optional<Client> clientOpt = clientRepository.findByClientId(clientId);
            if (clientOpt.isPresent()) {
                byte[] bytesOfMessage = new ClientDtoGet(clientOpt.get()).toString().getBytes(StandardCharsets.UTF_8);
                MessageDigest md = MessageDigest.getInstance("MD5");
                if (ifMatching.equals(DatatypeConverter.printHexBinary(md.digest(bytesOfMessage)))) {
                    clientOpt.get().setName(client.getName());
                    clientOpt.get().setSurname(client.getSurname());
                    AtomicBoolean isCorrectQuery = new AtomicBoolean(true);
                    prepareClientNewReservationList(client, clientOpt.get(), isCorrectQuery);
                    if (isCorrectQuery.get()) {
                        clientRepository.save(clientOpt.get());
                        bytesOfMessage = new ClientDtoGet(clientOpt.get()).toString().getBytes(StandardCharsets.UTF_8);
                        return ResponseEntity.ok().eTag(DatatypeConverter.printHexBinary(md.digest(bytesOfMessage))).body(new ClientDtoGet(clientOpt.get()));
                    }else{
                        ResponseEntity.notFound().build();
                    }
                }
                return ResponseEntity.status(412).build();
            }
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.status(403).build();
    }

    private void prepareClientNewReservationList(@RequestBody @Valid ClientDtoPut client, Client clientDataBase, AtomicBoolean isCorrectQuery) {
        ArrayList<Reservation> newReservations = new ArrayList<>();
        client.getReservations().forEach(reservationId -> {
            Optional<Reservation> reservation = reservationRepository.findByReservationId(reservationId);
            if (reservation.isPresent()) {
                newReservations.add(reservation.get());
            } else {
                isCorrectQuery.set(false);
            }
        });
        clientDataBase.changeReservationList(newReservations);
    }

    @PatchMapping("/clients/{clientId}")
    public ResponseEntity<?> patchClientByClientId(@PathVariable long clientId, @RequestBody ClientDtoPut client) {
        Optional<Client> clientOpt = clientRepository.findByClientId(clientId);
        if (clientOpt.isPresent()) {
            if (client.getName() != null)
                clientOpt.get().setName(client.getName());
            if (client.getSurname() != null)
                clientOpt.get().setSurname(client.getSurname());
            AtomicBoolean isCorrectQuery = new AtomicBoolean(true);
            if (client.getReservations() != null) {
                prepareClientNewReservationList(client, clientOpt.get(), isCorrectQuery);
            }
            if (isCorrectQuery.get()) {
                clientRepository.save(clientOpt.get());
                return ResponseEntity.ok(new ClientDtoGet(clientOpt.get()));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/clients/{clientId}")
    public ResponseEntity<?> deleteClientByClientId(@PathVariable long clientId) {
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
                    .forEach(reservation -> reservations.add(new ReservationDtoGet(reservation, hotelRepository)));
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
                return ResponseEntity.ok(new ReservationDtoGet(reservationOpt.get(), hotelRepository));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/clients/{clientId}/reservations")
    public ResponseEntity<?> postReservationByClientId(@PathVariable long clientId) {
        Reservation rm = new Reservation();
        reservationRepository.save(rm);
        new ReservationGarbageCollector(rm.getReservationId(), reservationRepository);
        Map<String, Long> result = new HashMap<>();
        result.put("reservationId", rm.getReservationId());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/clients/{clientId}/reservations/{reservationId}")
    public ResponseEntity<?> putReservationByClientIdAndByReservationId(@PathVariable long clientId, @PathVariable long reservationId, @Valid @RequestBody ReservationDtoPut reservation) {
        Optional<Client> clientOpt = clientRepository.findByClientId(clientId);
        if (clientOpt.isPresent()) {
            Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
            if (reservationOpt.isPresent()) {
                AtomicBoolean isCorrectQuery = new AtomicBoolean(true);
                if (!roomRepository.findByRoomId(reservation.getRoomId()).isPresent()) {
                    isCorrectQuery.set(false);
                }
                reservation.getClientIds().forEach(id -> {
                    if (!clientRepository.findByClientId(id).isPresent()) {
                        isCorrectQuery.set(false);
                    }
                });
                if (isCorrectQuery.get()) {
                    Reservation rm = reservationOpt.get();
                    prepareReservation(clientId, reservation, rm);
                    reservationRepository.save(rm);
                    return ResponseEntity.ok(new ReservationDtoGet(rm, hotelRepository));
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    private void prepareReservation(long clientId, ReservationDtoPut reservation, Reservation rm) {
        Optional<Room> roomOpt = roomRepository.findById(reservation.getRoomId());
        roomOpt.ifPresent(rm::setRoom);
        rm.setDate(reservation.getDate());
        if(!reservation.getClientIds().contains(clientId)){
            reservation.getClientIds().add(clientId);
        }
        reservation.getClientIds().forEach(id -> {
            Optional<Client> clientOpt2 = clientRepository.findByClientId(id);
            if (clientOpt2.isPresent()) {
                if (!rm.getClients().contains(clientOpt2.get()))
                    rm.getClients().add(clientOpt2.get());
                if(!clientOpt2.get().getReservations().contains(rm))
                    clientOpt2.get().getReservations().add(rm);
            }
        });
    }

    @GetMapping("/hotels")
    public List<HotelDtoGet> getAllHotels() {
        List<HotelDtoGet> hotelDtoGets = new ArrayList<>();
        hotelRepository.findAll()
                .forEach(hotel -> hotelDtoGets.add(new HotelDtoGet(hotel)));
        return hotelDtoGets;
    }

    @GetMapping("/reservations")
    public List<ReservationDtoGet> getAllReservations() {
        List<ReservationDtoGet> reservationDtoGets = new ArrayList<>();
        reservationRepository.findAll()
                .forEach(reservation -> reservationDtoGets.add(new ReservationDtoGet(reservation, hotelRepository)));
        return reservationDtoGets;
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<?> getReservationByReservationId(@PathVariable long reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if (reservationOpt.isPresent()) {
            return ResponseEntity.ok(new ReservationDtoGet(reservationOpt.get(), hotelRepository));
        } else {
            return ResponseEntity.notFound().build();
        }
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

    @GetMapping("/hotels/{hotelId}/reservations")
    public ResponseEntity<?> getReservationsByHotelId(@PathVariable long hotelId) {
        Optional<Hotel> hotelOpt = hotelRepository.findByHotelId(hotelId);
        if (hotelOpt.isPresent()) {
            List<Room> rooms = new ArrayList<>(roomRepository.findByHotel(hotelOpt.get()));
            List<ReservationDtoGet> reservations = new ArrayList<>();
            rooms.forEach(room -> {
                reservationRepository.findByRoom(room)
                        .forEach(reservation -> reservations.add(new ReservationDtoGet(reservation, hotelRepository)));
            });
            return ResponseEntity.ok(reservations);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/hotels/{hotelId}/reservations/{reservationId}")
    public ResponseEntity<?> getReservationsByHotelIdAndReservationId(@PathVariable long hotelId, @PathVariable long reservationId) {
        Optional<Hotel> hotelOpt = hotelRepository.findByHotelId(hotelId);
        if (hotelOpt.isPresent()) {
            List<Room> rooms = new ArrayList<>(roomRepository.findByHotel(hotelOpt.get()));
            AtomicReference<ReservationDtoGet> reservationDtoGet = new AtomicReference<>();
            AtomicReference<Boolean> isReservationFound = new AtomicReference<>();
            isReservationFound.set(false);
            rooms.forEach(room -> {
                Optional<Reservation> reservationOpt = reservationRepository.findByRoomAndReservationId(room, reservationId);
                reservationOpt.ifPresent(reservation -> {
                    reservationDtoGet.set(new ReservationDtoGet(reservation, hotelRepository));
                    isReservationFound.set(true);
                });
            });
            if (isReservationFound.get()) {
                return ResponseEntity.ok(reservationDtoGet.get());
            }
        }
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/hotels/{hotelId}/rooms/{roomId}")
    public ResponseEntity<?> getRoomByHotelIdAndRoomId(@PathVariable long hotelId, @PathVariable long roomId) {
        Optional<Hotel> hotelOpt = hotelRepository.findByHotelId(hotelId);
        if (hotelOpt.isPresent()) {
            Optional<Room> roomOpt = roomRepository.findByHotelAndRoomId(hotelOpt.get(), roomId);
            if (roomOpt.isPresent()) {
                return ResponseEntity.ok(new RoomDtoGet(roomOpt.get()));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/hotels/{hotelId}/rooms/{roomId}/reservations")
    public ResponseEntity<?> getReservationByHotelIdAndRoomId(@PathVariable long hotelId, @PathVariable long roomId) {
        Optional<Hotel> hotelOpt = hotelRepository.findByHotelId(hotelId);
        if (hotelOpt.isPresent()) {
            Optional<Room> roomOpt = roomRepository.findByHotelAndRoomId(hotelOpt.get(), roomId);
            if (roomOpt.isPresent()) {
                ArrayList<ReservationDtoGet> reservationDtoGets = new ArrayList<>();
                reservationRepository.findByRoom(roomOpt.get()).forEach(reservation -> reservationDtoGets.add(new ReservationDtoGet(reservation, hotelRepository)));
                return ResponseEntity.ok(reservationDtoGets);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/hotels/{hotelId}/rooms/{roomId}/reservations/{reservationId}")
    public ResponseEntity<?> getReservationByHotelIdAndRoomIdAndReservationId(@PathVariable long hotelId, @PathVariable long roomId, @PathVariable long reservationId) {
        Optional<Hotel> hotelOpt = hotelRepository.findByHotelId(hotelId);
        if (hotelOpt.isPresent()) {
            Optional<Room> roomOpt = roomRepository.findByHotelAndRoomId(hotelOpt.get(), roomId);
            if (roomOpt.isPresent()) {
                Optional<Reservation> reservationOpt = reservationRepository.findByRoomAndReservationId(roomOpt.get(), reservationId);
                if (reservationOpt.isPresent()) {
                    return ResponseEntity.ok(new ReservationDtoGet(reservationOpt.get(), hotelRepository));
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    @Transactional
    @PostMapping("/hotels/{hotelId}/transfers")
    public ResponseEntity<?> transferFromRoomByHotelIdToNewRoom(@PathVariable long hotelId, @Valid @RequestBody TransferRequestBody transferData) {
        Optional<Room> roomOpt = roomRepository.findById(transferData.getCurrentRoomId());
        Optional<Room> newRoomOpt = roomRepository.findById(transferData.getNewRoomId());
        if (roomOpt.isPresent() && newRoomOpt.isPresent() &&
                roomOpt.get().getHotel().getHotelId() == hotelId && newRoomOpt.get().getHotel().getHotelId() == hotelId) {
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
