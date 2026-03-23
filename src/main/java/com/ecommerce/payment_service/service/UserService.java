package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.dto.UserDTO;
import com.ecommerce.payment_service.model.User;
import com.ecommerce.payment_service.repository.UserRepository;
import jdk.dynalink.linker.LinkerServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service

public class UserService {
    public final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO createUser(UserDTO dto){
        if (userRepository.existsByEmail(dto.getEmail())){
            throw new RuntimeException("Email already exists: "+ dto.getEmail());
        }
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        User saved = userRepository.save(user);
        return toDTO(saved);
    }
    public UserDTO getUserById(Long id){
        User user=userRepository.findById(id)
                .orElseThrow(()->new RuntimeException(" User not found: "+id));
        return toDTO(user);
    }

    public List<UserDTO>getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public  UserDTO updateUser(Long id, UserDTO dto){
        User user=userRepository.findById(id)
                .orElseThrow(()->new RuntimeException("user not found: "+id));
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        return toDTO(userRepository.save(user));
    }

    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        return dto;
    }
}
