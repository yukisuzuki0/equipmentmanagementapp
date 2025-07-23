package com.example.equipmentmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:mysql://localhost:3306/equipmentdb2?useSSL=false&serverTimezone=Asia/Tokyo",
	"spring.datasource.username=teamkaihatsu",
	"spring.datasource.password=password",
	"spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
	"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect"
  })
  class EquipmentmanagementApplicationTests {
	  @Test
	  void contextLoads() {
	  }
  }
  