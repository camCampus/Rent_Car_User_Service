package com.micro.demo.Services;


import com.micro.demo.Exception.LicenseNotValidException;
import com.micro.demo.Exception.NoSuchUserExistsException;
import com.micro.demo.Exception.UserAlreadyExistsException;
import com.micro.demo.Model.User;
import com.micro.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private  RestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;


    @Override
    public Iterable<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(Long licenseNumber) {
        return userRepository.findById(licenseNumber).orElseThrow(
                ()-> new NoSuchUserExistsException("User not found for license Number: " + licenseNumber)
        );
    }

    @Override
    public ResponseEntity<String> addUser(User user) {
        User existingUser = userRepository.findById(user.getLicenseNumber()).orElse(null);

        if (existingUser == null) {
            if (this.checkLicense(user.getLicenseNumber())) {
                userRepository.save(user);
                return ResponseEntity.status(HttpStatus.OK).body("User added successfully");
           } else {
                throw new LicenseNotValidException();
            }
        }
        else
            throw new UserAlreadyExistsException();
    }

    @Override
    public ResponseEntity<String> updateUser(User user) {
        User existingUser = userRepository.findById(user.getLicenseNumber()).orElse(null);
        if (existingUser == null) {
            throw new NoSuchUserExistsException("User not found for license Number: " + user.getLicenseNumber());
        } else {
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body("Record updated Successfully");
        }
    }
    @Override
    public ResponseEntity<String> deleteUser(Long licenseNumber) {
        User existingUser = userRepository.findById(licenseNumber).orElse(null);
        if (existingUser == null) {
            throw new NoSuchUserExistsException("User not found for license Number: " + licenseNumber);
        } else {
            userRepository.delete(existingUser);
            return ResponseEntity.status(HttpStatus.OK).body("Record delete Successfully");
        }
    }

    public Boolean checkLicense(Long licenseId)
    {
        String url = "http://localhost:8081/licenses/" + licenseId;
        return restTemplate.getForObject(url, Boolean.class);
    }
}
