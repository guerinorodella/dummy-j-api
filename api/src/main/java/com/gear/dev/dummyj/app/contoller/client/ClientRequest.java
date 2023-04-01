package com.gear.dev.dummyj.app.contoller.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientRequest implements Serializable {

  @JsonProperty(value = "name")
  private String name;
  @JsonProperty(value = "phone_number")
  private String phoneNumber;
  @JsonProperty(value = "email_address")
  private String emailAddress;
  @JsonProperty(value = "document_id")
  private String documentId;
  @JsonProperty(value = "status")
  private Integer status;
}
