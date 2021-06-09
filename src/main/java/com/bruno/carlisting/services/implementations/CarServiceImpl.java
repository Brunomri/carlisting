package com.bruno.carlisting.services.implementations;

import com.bruno.carlisting.domain.Car;
import com.bruno.carlisting.domain.User;
import com.bruno.carlisting.exceptions.ObjectNotFoundException;
import com.bruno.carlisting.exceptions.entityRelationshipIntegrityException;
import com.bruno.carlisting.repositories.CarRepository;
import com.bruno.carlisting.services.interfaces.CarService;
import com.bruno.carlisting.services.interfaces.PagingService;
import com.bruno.carlisting.services.interfaces.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CarServiceImpl implements CarService {
    
    private final CarRepository carRepository;
    private final UserService userService;
    private final PagingService pagingService;

    public CarServiceImpl(CarRepository carRepository, UserService userService, PagingService pagingService) {

        this.carRepository = carRepository;
        this.userService = userService;
        this.pagingService = pagingService;
    }

    @Override
    public Page<Car> getAllCars(int page, int size) {

        Pageable pageRequest = PageRequest.of(page, size);
        Page<Car> carsPage = carRepository.findAll(pageRequest);
        pagingService.validatePage(carsPage);
        return carsPage;
    }

    @Override
    public Car getCarById(Long carId) {

        Optional<Car> car = carRepository.findById(carId);
        return car.orElseThrow(() -> new ObjectNotFoundException("Car not found! Id: " + carId));
    }

    @Override
    public Page<Car> getCarsByMake(String searchMake, int page, int size) {

        Pageable pageRequest = PageRequest.of(page, size);
        Page<Car> carsPage = carRepository.findByMake(searchMake, pageRequest);
        pagingService.validatePage(carsPage);
        return carsPage;
    }

    @Override
    public Page<Car> getCarsByUserId(Long userId, int page, int size) {

        Pageable pageRequest = PageRequest.of(page, size);
        Page<Car> carsPage = carRepository.findByUser_UserId(userId, pageRequest);
        pagingService.validatePage(carsPage);
        return carsPage;
    }

    @Override
    public Car createCar(Car newCar, Long userId) {

        User user = userService.getUserById(userId);
        newCar.setUser(user);
        try {
            return carRepository.save(newCar);
        } catch (DataIntegrityViolationException e) {
            throw new entityRelationshipIntegrityException(String.format(
                    "This car already exists:" +
                            " Make: %s -" +
                            " Model: %s -" +
                            " Year: %s -" +
                            " Trim: %s", newCar.getMake(), newCar.getModel(), newCar.getYear(), newCar.getTrim()));
        }
    }

    @Override
    public Car updateCar(Car updatedCar, Long userId, Long carId) {

        Optional<Car> optionalCar = carRepository.findById(carId);
        optionalCar.orElseThrow(() -> new ObjectNotFoundException("Car not found! Id: " + carId));
        Car currentCar = optionalCar.get();

        currentCar.setMake(updatedCar.getMake());
        currentCar.setModel(updatedCar.getModel());
        currentCar.setYear(updatedCar.getYear());
        currentCar.setTrim(updatedCar.getTrim());
        currentCar.setColor(updatedCar.getColor());
        currentCar.setTransmission(updatedCar.getTransmission());
        currentCar.setFuel(updatedCar.getFuel());
        currentCar.setUser(userService.getUserById(userId));

        try {
            return carRepository.save(currentCar);
        } catch (DataIntegrityViolationException e) {
            throw new entityRelationshipIntegrityException(String.format(
                    "This car already exists:" +
                            " Make: %s -" +
                            " Model: %s -" +
                            " Year: %s -" +
                            " Trim: %s", currentCar.getMake(), currentCar.getModel(), currentCar.getYear(), currentCar.getTrim()));
        }
    }

    @Override
    public void deleteCar(Long carId) {

        Optional<Car> carToDelete = carRepository.findById(carId);
        carRepository.delete(carToDelete.orElseThrow(() -> new ObjectNotFoundException("Car not found! Id: " + carId)));
    }
}