package com.example.springboot_education.dtos.usersDTOs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserIdsRequestDto {
  private List<Integer> userIds;

}