package com.gear.dev.dummyj.core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clients")
public class ClientModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(length = 150)
  private String name;
  @Column(name = "phone_number", length = 20)
  private String phoneNumber;
  @Column(name = "email_address", length = 255)
  private String emailAddress;
  @Column(name = "document_id", length = 100)
  private String documentId;
  @Column(name = "created_date")
  private LocalDateTime createdDate;
//  @TODO map to Enum ClientStatus - ok, blocked, in_debit
  @Column(name = "status")
  private Integer status;
}
