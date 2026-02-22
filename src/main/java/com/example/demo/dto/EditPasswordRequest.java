package com.example.demo.dto;

import lombok.Data;

@Data
public class EditPasswordRequest {
	private String newPass;
	private String oldPass;

}
