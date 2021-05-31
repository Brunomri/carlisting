package com.bruno.carlisting.controller;

import com.bruno.carlisting.domain.User;
import com.bruno.carlisting.dtos.request.user.UserContactRequestDTO;
import com.bruno.carlisting.dtos.request.user.UserDisplayNameRequestDTO;
import com.bruno.carlisting.dtos.request.user.UserPasswordRequestDTO;
import com.bruno.carlisting.dtos.request.user.UserRequestDTO;
import com.bruno.carlisting.dtos.request.user.UserRolesRequestDTO;
import com.bruno.carlisting.dtos.response.user.UserPrivateResponseDTO;
import com.bruno.carlisting.dtos.response.user.UserPublicResponseDTO;
import com.bruno.carlisting.services.interfaces.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.net.URI;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "/users")
public class UserController {

    private static final String USER_PAGE_DEFAULT_NUMBER = "0";
    private static final String USER_PAGE_DEFAULT_SIZE = "1";
    private static final int USER_PAGE_MIN_NUMBER = 0;
    private static final int USER_PAGE_MIN_SIZE = 1;
    private static final int USER_PAGE_MAX_SIZE = 10;

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation(value = "Return all users grouped in pages")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Return a page of users"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Page content not found"),
        @ApiResponse(code = 500, message = "Server exception"),
    })
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<Page<UserPublicResponseDTO>> findAllUsers(

        @RequestParam(value = "page", required = false, defaultValue = USER_PAGE_DEFAULT_NUMBER)
        @Min(value = USER_PAGE_MIN_NUMBER,
            message = "Page number must be greater than or equal to " + USER_PAGE_MIN_NUMBER) int page,

        @RequestParam(value = "size", required = false, defaultValue = USER_PAGE_DEFAULT_SIZE)
        @Min(value = USER_PAGE_MIN_SIZE,
            message = "Page size must be greater than or equal to " + USER_PAGE_MIN_SIZE)
        @Max(value = USER_PAGE_MAX_SIZE,
                message = "Page size must be less than or equal to " + USER_PAGE_MAX_SIZE) int size) {

        Page<UserPublicResponseDTO> usersPageDTO = UserPublicResponseDTO.toUsersPagePublicDTO(
                userService.getAllUsers(page, size));
        return ResponseEntity.ok().body(usersPageDTO);
    }

    @ApiOperation(value = "Find a user by ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Return the user with corresponding ID"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "User not found"),
        @ApiResponse(code = 500, message = "Server exception"),
    })
    @GetMapping(value = "/{userId}", produces = "application/json")
    public ResponseEntity<UserPublicResponseDTO> findUserById(

        @PathVariable @Positive(message = "User ID must be a positive integer") Long userId) {

        User user = userService.getUserById(userId);
        return ResponseEntity.ok().body(UserPublicResponseDTO.toUserPublicDTO(user));
    }

    @ApiOperation(value = "Find a user by a car ID")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Return the user with associated to a car ID"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Server exception"),
    })
    @GetMapping(value = "/cars/{carId}", produces = "application/json")
    public ResponseEntity<UserPublicResponseDTO> findUserByCarId(

        @PathVariable @Positive(message = "Car ID must be a positive integer") Long carId) {

        User user = userService.getUserByCarId(carId);
        return ResponseEntity.ok().body(UserPublicResponseDTO.toUserPublicDTO(user));
    }

//    todo: Create User DTO to avoid passing rolesIds in the query parameters
    @ApiOperation(value = "Add a new user")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "New user created"),
        @ApiResponse(code = 400, message = "Invalid User data provided"),
        @ApiResponse(code = 500, message = "Server exception"),
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserPrivateResponseDTO> createUser(

        @Valid @RequestBody UserRequestDTO userRequestDTO) {

        User newUser = userService.createUser(userRequestDTO.toUser(), userRequestDTO.getRolesIds());
        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/users/{userId}").
                buildAndExpand(newUser.getUserId()).toUri();
        return ResponseEntity.created(uri).body(UserPrivateResponseDTO.toUserPrivateDTO(newUser));
    }

    @ApiOperation(value = "Update an existing user")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "User updated"),
        @ApiResponse(code = 400, message = "Invalid User data provided"),
        @ApiResponse(code = 404, message = "User not found"),
        @ApiResponse(code = 500, message = "Server exception"),
    })
    @PutMapping(value = "/{userId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserPrivateResponseDTO> updateUser(

        @PathVariable @Positive(message = "User ID must be a positive integer") Long userId,

        @Valid @RequestBody UserRequestDTO userRequestDTO) {

        User updatedUser = userService.updateUser(userRequestDTO.toUser(), userRequestDTO.getRolesIds(), userId);
        return ResponseEntity.ok().body(UserPrivateResponseDTO.toUserPrivateDTO(updatedUser));
    }

    @ApiOperation(value = "Update a user's password")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "User's password updated"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Server exception"),
    })
    @PatchMapping(value = "/{userId}/password", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserPrivateResponseDTO> updateUserPassword(

            @PathVariable @Positive(message = "User ID must be a positive integer") Long userId,

            @Valid @RequestBody UserPasswordRequestDTO userPasswordRequestDTO) {

        User updatedUser = userService.updateUserPassword(userPasswordRequestDTO.getPassword(), userId);
        return ResponseEntity.ok().body(UserPrivateResponseDTO.toUserPrivateDTO(updatedUser));
    }

    @ApiOperation(value = "Update a user's display name")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "User's display name updated"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Server exception"),
    })
    @PatchMapping(value = "/{userId}/displayName", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserPrivateResponseDTO> updateUserDisplayName(

        @PathVariable @Positive(message = "User ID must be a positive integer") Long userId,

        @Valid @RequestBody UserDisplayNameRequestDTO userDisplayNameRequestDTO) {

        User updatedUser = userService.updateUserDisplayName(userDisplayNameRequestDTO.getDisplayName(), userId);
        return ResponseEntity.ok().body(UserPrivateResponseDTO.toUserPrivateDTO(updatedUser));
    }

    @ApiOperation(value = "Update a user's contact")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "User's contact updated"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Server exception"),
    })
    @PatchMapping(value = "/{userId}/contact", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserPrivateResponseDTO> updateUserContact(

            @PathVariable @Positive(message = "User ID must be a positive integer") Long userId,

            @Valid @RequestBody UserContactRequestDTO userContactRequestDTO) {

        User updatedUser = userService.updateUserContact(userContactRequestDTO.getContact(), userId);
        return ResponseEntity.ok().body(UserPrivateResponseDTO.toUserPrivateDTO(updatedUser));
    }

    @ApiOperation(value = "Update a user's roles")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "User's roles updated"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Server exception"),
    })
    @PatchMapping(value = "/{userId}/roles", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserPrivateResponseDTO> updateUserRoles(

            @PathVariable @Positive(message = "User ID must be a positive integer") Long userId,

            @Valid @RequestBody UserRolesRequestDTO userRolesRequestDTO) {

        User updatedUser = userService.updateUserRoles(userRolesRequestDTO.getRolesIds(), userId);
        return ResponseEntity.ok().body(UserPrivateResponseDTO.toUserPrivateDTO(updatedUser));
    }

    @ApiOperation(value = "Delete an existing user")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "User deleted"),
            @ApiResponse(code = 500, message = "Server exception"),
    })
    @DeleteMapping(value = "/{userId}")
    public ResponseEntity<Void> deleteUser(

        @PathVariable @Positive(message = "User ID must be a positive integer") Long userId) {

        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
