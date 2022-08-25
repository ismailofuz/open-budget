package com.example.openbudget.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ProjectDTO {
    private String title;
    private Integer title_id;
    private boolean status;
}
