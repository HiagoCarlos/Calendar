package br.com.calendar.user.dto;

import jakarta.validation.constraints.Email;

public record UpdateUserDTO(

        String name,

        @Email(message = "Email invalido")
        String email){
}
